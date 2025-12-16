package com.traceurl.traceurl.core.domain.entity;

import com.traceurl.traceurl.common.base.BaseEntity;
import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "domains")
public class Domain extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false, unique = true)
    private String domain;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BaseStatus status = BaseStatus.ACTIVE;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = BaseStatus.ACTIVE;
        }
        if (isPrimary == null) {
            isPrimary = false;
        }
    }
}
