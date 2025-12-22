package com.traceurl.traceurl.core.shorturl.repository;

import com.traceurl.traceurl.core.shorturl.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
}
