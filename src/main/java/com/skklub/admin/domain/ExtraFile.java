package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Not;

@Entity
@Getter
@EqualsAndHashCode(exclude = {"id", "notice"}, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtraFile extends BaseTimeEntity{
    @Id @Column(name = "extra_file_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String savedName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    public ExtraFile(String originalName, String savedName) {
        this.originalName = originalName;
        this.savedName = savedName;
    }

    public void matchToNotice(Notice notice) {
        this.notice = notice;
    }
}
