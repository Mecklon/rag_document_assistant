package com.mecklon.backend.dtos;

import com.mecklon.backend.model.Workspace;
import com.mecklon.backend.model.types.DocumentType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    public UUID id;
    public String name;
    public String referencePath;
    public DocumentType type;
}

