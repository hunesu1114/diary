package com.danji.diary.controller;

import com.danji.diary.dto.DiaryRequest;
import com.danji.diary.dto.DiaryResponse;
import com.danji.diary.dto.UnlockRequest;
import com.danji.diary.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @GetMapping
    public List<DiaryResponse> list(@AuthenticationPrincipal Long userId) {
        return diaryService.list(userId);
    }

    @GetMapping("/{id}")
    public DiaryResponse get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return diaryService.get(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryResponse create(@AuthenticationPrincipal Long userId,
                                @Valid @RequestBody DiaryRequest req) {
        return diaryService.create(userId, req);
    }

    @PutMapping("/{id}")
    public DiaryResponse update(@AuthenticationPrincipal Long userId,
                                @PathVariable Long id,
                                @Valid @RequestBody DiaryRequest req) {
        return diaryService.update(userId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        diaryService.delete(userId, id);
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<Void> lock(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        diaryService.lock(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unlock")
    public DiaryResponse unlock(@AuthenticationPrincipal Long userId,
                                @PathVariable Long id,
                                @Valid @RequestBody UnlockRequest req) {
        return diaryService.unlock(userId, id, req.pin(), req.removeLock());
    }
}
