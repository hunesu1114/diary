package com.danji.diary.service;

import com.danji.diary.domain.User;
import com.danji.diary.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    /** 계정 공통 PIN 설정/변경 */
    @Transactional
    public void setPin(Long userId, String rawPin) {
        User user = getById(userId);
        user.setLockPin(passwordEncoder.encode(rawPin));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean verifyPin(Long userId, String rawPin) {
        User user = getById(userId);
        return user.hasPin() && passwordEncoder.matches(rawPin, user.getLockPin());
    }
}
