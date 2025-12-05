package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@TableName("pets")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String breed;

    private Integer age;

    @TableField(exist = false)
    private List<Symptom> symptoms;

    private LocalDateTime createdAt;

    public Pet(String name, String breed, Integer age) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.createdAt = LocalDateTime.now();
    }
}
