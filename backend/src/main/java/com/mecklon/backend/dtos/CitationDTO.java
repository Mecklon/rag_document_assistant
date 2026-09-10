package com.mecklon.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitationDTO {
    public UUID id;
    public int pageNumber;
    public String text;
    public UUID documentId;
}
