package com.traceurl.traceurl.core.shorturl.entity;

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
@Table(name = "short_urls", uniqueConstraints = {
        @UniqueConstraint(columnNames = "short_code")
})
public class ShortUrl extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    @Column(name = "short_code", nullable = false, length = 64)
    private String shortCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "is_custom")
    private Boolean isCustom;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaseStatus status = BaseStatus.ACTIVE;

}
