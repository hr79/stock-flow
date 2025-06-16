package com.example.stockflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class BaseEntity {
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = true)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = ZonedDateTime
                .now(ZoneId.of("Asia/Seoul"));
    }
}
