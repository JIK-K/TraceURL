package com.traceurl.traceurl.core.shorturl.scheduler;

import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrlLifecycle;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlLifecycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShortUrlExpireScheduler {

    private final ShortUrlLifecycleRepository lifecycleRepository;

    @Transactional
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행 (주기는 조절 가능)
    public void processExpiredUrls() {
        Instant now = Instant.now();
        List<ShortUrlLifecycle> expiredLifecycles = lifecycleRepository.findExpiredLifecycles(now);

        if (expiredLifecycles.isEmpty()) return;

        log.info("만료된 단축 URL {}건을 발견하여 INACTIVE로 전환합니다.", expiredLifecycles.size());

        for (ShortUrlLifecycle lifecycle : expiredLifecycles) {
            lifecycle.getShortUrl().setStatus(BaseStatus.INACTIVE);
        }

        // Transactional에 의해 변경 감지(Dirty Checking)로 자동 업데이트됩니다.
    }
}