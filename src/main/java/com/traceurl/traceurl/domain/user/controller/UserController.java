package com.traceurl.traceurl.domain.user.controller;

import com.traceurl.traceurl.common.dto.ResponseDto;
import com.traceurl.traceurl.domain.user.service.UserService;
import com.traceurl.traceurl.domain.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseDto<UserResponseDto> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        ResponseDto<UserResponseDto> response = new ResponseDto<>();
        response.setSuccess(userService.getMe(userId));

        return response;
    }
}
