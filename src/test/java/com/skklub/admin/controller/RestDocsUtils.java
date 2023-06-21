package com.skklub.admin.controller;

import akka.protobuf.WireFormat;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Attributes;

import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public interface RestDocsUtils {
    String LINK_CAMPUS_TYPE = "link:common/campus-type.html[캠퍼스 종류,role=\"popup\"]";
    String LINK_BELONGS_TYPE = "link:common/belongs.html[분과 종류,role=\"popup\"]";
    String LINK_BELONGS_TYPE_NULL = "link:common/belongs-null.html[분과 종류,role=\"popup\"]";
    String LINK_CLUB_TYPE = "link:common/club-type.html[동아리 종류,role=\"popup\"]";
    String LINK_CLUB_TYPE_NULL = "link:common/club-type-null.html[동아리 종류,role=\"popup\"]";
    String LINK_SORT = "link:common/sorting.html[정렬,role=\"popup\"]";
    static Attributes.Attribute example(String value){
        return new Attributes.Attribute("example", value);
    }

    public static void addPageableResponseFields(List<FieldDescriptor> base) {
        base.add(fieldWithPath("pageable.sort.empty").type(WireFormat.FieldType.BOOL).description("정렬 상태 - true인 필드만 확인").attributes(example("false")));
        base.add(fieldWithPath("pageable.sort.sorted").type(WireFormat.FieldType.BOOL).description("정렬 상태 - true인 필드만 확인").attributes(example("true")));
        base.add(fieldWithPath("pageable.sort.unsorted").type(WireFormat.FieldType.BOOL).description("정렬 상태 - true인 필드만 확인").attributes(example("false")));

        base.add(fieldWithPath("pageable.offset").type(WireFormat.FieldType.INT32).description("페이지 첫번째 요소의 전체 순번").attributes(example("0")));
        base.add(fieldWithPath("pageable.pageNumber").type(WireFormat.FieldType.INT32).description("Page Index (start 0)").attributes(example("0")));
        base.add(fieldWithPath("pageable.pageSize").type(WireFormat.FieldType.INT32).description("Elements Count Per Page").attributes(example("20")));
        base.add(fieldWithPath("pageable.paged").type(WireFormat.FieldType.BOOL).description("페이징 여부 - true인 필드만 확인").attributes(example("true")));
        base.add(fieldWithPath("pageable.unpaged").type(WireFormat.FieldType.BOOL).description("페이징 여부 - true인 필드만 확인").attributes(example("false")));

        base.add(fieldWithPath("totalPages").type(WireFormat.FieldType.INT32).description("전체 페이지 수").attributes(example("5")));
        base.add(fieldWithPath("totalElements").type(WireFormat.FieldType.INT32).description("요소 전체 개수").attributes(example("100")));
        base.add(fieldWithPath("first").type(WireFormat.FieldType.BOOL).description("첫번째 페이지인지?").attributes(example("true")));
        base.add(fieldWithPath("last").type(WireFormat.FieldType.BOOL).description("마지막 페이지인지?").attributes(example("false")));
        base.add(fieldWithPath("empty").type(WireFormat.FieldType.BOOL).description("빈 페이지?").attributes(example("false")));
        base.add(fieldWithPath("size").type(WireFormat.FieldType.INT32).description("무시해줭..").attributes(example("")));
        base.add(fieldWithPath("number").type(WireFormat.FieldType.INT32).description("무시해줭...").attributes(example("")));

        base.add(fieldWithPath("sort.empty").type(WireFormat.FieldType.BOOL).description("무시해줭..").attributes(example("")));
        base.add(fieldWithPath("sort.sorted").type(WireFormat.FieldType.BOOL).description("무시해줭..").attributes(example("")));
        base.add(fieldWithPath("sort.unsorted").type(WireFormat.FieldType.BOOL).description("무시해줭..").attributes(example("")));

        base.add(fieldWithPath("numberOfElements").type(WireFormat.FieldType.INT32).description("무시해줭..").attributes(example("")));
    }
}
