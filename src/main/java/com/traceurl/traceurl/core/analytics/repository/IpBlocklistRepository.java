package com.traceurl.traceurl.core.analytics.repository;

import com.traceurl.traceurl.core.analytics.entity.IpBlocklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IpBlocklistRepository extends JpaRepository<IpBlocklist, UUID> {
    boolean existsByShortUrlIdAndIpHash(UUID shortUrlId, String ipHash);
    void deleteByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    Page<IpBlocklist> findByOwnerUserIdAndShortUrl_ShortCode(
            UUID ownerUserId,
            String shortCode,
            Pageable pageable
    );
}

