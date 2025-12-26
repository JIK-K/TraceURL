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
@Table(name = "click_stats_daily",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"short_url_id", "stat_date"})
        },
        indexes = {
                @Index(name = "idx_stats_date_url", columnList = "stat_date, short_url_id")
        }
)
public class ClickStatsDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url_id", nullable = false)
    private UUID shortUrlId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(nullable = false)
    private Long pv; // Page View

    @Column(nullable = false)
    private Long uv; // Unique Visitor

    @Column(name = "country_top", length = 64)
    private String countryTop;
}