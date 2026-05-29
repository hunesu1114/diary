package com.danji.diary.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kakaoId;

    private String nickname;

    private String email;

    /** 계정 공통 잠금 PIN의 BCrypt 해시. 미설정 시 null */
    @Column(name = "lock_pin")
    private String lockPin;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean hasPin() {
        return lockPin != null && !lockPin.isBlank();
    }
}
