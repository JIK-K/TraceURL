package com.traceurl.traceurl.core.shorturl.repository;

import com.traceurl.traceurl.core.shorturl.entity.ShortUrlLifecycle;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortUrlLifecycleRepository extends JpaRepository<ShortUrlLifecycle, UUID> {
    Optional<ShortUrlLifecycle> findByShortUrlId(UUID shortUrlId);

    @Query("SELECT l FROM ShortUrlLifecycle l " +
            "JOIN FETCH l.shortUrl s " +
            "WHERE l.expireAt <= :now " +
            "AND s.status = 'ACTIVE'")
    List<ShortUrlLifecycle> findExpiredLifecycles(@Param("now") Instant now);
}
