package com.traceurl.traceurl.core.user.service;

import com.traceurl.traceurl.common.constant.UserError;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.core.user.dto.requset.UserUpdateNameRequestDto;
import com.traceurl.traceurl.core.user.entity.User;
import com.traceurl.traceurl.core.user.repository.UserRepository;
import com.traceurl.traceurl.core.user.dto.response.UserResponseDto;
import jakarta.transaction.Transactional;
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
        logger.info(userId.toString());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserError.NO_USER));

        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto updateUserName(UUID userId, UserUpdateNameRequestDto requestDto){
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserError.NO_USER));

        user.setDisplayName(requestDto.getDisplayName());

        userRepository.save(user);

        return UserResponseDto.from(user);
    }
}
