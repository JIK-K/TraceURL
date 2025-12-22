package com.traceurl.traceurl.core.analytics.entity;

import com.traceurl.traceurl.common.base.BaseEntity;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ip_blocklists",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"owner_user_id", "short_url_id", "ip_hash"})
        },
        indexes = {
            @Index(name = "idx_blocklist_ip_url", columnList = "ip_hash, short_url_id")
        }
)
public class IpBlocklist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id")
    private ShortUrl shortUrl;

    @Column(name = "ip_hash", nullable = false, columnDefinition = "CHAR(64)")
    private String ipHash;

    @Column(length = 200)
    private String reason;
}
