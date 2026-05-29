package com.danji.diary.controller;

import com.danji.diary.dto.MeResponse;
import com.danji.diary.dto.PinRequest;
import com.danji.diary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public MeResponse me(@AuthenticationPrincipal Long userId) {
        return MeResponse.of(userService.getById(userId));
    }

    @PostMapping("/pin")
    public ResponseEntity<Void> setPin(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody PinRequest req) {
        userService.setPin(userId, req.pin());
        return ResponseEntity.noContent().build();
    }
}
