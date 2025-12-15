package com.traceurl.traceurl.domain.user.dto.response;

import com.traceurl.traceurl.common.base.BaseDto;
import com.traceurl.traceurl.common.enums.UserStatus;
import com.traceurl.traceurl.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@SuperBuilder
public class UserResponseDto extends BaseDto {

    private UUID id;
    private String email;
    private String displayName;
    private UserStatus status;
    private OffsetDateTime lastLoginAt;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
