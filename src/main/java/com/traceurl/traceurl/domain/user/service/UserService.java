package com.traceurl.traceurl.domain.user.service;

import com.traceurl.traceurl.common.constant.JwtError;
import com.traceurl.traceurl.common.constant.UserError;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.domain.user.entity.User;
import com.traceurl.traceurl.domain.user.repository.UserRepository;
import com.traceurl.traceurl.domain.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserResponseDto getMe(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserError.NO_USER));

        return UserResponseDto.from(user);
    }
}
