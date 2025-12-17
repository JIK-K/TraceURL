package com.traceurl.traceurl.core.user.dto.requset;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateNameRequestDto {
    @NotBlank(message = "Display name cannot be blank")
    private String displayName;
}
