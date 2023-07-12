package com.skklub.admin.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skklub.admin.domain.QNotice;
import com.skklub.admin.repository.dto.NoticeDetailDto;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<NoticeDetailDto> findDetailById(Long id) {
        QNotice qnotice = QNotice.notice;
        jpaQueryFactory
                .selectFrom(qnotice)
                .where(qnotice.id.eq(id))
                .fetchOne();
        return Optional.empty();
    }
}
