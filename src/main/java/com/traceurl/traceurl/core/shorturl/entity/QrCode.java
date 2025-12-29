package com.traceurl.traceurl.core.shorturl.entity;

import com.traceurl.traceurl.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qr_codes")
public class QrCode extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", unique = true, nullable = false)
    private ShortUrl shortUrl;

    @Column(nullable = false, length = 20)
    private String format;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "file_path", length = 512)
    private String filePath;
}
