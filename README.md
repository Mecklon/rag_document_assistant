# PDF RAG Assistant 📄🤖

An AI-powered document question answering application built with **Spring Boot**, **React**, **Spring AI**, and **pgvector**. Upload a PDF and chat with it using Retrieval-Augmented Generation (RAG). The assistant retrieves the most relevant sections of the document and answers questions with **page-level citations** that link directly back to the source.

---

## Tech Stack

### Backend

- Spring Boot 4.1.1
- Spring AI
- Java 21
- PostgreSQL
- pgvector
- Apache PDFBox
- OpenRouter API

### Frontend

- React
- Vite
- Tailwind CSS
- Axios
- React PDF Viewer

### AI Models

- Gemini Flash (Answer Generation)
- Gemini Embedding 001 (1536-dimensional embeddings)

---

## Features

- Drag-and-drop PDF upload.
- Chat with uploaded documents using semantic search.
- Automatic document chunking and embedding generation.
- Page and line citations for every response.
- Clickable citations that navigate directly to the relevant PDF page.
- Workspace-based conversations.
- Persistent vector storage using pgvector.
- Streaming AI responses.
- Responsive split-screen interface for reading and chatting.

---

# Architecture

The application separates document storage, vector retrieval, and AI generation into independent layers.

## PostgreSQL + pgvector

Used for persistent document storage and semantic search.

Stores:

- Document metadata.
- Text chunks.
- Vector embeddings.
- Workspace and chat metadata.

## Local File Storage

Original PDFs are stored on the backend filesystem.

Stores:

- Uploaded PDF files.
- Unique filenames to avoid collisions.

---

# RAG Pipeline

Every uploaded document goes through an indexing pipeline before it becomes searchable.

```text
PDF Upload
      │
      ▼
PDF Text Extraction (PDFBox)
      │
      ▼
Chunking + Metadata
(Page Number, Line Range)
      │
      ▼
Embedding Generation
(Gemini Embedding 001)
      │
      ▼
pgvector Storage
      │
      ▼
Semantic Retrieval
      │
      ▼
Gemini Flash
      │
      ▼
Answer + Citations
```

---

# Document Processing

Each uploaded PDF is processed into searchable chunks.

For every chunk, the backend stores:

- Document ID.
- Page number.
- Starting line number.
- Ending line number.
- Chunk text.
- 1536-dimensional embedding vector.

This metadata allows answers to reference the exact location inside the original document.

---

# Retrieval Flow

The application combines vector search with LLM generation.

### Upload Time

1. Upload PDF.
2. Extract text using PDFBox.
3. Split text into chunks.
4. Generate embeddings.
5. Store embeddings inside pgvector.

### Query Time

1. Convert user question into an embedding.
2. Perform similarity search in pgvector.
3. Retrieve the top matching chunks.
4. Build the prompt using retrieved context.
5. Generate an answer using Gemini Flash.
6. Return the answer along with citations.

---

# Citations

Every generated response includes source references.

Example:

```text
Spring Boot applications start from the main application class.

(Page 12 • Lines 84–101)
```

Clicking the citation opens the PDF viewer directly on **Page 12**.

This allows users to verify every answer against the original document.

---

# Workspace System

Each document conversation belongs to a workspace.

A workspace contains:

- Workspace name.
- Uploaded document.
- Conversation history.
- Last accessed timestamp.

This allows users to keep independent chats for different PDFs.

---

# Real-Time Chat Flow

The frontend keeps the chat and PDF synchronized.

```text
User Question
      │
      ▼
Backend Retrieval
      │
      ▼
Streaming AI Response
      │
      ▼
Citation Metadata
      │
      ▼
PDF Viewer Navigation
```

Users can continue asking follow-up questions while the previous conversation context is maintained.

---

# Design Philosophy

The project focuses on building a simple but production-style RAG architecture.

Principles followed:

```text
Document Processing
        ↓
Semantic Retrieval
        ↓
Grounded AI Responses
        ↓
Source Citations
        ↓
Simple & Extensible Architecture
```

The goal is to generate answers grounded in the uploaded document instead of relying solely on the language model.

---

# Lessons Learned

Building this project provided experience with:

- Retrieval-Augmented Generation (RAG).
- Vector databases with pgvector.
- Embedding generation pipelines.
- Spring AI integration.
- PDF parsing using Apache PDFBox.
- Prompt construction using retrieved context.
- Citation-aware AI responses.
- Full-stack AI application architecture.

---

# Future Improvements

- Multi-document search within a workspace.
- OCR support for scanned PDFs.
- Hybrid keyword + vector search.
- Response reranking.
- Persistent chat memory using JDBC.
- Document summarization mode.
- Docker deployment.
- Cloud object storage for PDFs.
```