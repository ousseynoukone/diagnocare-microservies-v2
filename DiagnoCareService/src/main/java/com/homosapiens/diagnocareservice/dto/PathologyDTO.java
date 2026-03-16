package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

@Data
public class PathologyDTO {
    private Long id;
    private String pathologyName;
    private String description;
}
