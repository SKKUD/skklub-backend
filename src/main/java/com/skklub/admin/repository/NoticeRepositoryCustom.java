package com.skklub.admin.repository;

import com.skklub.admin.repository.dto.NoticeDetailDto;

import java.util.Optional;

public interface NoticeRepositoryCustom {
    public Optional<NoticeDetailDto> findDetailById(Long id);
}
