package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtraFile {
    @Id @Column(name = "extra_file_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String src;
    private String name;
}
