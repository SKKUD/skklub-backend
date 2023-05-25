package com.skklub.admin.controller;

import com.skklub.admin.domain.enums.Campus;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Attributes;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.snippet.Attributes.key;

public interface ApiDocumentUtils {
    static OperationRequestPreprocessor getDocumentRequest() {
        return preprocessRequest(
                modifyUris() // (1)
                        .scheme("https")
                        .host("docs.api.com")
                        .removePort(),
                prettyPrint()); // (2)
    }

    static OperationResponsePreprocessor getDocumentResponse() {
        return preprocessResponse(prettyPrint()); // (3)
    }

    static Attributes.Attribute getCampusFormat() {
        return key("format").value("명륜 | 율전");
    }
    static Attributes.Attribute getClubTypeFormat() {
        return key("format").value("동아리연합회 | 중앙동아리 | 기타동아리 | 소모임 | 학회 | 학생단체 | 전체");
    }
    static Attributes.Attribute getBelongsFormat() {
        return key("format").value("명륜 : 평면예술 | 연행예술 | 봉사 | 취미교양 | 스포츠 | 종교분과 | 학술분과 | 인문사회\n" +
                "율전 : 연행예술 | 평면예술 | 과학기술 | 취미교양 | 사회 | 종교 | 학술 | 건강체육");
    }
}
