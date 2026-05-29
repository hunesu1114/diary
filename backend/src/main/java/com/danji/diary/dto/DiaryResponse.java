package com.danji.diary.dto;

import com.danji.diary.domain.Diary;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record DiaryResponse(
        Long id,
        String title,
        String content,
        LocalDate diaryDate,
        LocalTime diaryTime,
        boolean locked,
        Instant createdAt,
        Instant updatedAt
) {
    /** 전체 내용 포함 */
    public static DiaryResponse of(Diary d) {
        return new DiaryResponse(d.getId(), d.getTitle(), d.getContent(),
                d.getDiaryDate(), d.getDiaryTime(), d.isLocked(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    /** 잠긴 일기: content 마스킹 (null) */
    public static DiaryResponse masked(Diary d) {
        return new DiaryResponse(d.getId(), d.getTitle(), null,
                d.getDiaryDate(), d.getDiaryTime(), d.isLocked(),
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
