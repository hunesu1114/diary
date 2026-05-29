package com.danji.diary.dto;

import com.danji.diary.domain.User;

public record MeResponse(
        Long id,
        String nickname,
        String email,
        boolean hasPin
) {
    public static MeResponse of(User u) {
        return new MeResponse(u.getId(), u.getNickname(), u.getEmail(), u.hasPin());
    }
}
