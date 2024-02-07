package com.skklub.admin.service;

import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import com.skklub.admin.exception.deprecated.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.ExtraFileRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.dto.FileNames;
import com.skklub.admin.service.dto.NoticeDeletionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final ExtraFileRepository extraFileRepository;
    private final UserRepository userRepository;

    public Long createNotice(String title, String content, String userName, Thumbnail thumbnail, List<ExtraFile> extraFiles) {
        User user = userRepository.findByUsername(userName);
        Notice notice = new Notice(title, content, user, thumbnail);
        noticeRepository.save(notice);
        notice.appendExtraFiles(extraFiles);
        extraFileRepository.saveAll(extraFiles);
        return notice.getId();
    }


    public int appendExtraFiles(Notice notice, List<ExtraFile> extraFiles) {
        notice.appendExtraFiles(extraFiles);
        extraFileRepository.saveAll(extraFiles);
        return extraFiles.size();
    }

    public Optional<String> updateNotice(Long noticeId, Notice updateInfo) {
        return noticeRepository.findById(noticeId)
                .map(
                        notice -> {
                            notice.update(updateInfo);
                            return notice.getTitle();
                        }
                );
    }

    public Optional<FileNames> updateThumbnail(Long noticeId, Thumbnail changeInfo) {
        return noticeRepository.findById(noticeId)
                .map(
                        notice -> {
                            Thumbnail thumbnail = notice.getThumbnail();
                            FileNames fileNames = new FileNames(thumbnail);
                            thumbnail.update(changeInfo);
                            return fileNames;
                        }
                );
    }

    public Optional<NoticeDeletionDto> deleteNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .map(
                        notice -> {
                            NoticeDeletionDto noticeDeletionDto = NoticeDeletionDto.builder()
                                    .noticeTitle(notice.getTitle())
                                    .thumbnailFileName(new FileNames(notice.getThumbnail()))
                                    .extraFileNames(notice.getExtraFiles()
                                            .stream()
                                            .map(FileNames::new)
                                            .collect(Collectors.toList())
                                    )
                                    .build();
                            noticeRepository.delete(notice);
                            return noticeDeletionDto;
                        }
                );
    }

    public Optional<FileNames> deleteExtraFile(Long noticeId, String fileName) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(NoticeIdMisMatchException::new);
        return extraFileRepository.findByOriginalNameAndNotice(fileName, notice)
                .map(
                        extraFile -> {
                            extraFileRepository.delete(extraFile);
                            return new FileNames(extraFile);
                        }
                );
    }

    public Optional<Notice> findPreNotice(Notice notice) {
        return noticeRepository.findPreByCreatedAt(notice.getCreatedAt());
    }

    public Optional<Notice> findPostNotice(Notice notice) {
        return noticeRepository.findPostByCreatedAt(notice.getCreatedAt());
    }
}
