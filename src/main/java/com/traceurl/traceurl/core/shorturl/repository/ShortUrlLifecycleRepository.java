package com.traceurl.traceurl.core.shorturl.repository;

import com.traceurl.traceurl.core.shorturl.entity.ShortUrlLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShortUrlLifecycleRepository extends JpaRepository<ShortUrlLifecycle, UUID> {
    Optional<ShortUrlLifecycle> findByShortUrlId(UUID shortUrlId);
}
