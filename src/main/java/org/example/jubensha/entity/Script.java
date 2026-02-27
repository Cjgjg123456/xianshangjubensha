package org.example.jubensha.entity;
import lombok.Data;

@Data
public class Script {
    private Integer scriptId;
    private String title;
    private String intro;
    private Integer playerCount;
    private String difficulty;
    private String coverUrl;
    private String tags;

    // ======== 新增字段 ========
    private String truthContent; // 剧本的完整真相/复盘内容
}