package com.traceurl.traceurl.core.user.entity;

import com.traceurl.traceurl.common.base.BaseEntity;
import com.traceurl.traceurl.common.enums.BaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "users")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, name = "display_name")
    private String displayName;

    @Column(nullable = true, name = "type")
    private String type;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private BaseStatus status = BaseStatus.ACTIVE;

    @Column(name = "last_login_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastLoginAt;

    @PrePersist
    public void prePersist() {
        if (this.lastLoginAt == null) {
            this.lastLoginAt = OffsetDateTime.now();
        }
        if (this.status == null){
            this.status = BaseStatus.ACTIVE;
        }
    }
}
