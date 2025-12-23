package com.traceurl.traceurl.core.shorturl.service;

import com.traceurl.traceurl.common.constant.CommonError;
import com.traceurl.traceurl.common.constant.UserError;
import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.core.shorturl.dto.request.ShortUrlCreateRequestDto;
import com.traceurl.traceurl.core.shorturl.dto.request.ShortUrlEditRequestDto;
import com.traceurl.traceurl.core.shorturl.dto.response.ShortUrlEditResponseDto;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrlLifecycle;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlLifecycleRepository;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlRepository;
import com.traceurl.traceurl.core.user.entity.User;
import com.traceurl.traceurl.core.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.traceurl.traceurl.common.util.string.StringUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortUrlService {
    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlLifecycleRepository shortUrlLifecycleRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    public Boolean createShortUrl(ShortUrlCreateRequestDto dto, UUID userId){
        String shortCode = dto.getIsCustom() != null && dto.getIsCustom() && dto.getAlias() != null
                ? dto.getAlias()
                : generateRandomCode();


        if(shortUrlRepository.existsByShortCode(shortCode)){
            throw new BusinessException(CommonError.DUPLICATE_ERROR);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserError.NO_USER));


         ShortUrl shortUrl = ShortUrl.builder()
                 .ownerUser(user)
                 .shortCode(shortCode)
                 .originalUrl(dto.getOriginalUrl())
                 .title(dto.getTitle())
                 .isCustom(dto.getIsCustom())
                 .build();

         shortUrlRepository.save(shortUrl);

        ShortUrlLifecycle lifecycle = ShortUrlLifecycle.builder()
                .shortUrl(shortUrl)
                .expireType(parseExpireType(dto.getExpireDate()))
                .expireAt(parseExpireAt(dto.getExpireDate()))
                .autoDelete(true)
                .build();

        shortUrlLifecycleRepository.save(lifecycle);

        qrCodeService.generateAndSave(shortUrl);

        return true;
    }

    public Page<ShortUrl> getMyShortUrls(
            UUID userId,
            Pageable pageable,
            BaseStatus status
    ){
        if (status == null) {
            return shortUrlRepository.findByOwnerUserIdAndStatusNot(userId, BaseStatus.DELETED, pageable);
        }
        return shortUrlRepository.findByOwnerUserIdAndStatus(
                userId,
                status,
                pageable
        );
    }

    public String getOriginalUrlByShortUrl(String shortUrl) {
        ShortUrl entity = shortUrlRepository.findByShortCode(shortUrl);
        return entity != null ? entity.getOriginalUrl() : null;
    }

    public ShortUrlEditResponseDto getShortUrlEditData(
        String shortCode,
        UUID userId
    ){
        ShortUrl shortUrl = shortUrlRepository
                .findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        ShortUrlLifecycle lifecycle = shortUrlLifecycleRepository
                .findByShortUrlId(shortUrl.getId())
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        return ShortUrlEditResponseDto.from(shortUrl, lifecycle);
    }

    @Transactional
    public Boolean patchShortUrlEditData(
            String shortCode,
            UUID userId,
            ShortUrlEditRequestDto dto
    ){
        ShortUrl shortUrl = shortUrlRepository
                .findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        ShortUrlLifecycle lifecycle = shortUrlLifecycleRepository
                .findByShortUrlId(shortUrl.getId())
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        if (dto.getTitle() != null) {
            shortUrl.setTitle(dto.getTitle());
        }

        if (dto.getExpireDate() != null) {
            lifecycle.setExpireAt(parseExpireAt(dto.getExpireDate()));
            lifecycle.setAutoDelete(dto.getAutoDelete());
        }

        return true;
    }

    @Transactional
    public Boolean deleteShortUrl(String shortCode, UUID userId) {
        ShortUrl shortUrl = shortUrlRepository
                .findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        shortUrl.setStatus(BaseStatus.DELETED);
        shortUrl.setDeletedAt(Instant.now());
        return true;
    }
}
