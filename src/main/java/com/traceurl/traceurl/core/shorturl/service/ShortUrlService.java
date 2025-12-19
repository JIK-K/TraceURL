package com.traceurl.traceurl.core.shorturl.service;

import com.traceurl.traceurl.common.constant.CommonError;
import com.traceurl.traceurl.common.constant.UserError;
import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.core.shorturl.dto.request.ShortUrlCreateRequestDto;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrlLifecycle;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlLifecycleRepository;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlRepository;
import com.traceurl.traceurl.core.user.entity.User;
import com.traceurl.traceurl.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.traceurl.traceurl.common.util.string.StringUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortUrlService {
    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlLifecycleRepository shortUrlLifecycleRepository;
    private final UserRepository userRepository;

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

        return true;
    }

    public Page<ShortUrl> getMyShortUrls(
            UUID userId,
            Pageable pageable,
            BaseStatus status
    ){
        if (status == null) {
            return shortUrlRepository.findByOwnerUserId(userId, pageable);
        }
        return shortUrlRepository.findByOwnerUserIdAndStatus(
                userId,
                status,
                pageable
        );
    }

    public String getOriginalUrlByShortUrl(String shortUrl) {
        ShortUrl entity = shortUrlRepository.findByShortCode(shortUrl);
        log.info(entity.getOriginalUrl());
        return entity != null ? entity.getOriginalUrl() : null;
    }
}
