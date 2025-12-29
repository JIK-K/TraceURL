package com.traceurl.traceurl.core.shorturl.repository;

import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

    @Query("SELECT s FROM ShortUrl s WHERE s.shortCode = :shortCode AND s.status = 'ACTIVE'")
    ShortUrl findActiveByShortCode(@Param("shortCode") String shortCode);

    Optional<ShortUrl> findByShortCodeAndOwnerUserId(
            String shortCode,
            UUID OwnerUserId
    );

    @Modifying
    @Query("UPDATE ShortUrl s SET s.status = 'DELETED', s.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE s.ownerUser = :owner AND s.status != 'DELETED'")
    void deleteAllByOwner(@Param("owner") User owner);

}



