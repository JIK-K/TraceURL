package com.traceurl.traceurl.common.util.ip;

import com.traceurl.traceurl.common.constant.CommonError;
import com.traceurl.traceurl.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class IpUtils {
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 여러 IP가 넘어올 경우 (콤마로 구분됨), 첫 번째 IP가 실제 클라이언트 IP입니다.
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    // IP 해싱 (SHA-256)
    public static String hashIp(String ipAddress) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new BusinessException(CommonError.INTERNAL_SERVER_ERROR);
        }
    }

    public static String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) return "0.0.0.0";

        // IPv4 기준으로 마지막 두 마디를 마스킹 (예: 123.123.123.123 -> 123.123.***.***)
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }

        // IPv6 등의 경우 앞부분만 일부 노출하고 마스킹 처리
        return ip.length() > 8 ? ip.substring(0, 8) + "...." : "****";
    }
}