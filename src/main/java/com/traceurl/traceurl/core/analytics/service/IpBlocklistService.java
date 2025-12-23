package com.traceurl.traceurl.core.analytics.service;

import com.traceurl.traceurl.common.constant.CommonError;
import com.traceurl.traceurl.common.constant.UserError;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.common.util.crypto.AesUtil;
import com.traceurl.traceurl.core.analytics.dto.request.IpBlocklistCreateRequestDto;
import com.traceurl.traceurl.core.analytics.dto.response.IpBlocklistResponseDto;
import com.traceurl.traceurl.core.analytics.entity.IpBlocklist;
import com.traceurl.traceurl.core.analytics.repository.IpBlocklistRepository;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlRepository;
import com.traceurl.traceurl.core.user.entity.User;
import com.traceurl.traceurl.core.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class IpBlocklistService {
    private final IpBlocklistRepository ipBlocklistRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final AesUtil aesUtil;

    @Transactional
    public IpBlocklistResponseDto blockIp(IpBlocklistCreateRequestDto dto, UUID userId) {
        String shortCode = dto.getShortCode();
        String ipAddress = dto.getIpAddress();
        String reason = dto.getReason();

        // 1. 빠른 조회를 위한 해싱 (SHA-256)
        String ipHash = hashIp(ipAddress);

        // 2. ShortUrl 존재 확인
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl == null) {
            throw new BusinessException(CommonError.ENTITY_NOT_FOUND);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserError.NO_USER));

        // 3. 중복 차단 확인 (해시로 비교)
        if (ipBlocklistRepository.existsByShortUrlIdAndIpHash(shortUrl.getId(), ipHash)) {
            throw new BusinessException(CommonError.DUPLICATE_ERROR);
        }

        // 4. 나중에 복호화하기 위한 암호화 (AES)
        String encryptedIp = aesUtil.encrypt(ipAddress);

        // 5. 저장
        IpBlocklist blocklist = IpBlocklist.builder()
                .shortUrl(shortUrl)
                .ownerUser(user)
                .ipHash(ipHash)
                .encrypted_ip(encryptedIp)
                .reason(reason)
                .build();

        ipBlocklistRepository.save(blocklist);
        return IpBlocklistResponseDto.from(blocklist, aesUtil);
    }

    public List<IpBlocklistResponseDto> getMyBlockedIps(
            UUID userId,
            Pageable pageable, // 필요 없다면 제거해도 되지만, 정렬(Sort)을 위해 남겨둘 수 있습니다.
            String shortCode
    ) {
        // 1. DB에서 엔티티 조회
        Page<IpBlocklist> blocklistPage = ipBlocklistRepository
                .findByOwnerUserIdAndShortUrl_ShortCode(userId, shortCode, pageable);

        // 2. DTO 변환 후 리스트로 변환 (.getContent() 사용)
        return blocklistPage.getContent().stream()
                .map(item -> IpBlocklistResponseDto.from(item, aesUtil))
                .toList();
    }

    @Transactional
    public void unblockIp(UUID blocklistId, UUID userId) {
        // 삭제 전 해당 리소스가 본인 것인지 확인하는 로직을 추가하면 더 안전합니다.
        ipBlocklistRepository.deleteByIdAndOwnerUserId(blocklistId, userId);
    }


    // 차단 여부 확인 (리다이렉트 시 호출됨)
    public boolean isBlocked(UUID shortUrlId, String ipAddress) {
        return ipBlocklistRepository.existsByShortUrlIdAndIpHash(shortUrlId, hashIp(ipAddress));
    }

    // IP 해싱 (SHA-256)
    private String hashIp(String ipAddress) {
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

}
