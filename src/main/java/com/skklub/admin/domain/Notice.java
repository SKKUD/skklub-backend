package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    private String title;
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "thumbnail_id")
    private Thumbnail thumbnail;

    @OneToMany(mappedBy = "notice", orphanRemoval = true)
    private List<ExtraFile> extraFiles = new ArrayList<>();

    public Notice(String title, String content, User writer, Thumbnail thumbnail) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.thumbnail = thumbnail;
    }

    public void appendExtraFiles(List<ExtraFile> additionalExtraFiles) {
        for (ExtraFile additionalExtraFile : additionalExtraFiles) {
            additionalExtraFile.matchToNotice(this);
            extraFiles.add(additionalExtraFile);
        }
    }

    public void update(Notice updateInfo) {
        this.title = updateInfo.getTitle();
        this.content = updateInfo.getContent();
    }
}
