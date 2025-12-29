package com.traceurl.traceurl.core.user.dto.response;

import com.traceurl.traceurl.common.base.BaseDto;
import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.user.entity.User;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@SuperBuilder
public class UserResponseDto extends BaseDto {

    private UUID id;
    private String email;
    private String displayName;
    private String type;
    private BaseStatus status;
    private OffsetDateTime lastLoginAt;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .type(user.getType())
                .lastLoginAt(
                        user.getLastLoginAt() == null
                                ? null
                                : user.getLastLoginAt().atOffset(ZoneOffset.UTC)
                )
                .createdAt(user.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(user.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }
}
