package com.danji.diary.repository;

import com.danji.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByUserIdOrderByDiaryDateDescDiaryTimeDesc(Long userId);
    Optional<Diary> findByIdAndUserId(Long id, Long userId);
}
