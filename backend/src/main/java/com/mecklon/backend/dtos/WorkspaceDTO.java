package com.mecklon.backend.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkspaceDTO {
    private UUID id;
    public String name;
    public LocalDateTime createdAt;
    public LocalDateTime lastUsed;
    public List<DocumentDTO> document = new ArrayList<>();
    public List<MessageDTO> messages = new ArrayList<>();
}
