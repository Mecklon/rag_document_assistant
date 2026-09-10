package com.mecklon.backend.dtos;

import com.mecklon.backend.model.Citation;
import com.mecklon.backend.model.Workspace;
import com.mecklon.backend.model.types.MessageRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class MessageDTO {
    private UUID id;
    private String content;
    private MessageRole role;
    private LocalDateTime createdAt;
    private UUID workspaceId;
    private List<CitationDTO> citations = new ArrayList<>();
}
