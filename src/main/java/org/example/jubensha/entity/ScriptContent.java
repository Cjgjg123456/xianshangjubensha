package org.example.jubensha.entity;

import lombok.Data;

@Data
public class ScriptContent {
    private Long contentId;
    private Long chapterId;
    private Long characterId;
    private String contentText;
    private Integer isDeleted;
}