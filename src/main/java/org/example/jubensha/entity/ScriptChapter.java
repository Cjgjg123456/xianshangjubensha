package org.example.jubensha.entity;

import lombok.Data;

@Data
public class ScriptChapter {
    private Long chapterId;
    private Long scriptId;
    private Integer chapterRound;
    private String chapterName;
    private Integer isDeleted;
}