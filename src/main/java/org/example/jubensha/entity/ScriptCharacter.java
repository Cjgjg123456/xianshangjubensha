package org.example.jubensha.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScriptCharacter {
    private Long characterId;
    private Long scriptId;
    private String characterName;
    private String avatarUrl;
    private Integer gender;
    private String intro;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}