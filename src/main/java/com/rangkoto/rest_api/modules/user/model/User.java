package com.rangkoto.rest_api.modules.user.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    private String username;
    private String email;
    private String password;

    // Audit
    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String deletedBy;
    private LocalDateTime deletedAt;

    // Soft delete helper
    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete(String user) {
        this.deletedBy = user;
        this.deletedAt = LocalDateTime.now();
    }
}
