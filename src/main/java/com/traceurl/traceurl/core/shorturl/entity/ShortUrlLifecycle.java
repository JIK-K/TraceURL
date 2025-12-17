package com.traceurl.traceurl.core.shorturl.entity;

import com.traceurl.traceurl.common.base.BaseEntity;
import com.traceurl.traceurl.common.enums.ExpireType;
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
@Table(name = "short_url_lifecycle",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "short_url_id")
        })
public class ShortUrlLifecycle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "expire_type", nullable = false)
    private ExpireType expireType = ExpireType.NONE; // NONE | HOURS | DAYS | DATE

    @Column(name = "expire_value")
    private Integer expireValue; // 24 / 7 / 30 등

    @Column(name = "expire_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime expireAt;

    @Column(name = "auto_delete")
    private Boolean autoDelete;

    @PrePersist
    public void prePersist() {
        if (this.expireType == null) this.expireType = ExpireType.NONE;
    }

}
