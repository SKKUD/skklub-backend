package com.skklub.admin.controller.notice;

import akka.protobuf.WireFormat;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.NoticeController;
import com.skklub.admin.controller.RestDocsUtils;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.NoticeCreateRequest;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.CannotCategorizeByMasterException;
import com.skklub.admin.exception.deprecated.error.exception.CannotCategorizeByUserException;
import com.skklub.admin.exception.deprecated.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.service.NoticeService;
import com.skklub.admin.service.dto.FileNames;
import com.skklub.admin.service.dto.NoticeDeletionDto;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.skklub.admin.controller.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    @MockBean
    private AuthValidator authValidator;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String area;

    @BeforeEach
    public void beforeEach() {
        doNothing().when(authValidator).validateUpdatingClub(anyLong());
        doNothing().when(authValidator).validateUpdatingNotice(anyLong());
        doNothing().when(authValidator).validateUpdatingUser(anyLong());
        doNothing().when(authValidator).validatePendingRequestAuthority(anyLong());
    }

    @Test
    @WithMockUser
    public void createNotice_WithThumbnailAndFiles_Success() throws Exception{
        //given
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("Notice Test Title", "Notice Test Content");
        MockMultipartFile mockThumbnail = readyMockThumbnail();
        FileNames fileNames = new FileNames("testThumb.png", "savedTestThumb.png");
        int fileCnt = 10;
        List<MockMultipartFile> multipartFiles = readyMockFiles(fileCnt);
        List<FileNames> fileFileNames = readyFileNames(fileCnt);
        List<ExtraFile> extraFiles = fileFileNames.stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        given(s3Transferer.uploadOne(mockThumbnail)).willReturn(fileNames);
        given(s3Transferer.uploadAll(new ArrayList<>(multipartFiles))).willReturn(fileFileNames);
        given(noticeService.createNotice(noticeCreateRequest.getTitle(), noticeCreateRequest.getContent(), username, fileNames.toThumbnailEntity(), extraFiles))
                .willReturn(12L);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice")
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
                        .file(mockThumbnail)
                        .queryParam("title", noticeCreateRequest.getTitle())
                        .queryParam("content", noticeCreateRequest.getContent())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.title").value(noticeCreateRequest.getTitle()))
                .andDo(document(
                                "notice/create/notice",
                                queryParameters(
                                        parameterWithName("title").description("공지 제목").attributes(example("[밴드] 제 22회 못갖춘마디 정기공연 초청")),
                                        parameterWithName("content").description("공지 본문").attributes(example("어쩌구저쩌구 초청합니다 어쩌구저쩌구 초청합니다.어쩌구저쩌구 초청합니다.\n어쩌구저쩌구 초청합니다.\n"))
                                ),
                                requestParts(
                                        partWithName("thumbnailFile").description("공지 썸네일").optional(),
                                        partWithName("files").description("첨부 파일(형식 제한 없음)").optional()
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
    @WithMockUser
    public void createNotice_NoThumbnail_SaveAsDefaultThumbnail() throws Exception {
        //given
        Long noticeId = 12L;
        int fileCnt = 10;
        List<MockMultipartFile> multipartFiles = readyMockFiles(fileCnt);
        List<FileNames> fileFileNames = readyFileNames(fileCnt);
        List<ExtraFile> extraFiles = fileFileNames.stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("Notice Test Title", "Notice Test Content");
        FileNames fileNames = new FileNames("default_thumb.png", "default_thumb.png");
        doThrow(RuntimeException.class).when(s3Transferer).uploadOne(any(MultipartFile.class));
        given(s3Transferer.uploadAll(new ArrayList<>(multipartFiles))).willReturn(fileFileNames);
        given(noticeService.createNotice(eq(noticeCreateRequest.getTitle()), eq(noticeCreateRequest.getContent()), anyString(), eq(fileNames.toThumbnailEntity()), eq(extraFiles)))
                .willReturn(noticeId);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice")
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
                        .queryParam("title", noticeCreateRequest.getTitle())
                        .queryParam("content", noticeCreateRequest.getContent())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noticeId))
                .andExpect(jsonPath("$.title").value(noticeCreateRequest.getTitle()));
    }

    @Test
    @WithMockUser
    public void createNotice_NoFiles_HandlingEmptyList() throws Exception {
        //given
        Long noticeId = 12L;
        List<ExtraFile> extraFiles = new ArrayList<>();
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("Notice Test Title", "Notice Test Content");
        MockMultipartFile mockThumbnail = readyMockThumbnail();
        FileNames fileNames = new FileNames("testThumb.png", "savedTestThumb.png");
        given(s3Transferer.uploadOne(mockThumbnail)).willReturn(fileNames);
        given(noticeService.createNotice(eq(noticeCreateRequest.getTitle()), eq(noticeCreateRequest.getContent()), anyString(), eq(fileNames.toThumbnailEntity()), eq(extraFiles)))
                .willReturn(noticeId);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice")
                        .file(mockThumbnail)
                        .queryParam("title", noticeCreateRequest.getTitle())
                        .queryParam("content", noticeCreateRequest.getContent())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
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
                                partWithName("files").description("첨부 파일(형식 제한 없음)")
                        ),
                        responseFields(
                                fieldWithPath("noticeId").type(WireFormat.FieldType.STRING).description("공지 ID").attributes(example("1")),
                                fieldWithPath("fileCnt").type(WireFormat.FieldType.STRING).description("첨부된 파일 개수").attributes(example("10"))
                        )
                ));
    }

    private List<FileNames> readyFileNames(int fileCnt) {
        List<FileNames> fileFileNames = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            fileFileNames.add(new FileNames(i + ".pdf", "saved" + i + ".pdf"));
        }
        return fileFileNames;
    }

    private List<MockMultipartFile> readyMockFiles(int fileCnt) throws IOException {
        List<MockMultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
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
    public void uploadActivityImages_IllegalNoticeId_NoticeIdMisMatchException() throws Exception {
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

    @Test
    public void updateNotice_Default_Success() throws Exception {
        //given
        Long noticeId = 0L;
        String updateTitle = "updateTitle";
        String updateContent = "updateContent";
        given(noticeService.updateNotice(eq(noticeId), any(Notice.class))).willReturn(Optional.of(updateTitle));

        //when
        ResultActions actions = mockMvc.perform(
                patch("/notice/{noticeId}", noticeId)
                        .with(csrf())
                        .queryParam("title", updateTitle)
                        .queryParam("content", updateContent)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noticeId))
                .andExpect(jsonPath("$.title").value(updateTitle))
                .andDo(
                        document("notice/update/notice",
                                pathParameters(
                                        parameterWithName("noticeId").description("수정 대상 공지 ID").attributes(example(noticeId.toString()))
                                ),
                                queryParameters(
                                        parameterWithName("title").description("제목(변경 없을 시 기존꺼 보내주세요)").attributes(example(updateTitle)),
                                        parameterWithName("content").description("글 내용(변경 없을 시 기존꺼 보내주세요)").attributes(example(updateContent))
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.INT64).description("수정 대상 공지 ID").attributes(example(noticeId.toString())),
                                        fieldWithPath("title").type(WireFormat.FieldType.INT64).description("변경 반영 이후 공지 제목").attributes(example(updateTitle))
                                )
                        )
                );

    }

    @Test
    public void updateNotice_BadNoticeId_NoticeMisMatchException() throws Exception {
        //given
        Long noticeId = -1L;
        String updateTitle = "updateTitle";
        String updateContent = "updateContent";
        given(noticeService.updateNotice(eq(noticeId), any(Notice.class))).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                patch("/notice/{noticeId}", noticeId)
                        .with(csrf())
                        .queryParam("title", updateTitle)
                        .queryParam("content", updateContent)
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(NoticeIdMisMatchException.class);

    }

    @Test
    public void updateThumbnail_ChangeFromSomeThumb_CalledS3Deletion() throws Exception {
        //given
        Long noticeId = 0L;
        MockMultipartFile mockMultipartFile = readyMockThumbnail();
        FileNames fileNames = new FileNames("testThumb.png", "savedTestThumb.png");
        FileNames oldFileNames = new FileNames("oldTestThumb.png", "oldSavedTestThumb.png");
        Thumbnail thumbnail = fileNames.toThumbnailEntity();
        given(noticeRepository.existsById(noticeId)).willReturn(true);
        given(s3Transferer.uploadOne(mockMultipartFile)).willReturn(fileNames);
        given(noticeService.updateThumbnail(noticeId, thumbnail)).willReturn(Optional.of(oldFileNames));
        doNothing().when(s3Transferer).deleteOne(oldFileNames.getSavedName());

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice/{noticeId}/thumbnail", noticeId)
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.deletedFileName").value(oldFileNames.getOriginalName()))
                .andExpect(jsonPath("$.changedFileName").value(fileNames.getOriginalName()))
                .andDo(
                        document("notice/update/thumbnail",
                                requestParts(
                                        partWithName("thumbnailFile").description("변경될 썸네일 파일")
                                ),
                                pathParameters(
                                        parameterWithName("noticeId").description("변경 대상 공지 ID").attributes(example(noticeId.toString()))
                                ),
                                responseFields(
                                        fieldWithPath("noticeId").type(WireFormat.FieldType.INT64).description("변경된 공지 ID").attributes(example(noticeId.toString())),
                                        fieldWithPath("deletedFileName").type(WireFormat.FieldType.STRING).description("삭제된 썸네일 파일명").attributes(example(oldFileNames.getOriginalName())),
                                        fieldWithPath("changedFileName").type(WireFormat.FieldType.STRING).description("새로 등록된 썸네일 파일명").attributes(example(fileNames.getOriginalName()))
                                )
                        )
                );
    }

    @Test
    public void updateThumbnail_ChangeFromDefault_SkipS3Deletion() throws Exception {
        //given
        Long noticeId = 0L;
        MockMultipartFile mockMultipartFile = readyMockThumbnail();
        FileNames fileNames = new FileNames("testThumb.png", "savedTestThumb.png");
        FileNames oldFileNames = new FileNames("default_thumb.png", "default_thumb.png");
        Thumbnail thumbnail = fileNames.toThumbnailEntity();
        given(noticeRepository.existsById(noticeId)).willReturn(true);
        given(s3Transferer.uploadOne(mockMultipartFile)).willReturn(fileNames);
        given(noticeService.updateThumbnail(noticeId, thumbnail)).willReturn(Optional.of(oldFileNames));
        doThrow(RuntimeException.class).when(s3Transferer).deleteOne(anyString());

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/notice/{noticeId}/thumbnail", noticeId)
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );


        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.deletedFileName").value(oldFileNames.getOriginalName()))
                .andExpect(jsonPath("$.changedFileName").value(fileNames.getOriginalName()));
    }

    @Test
    public void updateThumbnail_NoticeIsEmpty_NoticeIdMisMatchException() throws Exception {
        //given
        Long noticeId = -1L;
        MockMultipartFile mockMultipartFile = readyMockThumbnail();
        given(noticeRepository.existsById(noticeId)).willReturn(false);

        //when
        MvcResult noticeNotFound = mockMvc.perform(
                multipart("/notice/{noticeId}/thumbnail", noticeId)
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();


        //then
        Assertions.assertThat(noticeNotFound.getResolvedException()).isExactlyInstanceOf(NoticeIdMisMatchException.class);
    }

    @Test
    public void deleteNotice_WithThumbnailAndFiles_Success() throws Exception {
        //given
        Long noticeId = 0L;
        String noticeTitle = "test notice Title";
        FileNames thumbnailFileName = new FileNames("testThumb.png", "savedTestThumb.png");
        int fileCnt = 10;
        List<FileNames> extraFileNames = readyFileNames(fileCnt);
        NoticeDeletionDto noticeDeletionDto = NoticeDeletionDto.builder()
                .noticeTitle(noticeTitle)
                .thumbnailFileName(thumbnailFileName)
                .extraFileNames(extraFileNames)
                .build();
        given(noticeService.deleteNotice(noticeId)).willReturn(Optional.of(noticeDeletionDto));
        doNothing().when(s3Transferer).deleteOne(anyString());
        doNothing().when(s3Transferer).deleteAll(anyList());

        //when
        ResultActions actions = mockMvc.perform(
                delete("/notice/{noticeId}", noticeId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noticeId.toString()))
                .andExpect(jsonPath("$.title").value(noticeTitle))
                .andDo(
                        document("notice/delete/notice/",
                                pathParameters(
                                        parameterWithName("noticeId").description("삭제할 공지 ID").attributes(example(noticeId.toString()))
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.INT64).description("삭제된 공지 ID").attributes(example(noticeId.toString())),
                                        fieldWithPath("title").type(WireFormat.FieldType.STRING).description("삭제된 공지 제목").attributes(example(noticeTitle))
                                )
                        )
                );

    }

    @Test
    public void deleteNotice_WithDefaultThumbnail_SkipS3ThumbnailDeletion() throws Exception {
        //given
        Long noticeId = 0L;
        String noticeTitle = "test notice Title";
        String defaultThumbnailName = "default_thumb.png";
        FileNames thumbnailFileName = new FileNames(defaultThumbnailName, defaultThumbnailName);
        int fileCnt = 10;
        List<FileNames> extraFileNames = readyFileNames(fileCnt);
        NoticeDeletionDto noticeDeletionDto = NoticeDeletionDto.builder()
                .noticeTitle(noticeTitle)
                .thumbnailFileName(thumbnailFileName)
                .extraFileNames(extraFileNames)
                .build();
        given(noticeService.deleteNotice(noticeId)).willReturn(Optional.of(noticeDeletionDto));
        doNothing().when(s3Transferer).deleteOne(anyString());
        doThrow(RuntimeException.class).when(s3Transferer).deleteOne(defaultThumbnailName);
        doNothing().when(s3Transferer).deleteAll(anyList());

        //when
        ResultActions actions = mockMvc.perform(
                delete("/notice/{noticeId}", noticeId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noticeId.toString()))
                .andExpect(jsonPath("$.title").value(noticeTitle));
    }

    @Test
    public void deleteNotice_BadNoticeId_NoticeIdMisMatchException() throws Exception {
        //given
        Long noticeId = -1L;
        given(noticeService.deleteNotice(noticeId)).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                delete("/notice/{noticeId}", noticeId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(NoticeIdMisMatchException.class);
    }

    @Test
    public void deleteFileByOriginalName_Default_Success() throws Exception {
        //given
        Long noticeId = 0L;
        FileNames fileNames = readyFileNames(1).get(0);
        given(noticeService.deleteExtraFile(noticeId, fileNames.getOriginalName())).willReturn(Optional.of(fileNames));
        doNothing().when(s3Transferer).deleteOne(fileNames.getSavedName());

        //when
        ResultActions actions = mockMvc.perform(
                delete("/notice/{noticeId}/{fileName}", noticeId, fileNames.getOriginalName())
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.deletedFileName").value(fileNames.getOriginalName()))
                .andDo(
                        document(
                                "notice/delete/file/",
                                pathParameters(
                                        parameterWithName("noticeId").description("파일이 포함된 공지 ID").attributes(example(noticeId.toString())),
                                        parameterWithName("fileName").description("지우려는 파일 원본명").attributes(example(fileNames.getOriginalName()))
                                ),
                                responseFields(
                                        fieldWithPath("noticeId").type(WireFormat.FieldType.INT64).description("파일이 삭제된 공지 ID").attributes(example(noticeId.toString())),
                                        fieldWithPath("deletedFileName").type(WireFormat.FieldType.STRING).description("지워진 파일 원본명").attributes(example(fileNames.getOriginalName()))
                                )
                        )
                );

    }

    @Test
    public void deleteFileByOriginalName_BadNoticeId_NoticeIdMisMatchException() throws Exception {
        //given
        Long noticeId = -1L;
        FileNames fileNames = readyFileNames(1).get(0);
        given(noticeService.deleteExtraFile(noticeId, fileNames.getOriginalName())).willThrow(NoticeIdMisMatchException.class);

        //when
        MvcResult badIdResult = mockMvc.perform(
                delete("/notice/{noticeId}/{fileName}", noticeId, fileNames.getOriginalName())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException())
                .isExactlyInstanceOf(NoticeIdMisMatchException.class);

    }

    @Test
    public void deleteFileByOriginalName_FileNotInNotice_ExtraFileNameMisMatchException() throws Exception {
        //given

        //when

        //then

    }

    @Test
    public void getDetailNotice_HasPrePost_Success() throws Exception {
        //given
        Long noticeId = 0L;
        User user = new User(null, null, Role.ROLE_ADMIN_SEOUL_CENTRAL, "홍길동", null);
        Notice notice = new Notice("Test Title", "Test Content", user, null);
        settingCreatedAt(notice);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = readyFileNames(fileCnt).stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        Field extraFileIdField = extraFiles.get(0).getClass().getDeclaredField("id");
        extraFileIdField.setAccessible(true);
        for (int i = 0; i < fileCnt; i++) {
            extraFileIdField.set(extraFiles.get(i), Long.valueOf(i));
        }
        notice.appendExtraFiles(extraFiles);
        Optional<Notice> preNotice = Optional.of(new Notice("Test Pre Title", null, null, null));
        Optional<Notice> postNotice = Optional.of(new Notice("Test Post Title", null, null, null));
        Field idField = preNotice.get().getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(notice, noticeId);
        idField.set(preNotice.get(), 1L);
        idField.set(postNotice.get(), 1L);

        List<S3DownloadDto> s3DownloadDtos = readyFileNames(fileCnt).stream()
                .map(
                        f -> new S3DownloadDto(f.getId(), f.getOriginalName(), convertToURL(f.getSavedName()))
                ).collect(Collectors.toList());
        given(s3Transferer.downloadAll(notice.getExtraFiles().stream().map(FileNames::new).collect(Collectors.toList()))).willReturn(s3DownloadDtos);
        given(noticeRepository.findDetailById(noticeId)).willReturn(Optional.of(notice));
        given(noticeService.findPreNotice(notice)).willReturn(preNotice);
        given(noticeService.findPostNotice(notice)).willReturn(postNotice);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/{noticeId}", noticeId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.title").value(notice.getTitle()))
                .andExpect(jsonPath("$.content").value(notice.getContent()))
                .andExpect(jsonPath("$.writerName").value(user.getName()))
                .andExpect(jsonPath("$.createdAt").value(notice.getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                .andExpect(jsonPath("$.preNotice.id").value(preNotice.get().getId()))
                .andExpect(jsonPath("$.preNotice.title").value(preNotice.get().getTitle()))
                .andExpect(jsonPath("$.postNotice.id").value(postNotice.get().getId()))
                .andExpect(jsonPath("$.postNotice.title").value(postNotice.get().getTitle()));
        for (int i = 0; i < fileCnt; i++) {
            actions
//                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].id").value(extraFiles.get(i).getId()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].fileName").value(extraFiles.get(i).getOriginalName()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].url").value(convertToURL(extraFiles.get(i).getSavedName())));
        }
        actions.andDo(
                document(
                        "notice/read/detail",
                        pathParameters(
                                parameterWithName("noticeId").description("공지 ID").attributes(example(noticeId.toString()))
                        ),
                        responseFields(
                                fieldWithPath("noticeId").type(WireFormat.FieldType.INT64).description("공지 ID").attributes(example(noticeId.toString())),
                                fieldWithPath("title").type(WireFormat.FieldType.STRING).description("공지사항 제목").attributes(example(notice.getTitle())),
                                fieldWithPath("content").type(WireFormat.FieldType.STRING).description("공지사항 내용").attributes(example(notice.getContent())),
                                fieldWithPath("writerName").type(WireFormat.FieldType.STRING).description("작성자").attributes(example(user.getName())),
                                fieldWithPath("createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example(notice.getCreatedAt().toString())),
                                fieldWithPath("preNotice.id").type(WireFormat.FieldType.INT64).description("이전 공지 ID").attributes(example(preNotice.get().getId().toString())),
                                fieldWithPath("preNotice.title").type(WireFormat.FieldType.STRING).description("이전 공지 제목").attributes(example(preNotice.get().getTitle())),
                                fieldWithPath("postNotice.id").type(WireFormat.FieldType.INT64).description("다음 공지 ID").attributes(example(postNotice.get().getId().toString())),
                                fieldWithPath("postNotice.title").type(WireFormat.FieldType.STRING).description("다음 공지 제목").attributes(example(postNotice.get().getTitle())),
                                fieldWithPath("extraFileDownloadDtos[].id").type(WireFormat.FieldType.INT64).description("첨부 파일 식별용 ID").attributes(example("1")),
                                fieldWithPath("extraFileDownloadDtos[].fileName").type(WireFormat.FieldType.STRING).description("첨부 파일 원 파일명").attributes(example(extraFiles.get(1).getOriginalName())),
                                fieldWithPath("extraFileDownloadDtos[].url").type(WireFormat.FieldType.STRING).description("첨부 파일 리소스 url").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/eb0808d7-83ee-4ee6-aa1e-e0359dcb54b3.hwp"))

                        )
                )
        );
    }

    private void settingCreatedAt(Notice notice) throws NoSuchFieldException, IllegalAccessException {
        Field createdAtField = notice.getClass().getSuperclass().getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(notice, LocalDateTime.now());
    }

    @Test
    public void getDetailNotice_NoPre_Success() throws Exception {
        //given
        Long noticeId = 0L;
        User user = new User(null, null, Role.ROLE_ADMIN_SEOUL_CENTRAL, "홍길동", null);
        Notice notice = new Notice("Test Title", "Test Content", user, null);
        settingCreatedAt(notice);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = readyFileNames(fileCnt).stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        Field extraFileIdField = extraFiles.get(0).getClass().getDeclaredField("id");
        extraFileIdField.setAccessible(true);
        for (int i = 0; i < fileCnt; i++) {
            extraFileIdField.set(extraFiles.get(i), Long.valueOf(i));
        }
        notice.appendExtraFiles(extraFiles);
        Optional<Notice> postNotice = Optional.of(new Notice("Test Post Title", null, null, null));
        Field idField = postNotice.get().getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(notice, noticeId);
        idField.set(postNotice.get(), 1L);

        List<S3DownloadDto> s3DownloadDtos = readyFileNames(fileCnt).stream()
                .map(
                        f -> new S3DownloadDto(f.getId(), f.getOriginalName(), convertToURL(f.getSavedName()))
                ).collect(Collectors.toList());
        given(s3Transferer.downloadAll(notice.getExtraFiles().stream().map(FileNames::new).collect(Collectors.toList()))).willReturn(s3DownloadDtos);
        given(noticeRepository.findDetailById(noticeId)).willReturn(Optional.of(notice));
        given(noticeService.findPreNotice(notice)).willReturn(Optional.empty());
        given(noticeService.findPostNotice(notice)).willReturn(postNotice);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/{noticeId}", noticeId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.title").value(notice.getTitle()))
                .andExpect(jsonPath("$.content").value(notice.getContent()))
                .andExpect(jsonPath("$.writerName").value(user.getName()))
                .andExpect(jsonPath("$.createdAt").value(notice.getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                .andExpect(jsonPath("$.preNotice").isEmpty())
                .andExpect(jsonPath("$.postNotice.id").value(postNotice.get().getId()))
                .andExpect(jsonPath("$.postNotice.title").value(postNotice.get().getTitle()));
        for (int i = 0; i < fileCnt; i++) {
            actions
//                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].id").value(extraFiles.get(i).getId()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].fileName").value(extraFiles.get(i).getOriginalName()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].url").value(convertToURL(extraFiles.get(i).getSavedName())));
        }

    }

    @Test
    public void getDetailNotice_NoPost_Success() throws Exception {
        //given
        Long noticeId = 0L;
        User user = new User(null, null, Role.ROLE_ADMIN_SEOUL_CENTRAL, "홍길동", null);
        Notice notice = new Notice("Test Title", "Test Content", user, null);
        settingCreatedAt(notice);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = readyFileNames(fileCnt).stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        Field extraFileIdField = extraFiles.get(0).getClass().getDeclaredField("id");
        extraFileIdField.setAccessible(true);
        for (int i = 0; i < fileCnt; i++) {
            extraFileIdField.set(extraFiles.get(i), Long.valueOf(i));
        }
        notice.appendExtraFiles(extraFiles);
        Optional<Notice> preNotice = Optional.of(new Notice("Test Post Title", null, null, null));
        Field idField = preNotice.get().getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(notice, noticeId);
        idField.set(preNotice.get(), 1L);

        List<S3DownloadDto> s3DownloadDtos = readyFileNames(fileCnt).stream()
                .map(
                        f -> new S3DownloadDto(f.getId(), f.getOriginalName(), convertToURL(f.getSavedName()))
                ).collect(Collectors.toList());
        given(s3Transferer.downloadAll(notice.getExtraFiles().stream().map(FileNames::new).collect(Collectors.toList()))).willReturn(s3DownloadDtos);
        given(noticeRepository.findDetailById(noticeId)).willReturn(Optional.of(notice));
        given(noticeService.findPreNotice(notice)).willReturn(preNotice);
        given(noticeService.findPostNotice(notice)).willReturn(Optional.empty());

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/{noticeId}", noticeId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.title").value(notice.getTitle()))
                .andExpect(jsonPath("$.content").value(notice.getContent()))
                .andExpect(jsonPath("$.writerName").value(user.getName()))
                .andExpect(jsonPath("$.createdAt").value(notice.getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                .andExpect(jsonPath("$.preNotice.id").value(preNotice.get().getId()))
                .andExpect(jsonPath("$.preNotice.title").value(preNotice.get().getTitle()))
                .andExpect(jsonPath("$.postNotice").isEmpty());
        for (int i = 0; i < fileCnt; i++) {
            actions
//                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].id").value(extraFiles.get(i).getId()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].fileName").value(extraFiles.get(i).getOriginalName()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].url").value(convertToURL(extraFiles.get(i).getSavedName())));
        }
    }

    @Test
    public void getDetailNotice_NoPreAndPost_Success() throws Exception {
        //given
        Long noticeId = 0L;
        User user = new User(null, null, Role.ROLE_ADMIN_SEOUL_CENTRAL, "홍길동", null);
        Notice notice = new Notice("Test Title", "Test Content", user, null);
        settingCreatedAt(notice);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = readyFileNames(fileCnt).stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        Field extraFileIdField = extraFiles.get(0).getClass().getDeclaredField("id");
        extraFileIdField.setAccessible(true);
        for (int i = 0; i < fileCnt; i++) {
            extraFileIdField.set(extraFiles.get(i), Long.valueOf(i));
        }
        notice.appendExtraFiles(extraFiles);
        Field idField = notice.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(notice, noticeId);

        List<S3DownloadDto> s3DownloadDtos = readyFileNames(fileCnt).stream()
                .map(
                        f -> new S3DownloadDto(f.getId(), f.getOriginalName(), convertToURL(f.getSavedName()))
                ).collect(Collectors.toList());
        given(s3Transferer.downloadAll(notice.getExtraFiles().stream().map(FileNames::new).collect(Collectors.toList()))).willReturn(s3DownloadDtos);
        given(noticeRepository.findDetailById(noticeId)).willReturn(Optional.of(notice));
        given(noticeService.findPreNotice(notice)).willReturn(Optional.empty());
        given(noticeService.findPostNotice(notice)).willReturn(Optional.empty());

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/{noticeId}", noticeId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.title").value(notice.getTitle()))
                .andExpect(jsonPath("$.content").value(notice.getContent()))
                .andExpect(jsonPath("$.writerName").value(user.getName()))
                .andExpect(jsonPath("$.createdAt").value(notice.getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                .andExpect(jsonPath("$.preNotice").isEmpty())
                .andExpect(jsonPath("$.postNotice").isEmpty());
        for (int i = 0; i < fileCnt; i++) {
            actions
//                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].id").value(extraFiles.get(i).getId()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].fileName").value(extraFiles.get(i).getOriginalName()))
                    .andExpect(jsonPath("$.extraFileDownloadDtos[" + i + "].url").value(convertToURL(extraFiles.get(i).getSavedName())));
        }
    }

    @Test
    public void getDetailNotice_BadNoticeId_NoticeIdMisMatchException() throws Exception {
        //given
        Long noticeId = -1L;
        given(noticeRepository.findDetailById(noticeId)).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                get("/notice/{noticeId}", noticeId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(NoticeIdMisMatchException.class);
    }

    @Test
    public void getNoticePrevWithThumbnail_Default_Success() throws Exception {
        //given
        PageRequest request = PageRequest.of(1, 5, Sort.by("title").ascending().and(Sort.by("createdAt").ascending()));
        int noticeCnt = 20;
        List<Notice> notices = readyNoticeWithUserAndThumbnail(noticeCnt);
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        PageImpl<Notice> noticePages = new PageImpl<>(notices, request, noticeCnt);
        given(noticeRepository.findAllWithThumbnailBy(request)).willReturn(noticePages);
        for (Notice notice : notices) {
            given(s3Transferer.downloadOne(new FileNames(notice.getThumbnail())))
                    .willReturn(new S3DownloadDto(null, notice.getThumbnail().getOriginalName(), convertToURL(notice.getThumbnail().getUploadedName())));
        }

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev/thumbnail")
                        .with(csrf())
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
                        .queryParam("sort", "title,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for (int i = 0; i < 5; i++) {
            actions.andExpect(jsonPath("$.content[" + i + "].noticeId").value(i))
                    .andExpect(jsonPath("$.content[" + i + "].title").value(notices.get(i).getTitle()))
                    .andExpect(jsonPath("$.content[" + i + "].content").value(notices.get(i).getContent()))
                    .andExpect(jsonPath("$.content[" + i + "].createdAt").value(notices.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                    .andExpect(jsonPath("$.content[" + i + "].thumbnail.fileName").value(notices.get(i).getThumbnail().getOriginalName()))
                    .andExpect(jsonPath("$.content[" + i + "].thumbnail.url").value(convertToURL(notices.get(i).getThumbnail().getUploadedName())));
        }

        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].noticeId").type(WireFormat.FieldType.INT64).description("공지 아이디").attributes(example(notices.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example(notices.get(0).getTitle())));
        pageableResponseFields.add(fieldWithPath("content[].content").type(WireFormat.FieldType.STRING).description("공지 내용").attributes(example(notices.get(0).getContent())));
        pageableResponseFields.add(fieldWithPath("content[].createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example("yyyy-MM-dd'T'HH:mm")));
        pageableResponseFields.add(fieldWithPath("content[].thumbnail.id").type(WireFormat.FieldType.INT64).description("썸네일 아이디").attributes(example("1")));
        pageableResponseFields.add(fieldWithPath("content[].thumbnail.fileName").type(WireFormat.FieldType.STRING).description("썸네일 원본 파일명").attributes(example(notices.get(0).getThumbnail().getOriginalName())));
        pageableResponseFields.add(fieldWithPath("content[].thumbnail.url").type(WireFormat.FieldType.STRING).description("썸네일 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("notice/read/prevs/thumbnail",
                        queryParameters(
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬(기본 시간순)").attributes(example(LINK_SORT_NOTICE))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );

    }

    private String convertToURL(String uploadedName) {
        return "https://s3." + area + ".amazonaws.com/" + bucket + "/" + uploadedName;
    }

    @Test
    public void getNoticePrevWithThumbnail_NoSort_Success() throws Exception {
        //given
        PageRequest request = PageRequest.of(1, 5, Sort.by("createdAt").ascending());
        int noticeCnt = 20;
        List<Notice> notices = readyNoticeWithUserAndThumbnail(noticeCnt);
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        PageImpl<Notice> noticePages = new PageImpl<>(notices, request, noticeCnt);
        given(noticeRepository.findAllWithThumbnailBy(request)).willReturn(noticePages);
        for (Notice notice : notices) {
            given(s3Transferer.downloadOne(new FileNames(notice.getThumbnail())))
                    .willReturn(new S3DownloadDto(null, notice.getThumbnail().getOriginalName(), convertToURL(notice.getThumbnail().getUploadedName())));
        }

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev/thumbnail")
                        .with(csrf())
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for (int i = 0; i < 5; i++) {
            actions.andExpect(jsonPath("$.content[" + i + "].noticeId").value(i))
                    .andExpect(jsonPath("$.content[" + i + "].title").value(notices.get(i).getTitle()))
                    .andExpect(jsonPath("$.content[" + i + "].content").value(notices.get(i).getContent()))
                    .andExpect(jsonPath("$.content[" + i + "].createdAt").value(notices.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                    .andExpect(jsonPath("$.content[" + i + "].thumbnail.fileName").value(notices.get(i).getThumbnail().getOriginalName()))
                    .andExpect(jsonPath("$.content[" + i + "].thumbnail.url").value(convertToURL(notices.get(i).getThumbnail().getUploadedName())));
        }

        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].noticeId").type(WireFormat.FieldType.INT64).description("공지 아이디").attributes(example(notices.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example(notices.get(0).getTitle())));
        pageableResponseFields.add(fieldWithPath("content[].content").type(WireFormat.FieldType.STRING).description("공지 내용").attributes(example(notices.get(0).getContent())));
        pageableResponseFields.add(fieldWithPath("content[].createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example("yyyy-MM-dd'T'HH:mm")));
        pageableResponseFields.add(fieldWithPath("content[].thumbnail.id").type(WireFormat.FieldType.INT64).description("썸네일 아이디").attributes(example("1")));
        pageableResponseFields.add(fieldWithPath("content[].thumbnail.fileName").type(WireFormat.FieldType.STRING).description("썸네일 원본 파일명").attributes(example(notices.get(0).getThumbnail().getOriginalName())));
        pageableResponseFields.add(fieldWithPath("content[].thumbnail.url").type(WireFormat.FieldType.STRING).description("썸네일 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("notice/read/prevs/thumbnail",
                        queryParameters(
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬(기본 시간순)").attributes(example(LINK_SORT_NOTICE))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );

    }



    @Test
    public void getNoticeThumbnailByNoticeId_Default_Success() throws Exception{
        //given
        Long noticeId = 0L;
        List<Notice> notices = readyNoticeWithUserAndThumbnail(1);
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        Notice notice = notices.get(0);
        given(noticeRepository.findWithThumbnailById(noticeId)).willReturn(Optional.of(notice));
        given(s3Transferer.downloadOne(new FileNames(notice.getThumbnail())))
                .willReturn(new S3DownloadDto(null, notice.getThumbnail().getOriginalName(), convertToURL(notice.getThumbnail().getUploadedName())));

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev/{noticeId}", noticeId)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.title").value(notice.getTitle()))
                .andExpect(jsonPath("$.content").value(notice.getContent()))
                .andExpect(jsonPath("$.createdAt").value(notice.getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                .andExpect(jsonPath("$.thumbnail.fileName").value(notice.getThumbnail().getOriginalName()))
                .andExpect(jsonPath("$.thumbnail.url").value(convertToURL(notice.getThumbnail().getUploadedName())))
                .andDo(
                        document("notice/read/prevs/thumbnail/byId",
                                pathParameters(
                                        parameterWithName("noticeId").description("공지 Id").attributes(example(noticeId.toString()))
                                ),
                                responseFields(
                                        fieldWithPath("noticeId").type(WireFormat.FieldType.INT64).description("공지 아이디").attributes(example(notice.getId().toString())),
                                        fieldWithPath("title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example(notice.getTitle())),
                                        fieldWithPath("content").type(WireFormat.FieldType.STRING).description("공지 내용").attributes(example(notice.getContent())),
                                        fieldWithPath("createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example("yyyy-MM-dd'T'HH:mm")),
                                        fieldWithPath("thumbnail.id").type(WireFormat.FieldType.INT64).description("썸네일 아이디").attributes(example("1")),
                                        fieldWithPath("thumbnail.fileName").type(WireFormat.FieldType.STRING).description("썸네일 원본 파일명").attributes(example(notice.getThumbnail().getOriginalName())),
                                        fieldWithPath("thumbnail.url").type(WireFormat.FieldType.STRING).description("썸네일 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg"))
                                )
                        )
                );
    }

    @Test
    public void getNoticePrev_NoRole_전체조회() throws Exception {
        //given
        PageRequest request = PageRequest.of(1, 5, Sort.by("title").ascending().and(Sort.by("createdAt").ascending()));
        int noticeCnt = 20;
        List<Notice> notices = readyNoticeWithUserAndThumbnail(noticeCnt);
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        PageImpl<Notice> noticePages = new PageImpl<>(notices, request, noticeCnt);
        given(noticeRepository.findAll(request)).willReturn(noticePages);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev")
                        .with(csrf())
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
                        .queryParam("sort", "title,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for (int i = 0; i < 5; i++) {
            actions.andExpect(jsonPath("$.content[" + i + "].noticeId").value(i))
                    .andExpect(jsonPath("$.content[" + i + "].title").value(notices.get(i).getTitle()))
                    .andExpect(jsonPath("$.content[" + i + "].writerName").value(notices.get(i).getWriter().getName()))
                    .andExpect(jsonPath("$.content[" + i + "].createdAt").value(notices.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()));
        }


        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].noticeId").type(WireFormat.FieldType.INT64).description("공지 아이디").attributes(example(notices.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example(notices.get(0).getTitle())));
        pageableResponseFields.add(fieldWithPath("content[].writerName").type(WireFormat.FieldType.STRING).description("작성자").attributes(example(notices.get(0).getWriter().getName())));
        pageableResponseFields.add(fieldWithPath("content[].createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example("yyyy-MM-dd'T'HH:mm")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("notice/read/prevs/role",
                        queryParameters(
                                parameterWithName("role").optional().description("검색할 유저 - 권한").attributes(example(RestDocsUtils.LINK_ADMIN)),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬(기본 시간순)").attributes(example(LINK_SORT_NOTICE))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );

    }

    @Test
    public void getNoticePrev_NoSort_전체조회() throws Exception {
        //given
        PageRequest request = PageRequest.of(1, 5, Sort.by("createdAt").ascending());
        int noticeCnt = 20;
        List<Notice> notices = readyNoticeWithUserAndThumbnail(noticeCnt);
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        PageImpl<Notice> noticePages = new PageImpl<>(notices, request, noticeCnt);
        given(noticeRepository.findAll(request)).willReturn(noticePages);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev")
                        .with(csrf())
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for (int i = 0; i < 5; i++) {
            actions.andExpect(jsonPath("$.content[" + i + "].noticeId").value(i))
                    .andExpect(jsonPath("$.content[" + i + "].title").value(notices.get(i).getTitle()))
                    .andExpect(jsonPath("$.content[" + i + "].writerName").value(notices.get(i).getWriter().getName()))
                    .andExpect(jsonPath("$.content[" + i + "].createdAt").value(notices.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()));
        }


        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].noticeId").type(WireFormat.FieldType.INT64).description("공지 아이디").attributes(example(notices.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example(notices.get(0).getTitle())));
        pageableResponseFields.add(fieldWithPath("content[].writerName").type(WireFormat.FieldType.STRING).description("작성자").attributes(example(notices.get(0).getWriter().getName())));
        pageableResponseFields.add(fieldWithPath("content[].createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example("yyyy-MM-dd'T'HH:mm")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("notice/read/prevs/role",
                        queryParameters(
                                parameterWithName("role").optional().description("검색할 유저 - 권한").attributes(example(RestDocsUtils.LINK_ADMIN)),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬(기본 시간순)").attributes(example(LINK_SORT_NOTICE))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );

    }

    @Test
    public void getNoticePrev_RoleAdmin_findAllSpecificNotices() throws Exception {
        //given
        Role role = Role.ROLE_ADMIN_SEOUL_CENTRAL;
        PageRequest request = PageRequest.of(1, 5, Sort.by("title").ascending().and(Sort.by("createdAt").ascending()));
        int noticeCnt = 20;
        List<Notice> notices = readyNoticeWithUserAndThumbnail(noticeCnt);
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        PageImpl<Notice> noticePages = new PageImpl<>(notices, request, noticeCnt);
        given(noticeRepository.findAll(request)).willReturn(noticePages);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev")
                        .with(csrf())
                        .queryParam("role", role.toString())
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
                        .queryParam("sort", "title,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for (int i = 0; i < 5; i++) {
            actions.andExpect(jsonPath("$.content[" + i + "].noticeId").value(i))
                    .andExpect(jsonPath("$.content[" + i + "].title").value(notices.get(i).getTitle()))
                    .andExpect(jsonPath("$.content[" + i + "].writerName").value(notices.get(i).getWriter().getName()))
                    .andExpect(jsonPath("$.content[" + i + "].createdAt").value(notices.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()));
        }
    }

    @Test
    public void getNoticePrev_RoleUser_CannotCategorizeByUserException() throws Exception {
        //given
        PageRequest request = PageRequest.of(1, 5, Sort.by("title").ascending().and(Sort.by("createdAt").ascending()));
        Role role = Role.ROLE_USER;

        //when
        MvcResult badRoleResult = mockMvc.perform(
                get("/notice/prev")
                        .with(csrf())
                        .queryParam("role", role.toString())
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
                        .queryParam("sort", "title,ASC")
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badRoleResult.getResolvedException())
                .isExactlyInstanceOf(CannotCategorizeByUserException.class);
    }

    @Test
    public void getNoticePrev_RoleMaster_CannotCategorizeByMasterException() throws Exception {
        //given
        PageRequest request = PageRequest.of(1, 5, Sort.by("title").ascending().and(Sort.by("createdAt").ascending()));
        Role role = Role.ROLE_MASTER;

        //when
        MvcResult badRoleResult = mockMvc.perform(
                get("/notice/prev")
                        .with(csrf())
                        .queryParam("role", role.toString())
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
                        .queryParam("sort", "title,ASC")
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badRoleResult.getResolvedException())
                .isExactlyInstanceOf(CannotCategorizeByMasterException.class);

    }

    @Test
    public void getNoticePrevByTitle_Default_Success() throws Exception {
        //given
        String keyword = "test";
        PageRequest request = PageRequest.of(0, 2, Sort.by("title").ascending().and(Sort.by("createdAt").ascending()));
        User user = new User("userId", "userPassword", Role.ROLE_ADMIN_SEOUL_CENTRAL, "test User", "test Contact");
        List<Notice> notices = new ArrayList<>();
        notices.add(new Notice("test title", "test content", user, null));
        notices.add(new Notice("tittestle", "test content", user, null));
        notices.add(new Notice("title test", "test content", user, null));
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        PageImpl<Notice> noticePages = new PageImpl<>(notices, request, 3);
        given(noticeRepository.findWithWriterAllByTitleContainingOrderByCreatedAt(keyword, request))
                .willReturn(noticePages);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev/search/title")
                        .queryParam("title", keyword)
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
                        .queryParam("sort", "title,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for (int i = 0; i < 2; i++) {
            actions.andExpect(jsonPath("$.content[" + i + "].noticeId").value(i))
                    .andExpect(jsonPath("$.content[" + i + "].title").value(notices.get(i).getTitle()))
                    .andExpect(jsonPath("$.content[" + i + "].writerName").value(notices.get(i).getWriter().getName()))
                    .andExpect(jsonPath("$.content[" + i + "].createdAt").value(notices.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()));
        }


        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].noticeId").type(WireFormat.FieldType.INT64).description("공지 아이디").attributes(example(notices.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example(notices.get(0).getTitle())));
        pageableResponseFields.add(fieldWithPath("content[].writerName").type(WireFormat.FieldType.STRING).description("작성자").attributes(example(notices.get(0).getWriter().getName())));
        pageableResponseFields.add(fieldWithPath("content[].createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example("yyyy-MM-dd'T'HH:mm")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("notice/read/prevs/title",
                        queryParameters(
                                parameterWithName("title").optional().description("제목 검색 키워드").attributes(example("'test' : 'testabcdef' or 'abctestdef' or 'abcdeftest'")),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬(기본 시간순)").attributes(example(LINK_SORT_NOTICE))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );

    }

    @Test
    public void getNoticePrevByTitle_NoSort_SortByCreatedAt() throws Exception {
        //given
        String keyword = "test";
        PageRequest request = PageRequest.of(0, 2, Sort.by("createdAt").ascending());
        User user = new User("userId", "userPassword", Role.ROLE_ADMIN_SEOUL_CENTRAL, "test User", "test Contact");
        List<Notice> notices = new ArrayList<>();
        notices.add(new Notice("test title", "test content", user, null));
        notices.add(new Notice("tittestle", "test content", user, null));
        notices.add(new Notice("title test", "test content", user, null));
        setNoticeIds(notices);
        setNoticeCreatedAt(notices);
        PageImpl<Notice> noticePages = new PageImpl<>(notices, request, 3);
        given(noticeRepository.findWithWriterAllByTitleContainingOrderByCreatedAt(keyword, request))
                .willReturn(noticePages);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/prev/search/title")
                        .queryParam("title", keyword)
                        .queryParam("size", String.valueOf(request.getPageSize()))
                        .queryParam("page", String.valueOf(request.getPageNumber()))
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for (int i = 0; i < 2; i++) {
            actions.andExpect(jsonPath("$.content[" + i + "].noticeId").value(i))
                    .andExpect(jsonPath("$.content[" + i + "].title").value(notices.get(i).getTitle()))
                    .andExpect(jsonPath("$.content[" + i + "].writerName").value(notices.get(i).getWriter().getName()))
                    .andExpect(jsonPath("$.content[" + i + "].createdAt").value(notices.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()));
        }


        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].noticeId").type(WireFormat.FieldType.INT64).description("공지 아이디").attributes(example(notices.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].title").type(WireFormat.FieldType.STRING).description("공지 제목").attributes(example(notices.get(0).getTitle())));
        pageableResponseFields.add(fieldWithPath("content[].writerName").type(WireFormat.FieldType.STRING).description("작성자").attributes(example(notices.get(0).getWriter().getName())));
        pageableResponseFields.add(fieldWithPath("content[].createdAt").type(WireFormat.FieldType.STRING).description("작성일자").attributes(example("yyyy-MM-dd'T'HH:mm")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("notice/read/prevs/title",
                        queryParameters(
                                parameterWithName("title").optional().description("제목 검색 키워드").attributes(example("'test' : 'testabcdef' or 'abctestdef' or 'abcdeftest'")),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬(기본 시간순)").attributes(example(LINK_SORT_NOTICE))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );

    }

//    @Test
    public void getFile_Default_Success() throws Exception {
        //given
        String fileSavedName = "save_File.pdf";
        byte[] bytes = "test Bytes".getBytes();
        S3DownloadDto s3DownloadDto = new S3DownloadDto(2L, "original_file.pdf", "test Bytes");
        given(s3Transferer.downloadOne(new FileNames(null, fileSavedName))).willReturn(s3DownloadDto);

        //when
        ResultActions actions = mockMvc.perform(
                get("/notice/file")
                        .with(csrf())
                        .queryParam("fileSavedName", fileSavedName)
        );

        //then
        actions.andExpect(status().isOk())
                .andDo(
                        document("notice/read/file",
                                queryParameters(
                                        parameterWithName("fileSavedName").description("파일이 S3에 저장된 이름").attributes(example("002e73a5-511a-4315-a85d-6c40fb60.pdf"))
                                )
                        )
                );

    }

    private List<Notice> readyNoticeWithUserAndThumbnail(int noticeCnt) {
        List<Notice> notices = new ArrayList<>();
        for (int i = 0; i < noticeCnt; i++) {
            Thumbnail thumbnail = new Thumbnail("test_Thumb" + i + ".jpg", "saved_Thumb" + i + ".jpg");
            User user = new User("username " + i, "password " + i, Role.ROLE_ADMIN_SEOUL_CENTRAL, "test name " + i, null);
            notices.add(
                    new Notice("test title" + i, "test content " + i, user, thumbnail)
            );
        }
        return notices;
    }

    private void setNoticeIds(List<Notice> notices) throws NoSuchFieldException, IllegalAccessException {
        Field id = notices.get(0).getClass().getDeclaredField("id");
        id.setAccessible(true);
        for (int i = 0; i < notices.size(); i++) {
            id.set(notices.get(i), (long) i);
        }
    }

    private void setNoticeCreatedAt(List<Notice> notices) throws NoSuchFieldException, IllegalAccessException {
        Field createdAt = notices.get(0).getClass().getSuperclass().getSuperclass().getDeclaredField("createdAt");
        createdAt.setAccessible(true);
        for (int i = 0; i < notices.size(); i++) {
            createdAt.set(notices.get(i), LocalDateTime.now());
        }
    }
}
