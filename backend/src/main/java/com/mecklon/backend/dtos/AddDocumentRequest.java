package com.mecklon.backend.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AddDocumentRequest {
    public UUID workspaceId;
}
