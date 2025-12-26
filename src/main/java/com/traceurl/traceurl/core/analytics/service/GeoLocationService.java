package com.traceurl.traceurl.core.analytics.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.traceurl.traceurl.core.analytics.dto.common.GeoLocationDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.InetAddress;

@Slf4j
@Service
public class GeoLocationService {

    private DatabaseReader dbReader;

    @PostConstruct
    public void init() {
        try (InputStream is = new ClassPathResource("geoip/GeoLite2-City.mmdb").getInputStream()) {
            this.dbReader = new DatabaseReader.Builder(is).build();
            log.info("GeoIP Database loaded successfully.");
        } catch (Exception e) {
            log.error("GeoIP Database load failed: {}", e.getMessage());
        }
    }

    public GeoLocationDto getLocation(String ipAddress) {
        if (dbReader == null || ipAddress == null) {
            return GeoLocationDto.unknown();
        }

        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);

            String isoCode = response.getCountry().getIsoCode();

            return GeoLocationDto.builder()
                    .country(response.getCountry().getName())
                    .countryCode(isoCode != null ? isoCode.toLowerCase() : "unknown")
                    .region(response.getMostSpecificSubdivision().getName())
                    .city(response.getCity().getName())
                    .build();
        } catch (Exception e) {
            // 로컬 IP(127.0.0.1)나 분석 불가능한 IP일 경우
            return GeoLocationDto.unknown();
        }
    }
}