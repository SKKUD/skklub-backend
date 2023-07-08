package com.skklub.admin.service;

import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import com.skklub.admin.repository.ExtraFileRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final ExtraFileRepository extraFileRepository;
    private final UserRepository userRepository;

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
}
