package com.danji.diary.dto;

import jakarta.validation.constraints.NotBlank;

public record UnlockRequest(
        @NotBlank String pin,
        /** true 이면 검증 성공 시 잠금을 영구 해제 */
        boolean removeLock
) {}
