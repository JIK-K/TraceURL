package com.traceurl.traceurl.core.shorturl.repository;

import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, UUID> {
    boolean existsByShortCode(String shortUrl);

    Page<ShortUrl> findByOwnerUserIdAndStatusNot(
            UUID ownerUserId,
            BaseStatus status,
            Pageable pageable
    );
    Page<ShortUrl> findByOwnerUserIdAndStatus(
            UUID ownerUserId,
            BaseStatus status,
            Pageable pageable
    );
    ShortUrl findByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCodeAndOwnerUserId(
            String shortCode,
            UUID OwnerUserId
    );
}



