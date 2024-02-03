package com.skklub.admin.domain.imagefile;

import com.skklub.admin.domain.FileName;
import com.skklub.admin.domain.Notice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(exclude = {"id", "notice"}, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtraFile extends FileName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "extra_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    public ExtraFile(String originalName, String uploadedName) {
        super(originalName, uploadedName);
    }

    public void matchToNotice(Notice notice) {
        this.notice = notice;
    }
}
