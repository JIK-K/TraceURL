package com.traceurl.traceurl.core.analytics.dto.response;

import com.traceurl.traceurl.common.base.BaseDto;
import com.traceurl.traceurl.common.util.crypto.AesUtil;
import com.traceurl.traceurl.core.analytics.entity.IpBlocklist;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@SuperBuilder
public class IpBlocklistResponseDto extends BaseDto {
    private UUID id;
    private String ipAddress;
    private String reason;
    private String ipHash;

    public static IpBlocklistResponseDto from(IpBlocklist entity, AesUtil aesUtil){
        return IpBlocklistResponseDto.builder()
                .id(entity.getId())
                .ipAddress(aesUtil.decrypt(entity.getEncrypted_ip()))
                .reason(entity.getReason())
                .ipHash(entity.getIpHash())
                .createdAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }
}
