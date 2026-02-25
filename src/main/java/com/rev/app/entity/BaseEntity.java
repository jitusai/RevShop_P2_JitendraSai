package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate()
    {
        this.createdAt = LocalDateTime.now();

    }

    @PreUpdate
    public void onUpdate()
    {
        this.updatedAt= LocalDateTime.now();
    }
}
