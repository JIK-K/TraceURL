package com.traceurl.traceurl.core.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "click_stats_breakdown",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"short_url_id", "stat_date", "dimension", "dimension_value"})
        }
)
public class ClickStatsBreakdown {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url_id", nullable = false)
    private UUID shortUrlId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(length = 30, nullable = false)
    private String dimension; // COUNTRY, DEVICE, BROWSER, HOUR

    @Column(name = "dimension_value", length = 64, nullable = false)
    private String dimensionValue;

    @Column(nullable = false)
    private Long pv;

    @Column(nullable = true)
    private Long uv;
}