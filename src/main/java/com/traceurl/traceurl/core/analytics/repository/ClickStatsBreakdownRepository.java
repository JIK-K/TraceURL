package com.traceurl.traceurl.core.analytics.repository;

import com.traceurl.traceurl.core.analytics.entity.ClickStatsBreakdown;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ClickStatsBreakdownRepository extends JpaRepository<ClickStatsBreakdown, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO click_stats_breakdown (short_url_id, stat_date, dimension, dimension_value, pv, uv)
        VALUES (:shortUrlId, :statDate, :dimension, :value, 1, :uvAdd)
        ON CONFLICT (short_url_id, stat_date, dimension, dimension_value)
        DO UPDATE SET 
            pv = click_stats_breakdown.pv + 1,
            uv = click_stats_breakdown.uv + :uvAdd
        """, nativeQuery = true)
    void upsertBreakdown(@Param("shortUrlId") UUID shortUrlId,
                         @Param("statDate") LocalDate statDate,
                         @Param("dimension") String dimension,
                         @Param("value") String value,
                         @Param("uvAdd") int uvAdd);

    @Query("SELECT b FROM ClickStatsBreakdown b WHERE b.shortUrlId = :id AND b.statDate = :date " +
            "AND b.dimension = 'COUNTRY' ORDER BY b.pv DESC")
    List<ClickStatsBreakdown> findTopCountry(@Param("id") UUID id, @Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT b.dimensionValue as label, SUM(b.pv) as count " +
            "FROM ClickStatsBreakdown b " +
            "WHERE b.shortUrlId = :shortUrlId " +
            "AND b.dimension = :dimension " +
            "GROUP BY b.dimensionValue " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getTotalBreakdownStats(
            @Param("shortUrlId") UUID shortUrlId,
            @Param("dimension") String dimension
    );
}