package com.mecklon.backend.model;

import com.mecklon.backend.model.types.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String referencePath;

    @Enumerated(EnumType.STRING)
    private DocumentType type;

    @ManyToOne
    private Workspace workspace;
}