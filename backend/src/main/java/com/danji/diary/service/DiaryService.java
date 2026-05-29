package com.danji.diary.service;

import com.danji.diary.domain.Diary;
import com.danji.diary.domain.User;
import com.danji.diary.dto.DiaryRequest;
import com.danji.diary.dto.DiaryResponse;
import com.danji.diary.repository.DiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserService userService;

    public DiaryService(DiaryRepository diaryRepository, UserService userService) {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
    }

    /** 목록: 잠긴 일기는 content 마스킹 */
    @Transactional(readOnly = true)
    public List<DiaryResponse> list(Long userId) {
        return diaryRepository.findByUserIdOrderByDiaryDateDescDiaryTimeDesc(userId).stream()
                .map(d -> d.isLocked() ? DiaryResponse.masked(d) : DiaryResponse.of(d))
                .toList();
    }

    /** 단건: 잠긴 일기는 content 마스킹 */
    @Transactional(readOnly = true)
    public DiaryResponse get(Long userId, Long id) {
        Diary d = findOwned(userId, id);
        return d.isLocked() ? DiaryResponse.masked(d) : DiaryResponse.of(d);
    }

    @Transactional
    public DiaryResponse create(Long userId, DiaryRequest req) {
        User user = userService.getById(userId);
        Diary diary = Diary.builder()
                .user(user)
                .title(req.title())
                .content(req.content())
                .diaryDate(req.diaryDate())
                .diaryTime(req.diaryTime())
                .locked(false)
                .build();
        return DiaryResponse.of(diaryRepository.save(diary));
    }

    @Transactional
    public DiaryResponse update(Long userId, Long id, DiaryRequest req) {
        Diary d = findOwned(userId, id);
        d.setTitle(req.title());
        d.setContent(req.content());
        d.setDiaryDate(req.diaryDate());
        d.setDiaryTime(req.diaryTime());
        return DiaryResponse.of(diaryRepository.save(d));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Diary d = findOwned(userId, id);
        diaryRepository.delete(d);
    }

    /** 잠금 설정. PIN 미설정 시 거부 */
    @Transactional
    public void lock(Long userId, Long id) {
        if (!userService.getById(userId).hasPin()) {
            throw new ResponseStatusException(CONFLICT, "먼저 잠금 PIN을 설정하세요.");
        }
        Diary d = findOwned(userId, id);
        d.setLocked(true);
        diaryRepository.save(d);
    }

    /**
     * PIN 검증 후 전체 내용 열람(view-only). 잠금 상태는 유지한다.
     * @param removeLock true 이면 검증 성공 시 잠금을 영구 해제한다.
     */
    @Transactional
    public DiaryResponse unlock(Long userId, Long id, String pin, boolean removeLock) {
        if (!userService.verifyPin(userId, pin)) {
            throw new ResponseStatusException(UNAUTHORIZED, "PIN이 올바르지 않습니다.");
        }
        Diary d = findOwned(userId, id);
        if (removeLock) {
            d.setLocked(false);
            diaryRepository.save(d);
        }
        return DiaryResponse.of(d);
    }

    private Diary findOwned(Long userId, Long id) {
        return diaryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "일기를 찾을 수 없습니다."));
    }
}
