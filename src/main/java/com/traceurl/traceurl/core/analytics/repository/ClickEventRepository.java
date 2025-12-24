package com.traceurl.traceurl.core.analytics.repository;

import com.traceurl.traceurl.core.analytics.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClickEventRepository extends JpaRepository<ClickEvent, UUID> {
}
