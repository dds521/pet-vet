package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("symptoms")
public class Symptom {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String description;

    private Long petId;

    private LocalDateTime reportedAt;

    public Symptom(String description, Long petId) {
        this.description = description;
        this.petId = petId;
        this.reportedAt = LocalDateTime.now();
    }
}
