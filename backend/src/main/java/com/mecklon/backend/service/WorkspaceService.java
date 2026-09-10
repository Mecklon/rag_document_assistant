package com.mecklon.backend.service;


import com.mecklon.backend.configuration.CustomUserDetails;
import com.mecklon.backend.dtos.*;
import com.mecklon.backend.model.Citation;
import com.mecklon.backend.model.Document;
import com.mecklon.backend.model.Message;
import com.mecklon.backend.model.Workspace;
import com.mecklon.backend.model.types.DocumentType;
import com.mecklon.backend.model.types.MessageRole;
import com.mecklon.backend.repositories.DocumentRepository;
import com.mecklon.backend.repositories.MessageRepository;
import com.mecklon.backend.repositories.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final DocumentRepository documentRepository;
    private final EmbeddingModel embeddingModel;
    private final TokenTextSplitter splitter;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final MessageRepository messageRepository;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            DocumentRepository documentRepository,
            EmbeddingModel embeddingModel,
            TokenTextSplitter tokenTextSplitter,
            VectorStore vectorStore,
            ChatClient.Builder builder,
            MessageRepository messageRepository
    ){
        this.workspaceRepository = workspaceRepository;
        this.documentRepository = documentRepository;
        this.embeddingModel = embeddingModel;
        this.splitter = tokenTextSplitter;
        this.vectorStore = vectorStore;
        this.messageRepository = messageRepository;

        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<WorkspaceDTO> getWorkspaces(CustomUserDetails userDetails) {
        List<Workspace> res= workspaceRepository.findAllByUserId(userDetails.getId());
        System.out.println("workspaces");

        System.out.println(res);
        System.out.println("workspaces");
        return res.stream().map(workspace -> new WorkspaceDTO(workspace.getId(), workspace.getName(), workspace.getCreatedAt(), workspace.getLastUsed(), null, null)).toList();
    }

    public WorkspaceDTO addDocument(CustomUserDetails userDetails, AddDocumentRequest request, MultipartFile file) {
        if(request.getWorkspaceId()==null){
            Workspace newWorkspace = Workspace.builder()
                    .name(file.getOriginalFilename())
                    .conversationId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .lastUsed(LocalDateTime.now())
                    .user(userDetails.getUser())
                    .build();
            workspaceRepository.save(newWorkspace);
            request.setWorkspaceId(newWorkspace.getId());
        }
        try{
            Workspace workspace = workspaceRepository
                    .findById(request.getWorkspaceId())
                    .orElseThrow(() -> new RuntimeException("Workspace not found"));
            String uniqueName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            String path = uploadDir + File.separator + uniqueName;
            file.transferTo(new File(path));
            Document document = new Document().builder().name(uniqueName)
                    .referencePath(path)
                    .workspace(workspace)
                    .type(DocumentType.PDF)
                    .build();
            documentRepository.save(document);
            List<Document> documentList = workspace.getDocuments();
            List<Message> messageList = workspace.getMessages();
            workspace.getDocuments().add(document);

            PagePdfDocumentReader reader =
                    new PagePdfDocumentReader(
                            new FileSystemResource(path)
                    );

            List<org.springframework.ai.document.Document> pages =
                    reader.get();

            for (int i = 0; i < pages.size(); i++) {

                org.springframework.ai.document.Document page =
                        pages.get(i);

                page.getMetadata().put(
                        "workspaceId",
                        workspace.getId().toString()
                );

                page.getMetadata().put(
                        "documentId",
                        document.getId().toString()
                );

                page.getMetadata().put(
                        "pageNumber",
                        i + 1
                );
            }
            List<org.springframework.ai.document.Document> chunks =
                    splitter.split(pages);
            vectorStore.add(chunks);

            WorkspaceDTO res = WorkspaceDTO.builder()
                    .id(workspace.getId())
                    .name(workspace.getName())
                    .createdAt(workspace.getCreatedAt())
                    .lastUsed(workspace.getLastUsed())
                    .document(documentList.stream().map(doc -> new DocumentDTO(doc.getId(),
                            doc.getName(),
                            doc.getReferencePath(),
                            doc.getType())).toList())
                    .messages(messageList.stream().map(mess-> new MessageDTO(
                            mess.getId(),
                            mess.getContent(),
                            mess.getRole(),
                            mess.getCreatedAt(),
                            mess.getWorkspace().getId(),
                            mess.getCitations().stream().map(cit-> new CitationDTO(
                                    cit.getId(),
                                    cit.getPageNumber(),
                                    cit.getText(),
                                    cit.getDocument().getId())).toList()))
                            .toList())
                    .build();
            return res;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public WorkspaceDTO getWorkspaceDetails(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()->new RuntimeException("This repository does not exist"));
        return new WorkspaceDTO(
                workspace.getId(),
                workspace.getName(),
                workspace.getCreatedAt(),
                workspace.getLastUsed(),
                workspace.getDocuments().stream().map(document ->
                        new DocumentDTO(
                                document.getId(),
                                document.getName(),
                                document.getReferencePath(),
                                document.getType()
                        )
                ).toList(),
                workspace.getMessages().stream().map(message ->
                        new MessageDTO(
                                message.getId(),
                                message.getContent(),
                                message.getRole(),
                                message.getCreatedAt(),
                                workspace.getId(),
                                message.getCitations().stream().map(citation ->
                                        new CitationDTO(
                                                citation.getId(),
                                                citation.getPageNumber(),
                                                citation.getText(),
                                                citation.getDocument().getId()
                                        )
                                ).toList()
                        )
                ).toList()
        );
    }
    public Resource getDocumentFile(CustomUserDetails userDetails, UUID documentId) {

        Document document = documentRepository
                .findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getWorkspace().getUser().getId()
                .equals(userDetails.getUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Resource resource = new FileSystemResource(
                document.getReferencePath()
        );

        if (!resource.exists()) {
            throw new RuntimeException("File not found");
        }

        return resource;
    }

    public MessageDTO getMessage(PromptRequest request) {

        Workspace workspace = workspaceRepository
                .findById(request.getWorkspaceId())
                .orElseThrow(() ->
                        new RuntimeException("This repository does not exist"));

        String conversationId = workspace.getConversationId().toString();
        System.out.println("got workspace");
        Message userMessage = Message.builder()
                .content(request.getPrompt())
                .role(MessageRole.USER)
                .createdAt(LocalDateTime.now())
                .workspace(workspace)
                .build();

        messageRepository.save(userMessage);
        System.out.println("saved user message");
        List<org.springframework.ai.document.Document> chunks =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(request.getPrompt())
                                .topK(8)
                                .filterExpression(
                                        "workspaceId == '" +
                                                request.getWorkspaceId() +
                                                "'"
                                )
                                .build()
                );
        System.out.println("got chunks");
        String context = IntStream.range(0, chunks.size())
                .mapToObj(i -> {

                    org.springframework.ai.document.Document chunk =
                            chunks.get(i);

                    return """
                        --- CHUNK %d ---
                        Page Number: %s
                        Content:
                        %s
                        --- END CHUNK %d ---
                        """.formatted(
                            i,
                            chunk.getMetadata().get("pageNumber"),
                            chunk.getText(),
                            i
                    );
                })
                .collect(Collectors.joining("\n\n"));

        BeanOutputConverter<ModelResponse> converter =
                new BeanOutputConverter<>(ModelResponse.class);

        String prompt = """
            You are a document question-answering assistant.

            Answer the user's question using ONLY the information contained
            in the provided document chunks.

            USER QUESTION:
            %s

            RETRIEVED DOCUMENT CHUNKS:
            %s

            CITATION RULES:

            1. Answer only using information contained in the provided chunks.
            2. Every factual claim based on the documents should be supported
               by one or more citations.
            3. citationIndex must contain ONLY the indexes of the chunks
               provided above.
            4. The indexes are zero-based.
            5. Never invent a citation index.
            6. If multiple chunks support the answer, include all relevant
               chunk indexes.
            7. If the provided chunks do not contain enough information,
               clearly state that the information is not available.
            8. Do not use outside knowledge to fill missing information.
            9. Keep the answer clear and concise.
            10. Do not include citation indexes that do not support the answer.

            Return the response using exactly this structure:

            %s
            """.formatted(
                request.getPrompt(),
                context,
                converter.getFormat()
        );

        ModelResponse modelResponse = chatClient.prompt()
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId
                ))
                .user(prompt)
                .call()
                .entity(ModelResponse.class);
        System.out.println("got prompt");

        Message message = Message.builder()
                .content(modelResponse.getAnswer())
                .role(MessageRole.ASSISTANT)
                .createdAt(LocalDateTime.now())
                .workspace(workspace)
                .build();

        List<Citation> citations = modelResponse.getCitationIndex()
                .stream()
                .filter(index -> index >= 0 && index < chunks.size())
                .distinct()
                .map(index -> {

                    org.springframework.ai.document.Document chunk =
                            chunks.get(index);

                    UUID documentId = UUID.fromString(
                            chunk.getMetadata()
                                    .get("documentId")
                                    .toString()
                    );

                    Document document = documentRepository
                            .findById(documentId)
                            .orElseThrow(() ->
                                    new RuntimeException("Document not found"));

                    return Citation.builder()
                            .pageNumber(
                                    Integer.parseInt(
                                            chunk.getMetadata()
                                                    .get("pageNumber")
                                                    .toString()
                                    )
                            )
                            .text(chunk.getText())
                            .document(document)
                            .message(message)
                            .build();

                })
                .toList();

        message.setCitations(citations);

        Message savedMessage = messageRepository.save(message);

        return new MessageDTO(
                savedMessage.getId(),
                savedMessage.getContent(),
                savedMessage.getRole(),
                savedMessage.getCreatedAt(),
                savedMessage.getWorkspace().getId(),
                savedMessage.getCitations()
                        .stream()
                        .map(citation -> new CitationDTO(
                                citation.getId(),
                                citation.getPageNumber(),
                                citation.getText(),
                                citation.getDocument().getId()
                        ))
                        .toList()
        );
    }
}
