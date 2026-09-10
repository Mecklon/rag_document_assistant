package com.mecklon.backend.controller;

import com.mecklon.backend.configuration.CustomUserDetails;
import com.mecklon.backend.dtos.*;
import com.mecklon.backend.model.User;
import com.mecklon.backend.repositories.UserRepository;
import com.mecklon.backend.service.WorkspaceService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkspaceController {
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final EmbeddingModel embeddingModel;



    @PostMapping("/autoLogin")
    public ResponseEntity<AuthResponse> autoLogin(Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(user.getId(), null, userDetails.getUsername(), userDetails.getDisplayUsername()));
    }


    @GetMapping("/workspaces")
    public ResponseEntity<List<WorkspaceDTO>> getWorkspaces(Authentication auth) {

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        List<WorkspaceDTO> res = workspaceService.getWorkspaces(userDetails);
        return ResponseEntity.status(HttpStatus.OK).body(res);

    }

    @PostMapping("/addDocument")
    public ResponseEntity<WorkspaceDTO> addDocument(Authentication auth, AddDocumentRequest request, MultipartFile file) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        WorkspaceDTO res = workspaceService.addDocument(userDetails, request,file);
        return ResponseEntity.status(HttpStatus.OK).body(res);

    }
    @GetMapping("/getWorkspaceDetails")
    public ResponseEntity<WorkspaceDTO> getWorkspaceDetails(@RequestParam UUID workspaceId){
        return ResponseEntity.status(HttpStatus.OK).body(workspaceService.getWorkspaceDetails(workspaceId));
    }

    @GetMapping("/documents/{documentId}/file")
    public ResponseEntity<Resource> getDocumentFile(
            @PathVariable UUID documentId,
            Authentication auth
    ) {
        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();

        Resource resource = workspaceService.getDocumentFile(userDetails, documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/test/{text}")
    public float[] getEmbeds(@PathVariable String text){
        return embeddingModel.embed(text);
    }


//    @GetMapping("/test/prompt/{message}/{workspaceId}")
//    public String getPrompt(
//            @PathVariable String message,
//            @PathVariable String workspaceId) {
//        return chatClient.prompt()
//                .advisors(a -> a.param(
//                        ChatMemory.CONVERSATION_ID,
//                        workspaceId
//                ))
//                .user(message)
//                .call()
//                .content();
//    }

    @PostMapping("/prompt")
    public ResponseEntity<MessageDTO> prompt(@RequestBody PromptRequest request){
        System.out.println("workspace id: "+request.getWorkspaceId());
        System.out.println();
        System.out.println();
        return ResponseEntity.status(HttpStatus.OK).body(workspaceService.getMessage(request));
    }

}
