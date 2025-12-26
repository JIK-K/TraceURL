package com.traceurl.traceurl.core.analytics.repository;

import com.traceurl.traceurl.core.analytics.entity.ClickStatsDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClickStatsDailyRepository extends JpaRepository<ClickStatsDaily, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO click_stats_daily (short_url_id, stat_date, pv, uv)
        VALUES (:shortUrlId, :statDate, 1, :uvAdd)
        ON CONFLICT (short_url_id, stat_date)
        DO UPDATE SET 
            pv = click_stats_daily.pv + 1,
            uv = click_stats_daily.uv + :uvAdd
        """, nativeQuery = true)
    void upsertDailyStats(@Param("shortUrlId") UUID shortUrlId,
                          @Param("statDate") LocalDate statDate,
                          @Param("uvAdd") int uvAdd);

    List<ClickStatsDaily> findByShortUrlIdAndStatDateGreaterThanEqualOrderByStatDateDesc(
            UUID shortUrlId, LocalDate startDate
    );
}