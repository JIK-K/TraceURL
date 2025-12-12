package com.traceurl.traceurl.domain.user;

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

    public User createUser(User user) {
        logger.info("=== CREATE USER START ===");
        logger.info("Before save: {}", user); // 저장 전 엔티티 상태

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User savedUser = userRepository.save(user); // DB에 저장
        logger.info("After save: {}", savedUser); // 저장 후 엔티티 상태, UUID 포함
        logger.info("=== CREATE USER END ===");

        return savedUser;
    }

    public User getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        logger.info("Get user: {}", user);
        return user;
    }
}
