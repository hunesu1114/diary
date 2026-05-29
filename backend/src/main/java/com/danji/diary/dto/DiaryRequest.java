package com.danji.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record DiaryRequest(
        @NotBlank String title,
        String content,
        @NotNull LocalDate diaryDate,
        @NotNull LocalTime diaryTime
) {}
