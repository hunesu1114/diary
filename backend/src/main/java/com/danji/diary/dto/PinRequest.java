package com.danji.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PinRequest(
        @NotBlank @Size(min = 4, max = 32) String pin
) {}
