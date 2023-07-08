package com.skklub.admin.controller.notice;

import akka.protobuf.WireFormat;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.skklub.admin.controller.NoticeController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.NoticeCreateRequest;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.service.NoticeService;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = NoticeController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser
public class NoticeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private NoticeController noticeController;
    @MockBean
    private S3Transferer s3Transferer;
    @MockBean
    private NoticeRepository noticeRepository;
    @MockBean
    private NoticeService noticeService;


    @Test
    @WithUserDetails(value = "userId0")
    public void createNotice_WithThumbnail_Success() throws Exception{
        //given
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("Notice Test Title", "Notice Test Content");
        MockMultipartFile mockThumbnail = readyMockThumbnail();
        FileNames fileNames = new FileNames("testThumb.png", "savedTestThumb.png");
        given(s3Transferer.uploadOne(mockThumbnail)).willReturn(fileNames);
        given(noticeService.createNotice(noticeCreateRequest.getTitle(), noticeCreateRequest.getContent(), "userId0", fileNames.toThumbnailEntity()))
                .willReturn(0L);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice")
                        .file(mockThumbnail)
                        .queryParam("title", noticeCreateRequest.getTitle())
                        .queryParam("content", noticeCreateRequest.getContent())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0L))
                .andExpect(jsonPath("$.title").value(noticeCreateRequest.getTitle()))
                .andDo(document(
                                "notice/create/",
                                queryParameters(
                                        parameterWithName("title").description("공지 제목").attributes(example("[밴드] 제 22회 못갖춘마디 정기공연 초청")),
                                        parameterWithName("content").description("공지 본문").attributes(example("어쩌구저쩌구 초청합니다 어쩌구저쩌구 초청합니다.어쩌구저쩌구 초청합니다.\n어쩌구저쩌구 초청합니다.\n"))
                                ),
                                requestParts(
                                        partWithName("thumbnailFile").description("공지 썸네일").optional()
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.STRING).description("공지 ID").attributes(example("0")),
                                        fieldWithPath("title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example("[밴드] 제 22회 못갖춘마디 정기공연 초청"))
                                )
                        )
                );
    }

    private MockMultipartFile readyMockThumbnail() throws IOException {
        Path path = Paths.get("src/test/resources/img/default_thumb.png");
        byte[] bytes = Files.readAllBytes(path);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        return new MockMultipartFile("thumbnailFile", "default_thumb.png", "image/png", byteArrayInputStream);
    }

    @Test
    @WithMockUser(value = "userId0")
    public void createNotice_NoThumbnail_SaveAsDefaultThumbnail() throws Exception{
        //given
        Long noticeId = 12L;
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("Notice Test Title", "Notice Test Content");
        FileNames fileNames = new FileNames("default_thumb.png", "default_thumb.png");
        doThrow(AssertionFailedException.class).when(s3Transferer).uploadOne(any(MultipartFile.class));
        given(noticeService.createNotice(noticeCreateRequest.getTitle(), noticeCreateRequest.getContent(), "userId0", fileNames.toThumbnailEntity()))
                .willReturn(noticeId);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice")
                        .queryParam("title", noticeCreateRequest.getTitle())
                        .queryParam("content", noticeCreateRequest.getContent())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noticeId))
                .andExpect(jsonPath("$.title").value(noticeCreateRequest.getTitle()));
    }

    @Test
    public void appendFile_MultiImages_Success() throws Exception {
        //given
        Long noticeId = 0L;
        int fileCnt = 10;
        Notice notice = new Notice("Notice Test Title", "Notice Test Content", null, null);
        List<MockMultipartFile> multipartFiles = readyMockFiles(fileCnt);
        List<FileNames> fileFileNames = readyFileNames(fileCnt);
        List<ExtraFile> extraFiles = fileFileNames.stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
        given(s3Transferer.uploadAll((new ArrayList<>(multipartFiles)))).willReturn(fileFileNames);
        given(noticeService.appendExtraFiles(notice, extraFiles)).willReturn(fileCnt);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice/{noticeId}/file", noticeId)
                        .file(multipartFiles.get(0))
                        .file(multipartFiles.get(1))
                        .file(multipartFiles.get(2))
                        .file(multipartFiles.get(3))
                        .file(multipartFiles.get(4))
                        .file(multipartFiles.get(5))
                        .file(multipartFiles.get(6))
                        .file(multipartFiles.get(7))
                        .file(multipartFiles.get(8))
                        .file(multipartFiles.get(9))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );

        //then

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.fileCnt").value(fileCnt))
                .andDo(document("notice/create/files",
                        pathParameters(
                                parameterWithName("noticeId").description("공지 ID").attributes(example("1"))
                        ),
                        requestParts(
                                partWithName("files").description("첨부 파일(형식은...제한없는듯?)")
                        ),
                        responseFields(
                                fieldWithPath("noticeId").type(WireFormat.FieldType.STRING).description("공지 ID").attributes(example("1")),
                                fieldWithPath("fileCnt").type(WireFormat.FieldType.STRING).description("첨부된 파일 개수").attributes(example("10"))
                        )
                ));
    }

    private List<FileNames> readyFileNames(int fileCnt) {
        List<FileNames> fileFileNames = new ArrayList<>();
        for(int i = 0; i < fileCnt; i++) {
            fileFileNames.add(new FileNames(i + ".pdf", "saved" + i + ".pdf"));
        }
        return fileFileNames;
    }

    private List<MockMultipartFile> readyMockFiles(int fileCnt) throws IOException {
        List<MockMultipartFile> multipartFiles = new ArrayList<>();
        for(int i = 0; i < fileCnt; i++){
            Path path = Paths.get("src/test/resources/file/" + i + ".pdf");
            byte[] bytes = Files.readAllBytes(path);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            multipartFiles.add(new MockMultipartFile("files", "file" + i + ".pdf", "application/pdf", byteArrayInputStream));
        }
        return multipartFiles;
    }

    @Test
    public void uploadActivityImages_NoList_Fail() throws Exception {
        //given

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice/{noticeId}/file", 0L)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );
        //then
        actions.andExpect(status().is4xxClientError());
    }

    @Test
    public void uploadActivityImages_IllegalNoticeId_NoticeIdMisMatchException() throws Exception{
        Long noticeId = 0L;
        int fileCnt = 10;
        List<MockMultipartFile> multipartFiles = readyMockFiles(fileCnt);
        given(noticeRepository.findById(noticeId)).willReturn(Optional.empty());

        //when
        MvcResult badNoticeIdResult = mockMvc.perform(
                        multipart("/notice/{noticeId}/file", noticeId)
                                .file(multipartFiles.get(0))
                                .file(multipartFiles.get(1))
                                .file(multipartFiles.get(2))
                                .file(multipartFiles.get(3))
                                .file(multipartFiles.get(4))
                                .file(multipartFiles.get(5))
                                .file(multipartFiles.get(6))
                                .file(multipartFiles.get(7))
                                .file(multipartFiles.get(8))
                                .file(multipartFiles.get(9))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badNoticeIdResult.getResolvedException()).isExactlyInstanceOf(NoticeIdMisMatchException.class);
    }


}
