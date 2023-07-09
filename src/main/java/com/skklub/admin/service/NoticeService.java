package com.skklub.admin.service;

import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import com.skklub.admin.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.ExtraFileRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.repository.ThumbnailRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.dto.FileNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final ExtraFileRepository extraFileRepository;
    private final UserRepository userRepository;
    private final ThumbnailRepository thumbnailRepository;

    public Long createNotice(String title, String content, String userName, Thumbnail thumbnail) {
        User user = userRepository.findByUsername(userName);
        Notice notice = new Notice(title, content, user, thumbnail);
        noticeRepository.save(notice);
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

    public FileNames updateThumbnail(Long noticeId, Thumbnail changeInfo) {
        return noticeRepository.findById(noticeId)
                .map(
                        notice -> {
                            Thumbnail thumbnail = notice.getThumbnail();
                            FileNames fileNames = new FileNames(thumbnail);
                            thumbnail.update(changeInfo);
                            return fileNames;
                        }
                ).orElseThrow(NoticeIdMisMatchException::new);
    }

    public Optional<String> deleteNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .map(
                        notice -> {
                            noticeRepository.delete(notice);
                            return notice.getTitle();
                        }
                );
    }

    public Optional<FileNames> deleteExtraFile(String fileName) {
        return extraFileRepository.findByOriginalName(fileName)
                .map(
                        extraFile -> {
                            extraFileRepository.delete(extraFile);
                            return new FileNames(extraFile);
                        }
                );
    }
}
