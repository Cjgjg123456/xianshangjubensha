package org.example.jubensha.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScriptClue {
    private Long clueId;
    private Long scriptId;
    private String locationName;
    private String clueName;
    private String clueDesc;
    private String clueImageUrl;
    private Integer isPublic;

    // ======== 新增字段 ========
    private Integer roleId; // 所属角色ID（如果是私密线索则填角色ID）

    private Long unlockChapterId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}