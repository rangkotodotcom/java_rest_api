package com.rangkoto.rest_api.modules.user.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
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
    private String name;
    private String username;
    private String email;
    private String password;

    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(columnDefinition = "jsonb")
    @Column(columnDefinition = "json")
    private List<String> roles;

    // Audit
    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private Instant updatedAt;

    private String deletedBy;
    private Instant deletedAt;

    // Soft delete helper
    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete(String user) {
        this.deletedBy = user;
        this.deletedAt = Instant.now();
    }
}
