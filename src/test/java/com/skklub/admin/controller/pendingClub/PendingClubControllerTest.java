package com.skklub.admin.controller.pendingClub;

import akka.protobuf.WireFormat;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.PendingClubController;
import com.skklub.admin.controller.dto.PendingClubRequest;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.CannotRequestCreationToUserException;
import com.skklub.admin.exception.deprecated.error.exception.InvalidApproachException;
import com.skklub.admin.exception.deprecated.error.exception.InvalidBelongsException;
import com.skklub.admin.exception.deprecated.error.exception.PendingClubIdMisMatchException;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.PendingClubService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.validation.BindException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.skklub.admin.controller.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WithMockUser
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = PendingClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class PendingClubControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PendingClubService pendingClubService;
    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PendingClubRepository pendingClubRepository;
    @MockBean
    private AuthValidator authValidator;

    @BeforeEach
    public void beforeEach() {
        doNothing().when(authValidator).validateUpdatingClub(anyLong());
        doNothing().when(authValidator).validateUpdatingNotice(anyLong());
        doNothing().when(authValidator).validateUpdatingUser(anyLong());
        doNothing().when(authValidator).validatePendingRequestAuthority(anyLong());
    }

    @Test
    public void createPending_Default_Success() throws Exception{
        //given
        Role requestTo = Role.ROLE_ADMIN_SEOUL_CENTRAL;
        String clubName = "testPendingName";
        String briefActivityDescription = "testBriefDescription";
        String activityDescription = "testActivityDescription";
        String clubDescription = "testClubDescription";
        String username = "testUserId";
        String password = "testPw";
        String presidentName = "testUser";
        String presidentContact = "testContact";
        PendingClubRequest pendingClubRequest = new PendingClubRequest(
                requestTo,
                clubName,
                briefActivityDescription,
                activityDescription,
                clubDescription,
                username,
                password,
                presidentName,
                presidentContact
        );
        Long pendingClubId = 31L;
        String encodedPw = "AfterEncodedPw";
        given(bCryptPasswordEncoder.encode(password)).willReturn(encodedPw);
        doAnswer(
                invocation -> {
                    PendingClub pendingClub = pendingClubRequest.toEntity(encodedPw);
                    Field declaredField = pendingClub.getClass().getDeclaredField("id");
                    declaredField.setAccessible(true);
                    declaredField.set(pendingClub, pendingClubId);
                    Field timeField = pendingClub.getClass().getSuperclass().getDeclaredField("createdAt");
                    timeField.setAccessible(true);
                    timeField.set(pendingClub, LocalDateTime.now());
                    return pendingClub;
                }
        ).when(pendingClubService).requestCreation(pendingClubRequest.toEntity(encodedPw));

        //when
        ResultActions actions = mockMvc.perform(
                post("/pending")
                        .with(csrf())
                        .queryParam("requestTo", requestTo.toString())
                        .queryParam("clubName", clubName)
                        .queryParam("briefActivityDescription", briefActivityDescription)
                        .queryParam("activityDescription", activityDescription)
                        .queryParam("clubDescription", clubDescription)
                        .queryParam("username", username)
                        .queryParam("password", password)
                        .queryParam("presidentName", presidentName)
                        .queryParam("presidentContact", presidentContact)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingClubId").value(pendingClubId))
                .andExpect(jsonPath("$.requestTo").value(pendingClubRequest.getRequestTo().toString()))
                .andExpect(jsonPath("$.requestedAt").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString()))
                .andExpect(jsonPath("$.clubName").value(pendingClubRequest.getClubName()))
                .andExpect(jsonPath("$.briefActivityDescription").value(pendingClubRequest.getBriefActivityDescription()))
                .andExpect(jsonPath("$.activityDescription").value(pendingClubRequest.getActivityDescription()))
                .andExpect(jsonPath("$.clubDescription").value(pendingClubRequest.getClubDescription()))
                .andExpect(jsonPath("$.presidentName").value(pendingClubRequest.getPresidentName()))
                .andExpect(jsonPath("$.presidentContact").value(pendingClubRequest.getPresidentContact()))
                .andDo(
                        document(
                                "pending/create",
                                queryParameters(
                                        parameterWithName("requestTo").description("지정한 생성 담당자").attributes(example(LINK_NON_USER)),
                                        parameterWithName("clubName").description("생성 희망 동아리 이름").attributes(example("생성하려는동아리")),
                                        parameterWithName("briefActivityDescription").description("동아리 분류 - 세부 활동").attributes(example("박물관견학")),
                                        parameterWithName("activityDescription").description("요청 동아리 활동 설명").attributes(example("우리는 xxx, xxxx를 합니다")),
                                        parameterWithName("clubDescription").description("요청 동아리 설명").attributes(example("우리는 xxx한 동아리입니다")),
                                        parameterWithName("username").description("희망 로그인 ID").attributes(example("UserIdExample412")),
                                        parameterWithName("password").description("희망 로그인 PW").attributes(example("UserPwExample1234")),
                                        parameterWithName("presidentName").description("신청자 이름").attributes(example("홍길동")),
                                        parameterWithName("presidentContact").description("신청자 연락처").attributes(example("010-1234-1234"))
                                ),
                                responseFields(
                                        fieldWithPath("pendingClubId").type(WireFormat.FieldType.INT64).description("생성 요청 ID").attributes(example("1")),
                                        fieldWithPath("requestTo").type(WireFormat.FieldType.STRING).description("지정된 생성 담당자").attributes(example(LINK_NON_USER)),
                                        fieldWithPath("requestedAt").type(WireFormat.FieldType.STRING).description("요청 생성 시점").attributes(example("yyyy-MM-dd'T'HH:mm")),
                                        fieldWithPath("clubName").type(WireFormat.FieldType.STRING).description("생성 희망 동아리 이름").attributes(example("생성하려는동아리")),
                                        fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("동아리 분류 - 세부 활동").attributes(example("박물관견학")),
                                        fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("요청 동아리 활동 설명").attributes(example("우리는 xxx, xxxx를 합니다")),
                                        fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("요청 동아리 설명").attributes(example("우리는 xxx한 동아리입니다")),
                                        fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("신청자 이름").attributes(example("홍길동")),
                                        fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("신청자 연락처").attributes(example("010-1234-1234"))
                                )
                        )
                );
    }

    @Test
    public void createPending_ReqToUser_CannotRequestCreationToUserException() throws Exception{
        //given
        Role requestTo = Role.ROLE_USER;
        String clubName = "testPendingName";
        String briefActivityDescription = "testBriefDescription";
        String activityDescription = "testActivityDescription";
        String clubDescription = "testClubDescription";
        String username = "testUserId";
        String password = "testPw";
        String presidentName = "testUser";
        String presidentContact = "testContact";

        //when
        MvcResult badRoleResult = mockMvc.perform(
                        post("/pending")
                                .with(csrf())
                                .queryParam("requestTo", requestTo.toString())
                                .queryParam("clubName", clubName)
                                .queryParam("briefActivityDescription", briefActivityDescription)
                                .queryParam("activityDescription", activityDescription)
                                .queryParam("clubDescription", clubDescription)
                                .queryParam("username", username)
                                .queryParam("password", password)
                                .queryParam("presidentName", presidentName)
                                .queryParam("presidentContact", presidentContact)
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badRoleResult.getResolvedException()).isExactlyInstanceOf(CannotRequestCreationToUserException.class);

    }

    @Test
    public void createPending_GivenSomeNull_BindException() throws Exception{
        //given
        Role requestTo = Role.ROLE_USER;
        String clubName = "testPendingName";
        String briefActivityDescription = "testBriefDescription";
        String activityDescription = "testActivityDescription";
        String clubDescription = "testClubDescription";
        String username = "testUserId";
        String password = "testPw";
        String presidentName = "testUser";
        String presidentContact = "testContact";

        //when
        MvcResult badRoleResult = mockMvc.perform(
                        post("/pending")
                                .with(csrf())
                                .queryParam("requestTo", requestTo.toString())
                                .queryParam("clubName", clubName)
                                .queryParam("briefActivityDescription", briefActivityDescription)
                                .queryParam("clubDescription", clubDescription)
                                .queryParam("presidentName", presidentName)
                                .queryParam("presidentContact", presidentContact)
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badRoleResult.getResolvedException()).isExactlyInstanceOf(BindException.class);
    }

    @Test
    public void getPendings_Default_Success() throws Exception{
        //given
        PageRequest pageRequest = PageRequest.of(1, 3, Sort.by("clubName").ascending());
        User user = new User("user", "mockUserPw", Role.ROLE_ADMIN_SEOUL_CENTRAL, "홍길동", "010-0000-0000");
        given(userRepository.findByUsername("user")).willReturn(user);
        int pendingClubCnt = 7;
        List<PendingClub> pendingClubs = new ArrayList<>();
        for (int i = 0; i < pendingClubCnt; i++) {
            PendingClub pendingClub = new PendingClub(
                    "testPendingName" + i,
                    "testBriefDescription" + i,
                    "testActivityDescription" + i,
                    "testClubDescription" + i,
                    "testUserId" + i,
                    "testPw" + i,
                    "testUser" + i,
                    "testContact" + i,
                    Role.ROLE_ADMIN_SEOUL_CENTRAL
            );
            Field createdAtField = pendingClub.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(pendingClub, LocalDateTime.now());
            Field idField = pendingClub.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pendingClub, Long.valueOf(i));
            pendingClubs.add(pendingClub);
        }
        pendingClubRepository.saveAll(pendingClubs);
        Page<PendingClub> pendingClubPage = new PageImpl<>(pendingClubs, pageRequest, pendingClubCnt);
        given(pendingClubRepository.findAllByRequestTo(eq(user.getRole()), any(Pageable.class))).willReturn(pendingClubPage);

        //when
        ResultActions actions = mockMvc.perform(
                get("/pending")
                        .queryParam("size", String.valueOf(pageRequest.getPageSize()))
                        .queryParam("page", String.valueOf(pageRequest.getOffset()))
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        List<PendingClub> results = pendingClubPage.get().collect(Collectors.toList());
        for (int i = 0; i < 3; i++) {
            actions
                    .andExpect(jsonPath("$.content[" + i + "].pendingClubId").value(results.get(i).getId()))
                    .andExpect(jsonPath("$.content[" + i + "].requestTo").value(results.get(i).getRequestTo().toString()))
                    .andExpect(jsonPath("$.content[" + i + "].requestedAt").value(results.get(i).getCreatedAt().truncatedTo(ChronoUnit.MINUTES).toString()))
                    .andExpect(jsonPath("$.content[" + i + "].clubName").value(results.get(i).getClubName()))
                    .andExpect(jsonPath("$.content[" + i + "].briefActivityDescription").value(results.get(i).getBriefActivityDescription()))
                    .andExpect(jsonPath("$.content[" + i + "].activityDescription").value(results.get(i).getActivityDescription()))
                    .andExpect(jsonPath("$.content[" + i + "].clubDescription").value(results.get(i).getClubDescription()))
                    .andExpect(jsonPath("$.content[" + i + "].presidentName").value(results.get(i).getPresidentName()))
                    .andExpect(jsonPath("$.content[" + i + "].presidentContact").value(results.get(i).getPresidentContact()));
        }
        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].pendingClubId").type(WireFormat.FieldType.INT64).description("동아리 생성 요청 ID").attributes(example("1")));
        pageableResponseFields.add(fieldWithPath("content[].requestTo").type(WireFormat.FieldType.STRING).description("요청 수락 담당자 권한").attributes(example(LINK_NON_USER)));
        pageableResponseFields.add(fieldWithPath("content[].requestedAt").type(WireFormat.FieldType.STRING).description("요청 생성 시점").attributes(example("yyyy-MM-dd'T'HH:mm")));
        pageableResponseFields.add(fieldWithPath("content[].clubName").type(WireFormat.FieldType.STRING).description("희망 동아리 이름").attributes(example("생성하려는동아리123")));
        pageableResponseFields.add(fieldWithPath("content[].briefActivityDescription").type(WireFormat.FieldType.INT64).description("동아리 분류 - 세부 활동").attributes(example("박물관견학")));
        pageableResponseFields.add(fieldWithPath("content[].activityDescription").type(WireFormat.FieldType.STRING).description("동아리 활동 설명").attributes(example("우리는 xxx, xxxx를 합니다")));
        pageableResponseFields.add(fieldWithPath("content[].clubDescription").type(WireFormat.FieldType.BYTES).description("동아리 설명").attributes(example("우리는 xxx한 동아리입니다")));
        pageableResponseFields.add(fieldWithPath("content[].presidentName").type(WireFormat.FieldType.BYTES).description("신청자 이름").attributes(example("홍길동")));
        pageableResponseFields.add(fieldWithPath("content[].presidentContact").type(WireFormat.FieldType.BYTES).description("신청자 연락처").attributes(example("010-1234-1234")));
        addPageableResponseFields(pageableResponseFields);

        actions
                .andDo(document(
                                "pending/read",
                                queryParameters(
                                        parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                        parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                        parameterWithName("sort").optional().description("페이지 정보 - 정렬, 기본 ID (입력 순)").attributes(example(LINK_SORT_PENDING))
                                ),
                                responseFields(pageableResponseFields)
                        )
                );

    }

    @Test
    public void getPendings_LoginWithUser_InvalidApproachException() throws Exception{
        //given
        PageRequest pageRequest = PageRequest.of(1, 3, Sort.by("clubName").ascending());
        User user = new User("user", "mockUserPw", Role.ROLE_USER, "홍길동", "010-0000-0000");
        given(userRepository.findByUsername("user")).willReturn(user);

        //when
        MvcResult badAccessResult = mockMvc.perform(
                        get("/pending")
                                .queryParam("size", String.valueOf(pageRequest.getPageSize()))
                                .queryParam("page", String.valueOf(pageRequest.getOffset()))
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badAccessResult.getResolvedException()).isExactlyInstanceOf(InvalidApproachException.class);

    }
    
    @Test
    public void acceptPending_Default_Success() throws Exception{
        //given
        Role requestTo = Role.ROLE_ADMIN_SEOUL_CENTRAL;
        String clubName = "testPendingName";
        String briefActivityDescription = "testBriefDescription";
        String activityDescription = "testActivityDescription";
        String clubDescription = "testClubDescription";
        String username = "testUserId";
        String password = "testPw";
        String presidentName = "testUser";
        String presidentContact = "testContact";
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "스포츠";
        PendingClubRequest pendingClubRequest = new PendingClubRequest(
                requestTo,
                clubName,
                briefActivityDescription,
                activityDescription,
                clubDescription,
                username,
                password,
                presidentName,
                presidentContact
        );
        Long pendingClubId = 31L;
        String encodedPw = "AfterEncodedPw";
        doAnswer(
                invocation -> {
                    PendingClub pendingClub = pendingClubRequest.toEntity(encodedPw);
                    User user = pendingClub.toUser();
                    Club club = pendingClub.toClubWithDefaultLogo(campus, clubType, belongs, user);
                    Field declaredField = club.getClass().getDeclaredField("id");
                    declaredField.setAccessible(true);
                    declaredField.set(club, pendingClubId);
                    return Optional.of(club);
                }
        ).when(pendingClubService).acceptRequest(pendingClubId, campus, clubType, belongs);

        //when
        ResultActions actions = mockMvc.perform(
                delete("/pending/{pendingClubId}/accept", pendingClubId)
                        .with(csrf())
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", belongs)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(pendingClubId))
                .andExpect(jsonPath("$.clubName").value(clubName))
                .andExpect(jsonPath("$.campus").value(campus.toString()))
                .andExpect(jsonPath("$.clubType").value(clubType.toString()))
                .andExpect(jsonPath("$.belongs").value(belongs))
                .andExpect(jsonPath("$.briefActivityDescription").value(briefActivityDescription))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.presidentName").value(presidentName))
                .andDo(
                        document(
                                "pending/delete/accept",
                                pathParameters(
                                        parameterWithName("pendingClubId").description("지정한 생성 담당자").attributes(example(LINK_NON_USER))
                                ),
                                queryParameters(
                                        parameterWithName("campus").description("배정된 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                        parameterWithName("clubType").description("배정된 동아리 종류").attributes(example(LINK_CLUB_TYPE)),
                                        parameterWithName("belongs").description("배정된 분과").attributes(example(LINK_BELONGS_TYPE))
                                ),
                                responseFields(
                                        fieldWithPath("clubId").type(WireFormat.FieldType.INT64).description("생성된 동아리 ID").attributes(example("1")),
                                        fieldWithPath("clubName").type(WireFormat.FieldType.STRING).description("생성된 동아리 이름").attributes(example("생성된동아리")),
                                        fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("동아리 분류 - 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                        fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("동아리 분류 - 동아리 종류").attributes(example(LINK_CLUB_TYPE)),
                                        fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("동아리 분류 - 분과").attributes(example(LINK_BELONGS_TYPE)),
                                        fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("동아리 분류 - 세부 활동").attributes(example("박물관견학")),
                                        fieldWithPath("username").type(WireFormat.FieldType.STRING).description("동아리 유저 로그인 ID").attributes(example("ExampleId1412")),
                                        fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("회장 이름").attributes(example("홍길동"))
                                )
                        )
                );
    }
    
    @Test
    public void acceptPending_BadBelongs_InvalidBelongsException() throws Exception{
        //given
        Long pendingClubId = 123L;
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "건강체육";

        //when
        MvcResult badBelongsResult = mockMvc.perform(
                        delete("/pending/{pendingClubId}/accept", pendingClubId)
                                .with(csrf())
                                .queryParam("campus", campus.toString())
                                .queryParam("clubType", clubType.toString())
                                .queryParam("belongs", belongs)
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badBelongsResult.getResolvedException()).isExactlyInstanceOf(InvalidBelongsException.class);
        
    }
    
    @Test
    public void acceptPending_BadPendingClubId_PendingClubIdMisMatchException() throws Exception{
        //given
        Long pendingClubId = 123L;
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "스포츠";
        given(pendingClubService.acceptRequest(pendingClubId, campus, clubType, belongs)).willReturn(Optional.empty());

        //when
        MvcResult badBelongsResult = mockMvc.perform(
                        delete("/pending/{pendingClubId}/accept", pendingClubId)
                                .with(csrf())
                                .queryParam("campus", campus.toString())
                                .queryParam("clubType", clubType.toString())
                                .queryParam("belongs", belongs)
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badBelongsResult.getResolvedException()).isExactlyInstanceOf(PendingClubIdMisMatchException.class);

    }

    @Test
    public void denyPending_Default_Success() throws Exception{
        //given
        Long pendingClubId = 123L;
        Role requestTo = Role.ROLE_ADMIN_SEOUL_CENTRAL;
        String clubName = "testPendingName";
        String briefActivityDescription = "testBriefDescription";
        String activityDescription = "testActivityDescription";
        String clubDescription = "testClubDescription";
        String username = "testUserId";
        String password = "testPw";
        String presidentName = "testUser";
        String presidentContact = "testContact";
        PendingClubRequest pendingClubRequest = new PendingClubRequest(
                requestTo,
                clubName,
                briefActivityDescription,
                activityDescription,
                clubDescription,
                username,
                password,
                presidentName,
                presidentContact
        );
        String encodedPw = "AfterEncodedPw";
        doAnswer(
                invocation -> {
                    PendingClub pendingClub = pendingClubRequest.toEntity(encodedPw);
                    Field declaredField = pendingClub.getClass().getDeclaredField("id");
                    declaredField.setAccessible(true);
                    declaredField.set(pendingClub, pendingClubId);
                    Field timeField = pendingClub.getClass().getSuperclass().getDeclaredField("createdAt");
                    timeField.setAccessible(true);
                    timeField.set(pendingClub, LocalDateTime.now());
                    return Optional.of(pendingClub);
                }
        ).when(pendingClubService).denyRequest(pendingClubId);

        //when
        ResultActions actions = mockMvc.perform(
                delete("/pending/{pendingClubId}/deny", pendingClubId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingClubId").value(pendingClubId))
                .andExpect(jsonPath("$.requestTo").value(pendingClubRequest.getRequestTo().toString()))
                .andExpect(jsonPath("$.requestedAt").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString()))
                .andExpect(jsonPath("$.clubName").value(pendingClubRequest.getClubName()))
                .andExpect(jsonPath("$.briefActivityDescription").value(pendingClubRequest.getBriefActivityDescription()))
                .andExpect(jsonPath("$.activityDescription").value(pendingClubRequest.getActivityDescription()))
                .andExpect(jsonPath("$.clubDescription").value(pendingClubRequest.getClubDescription()))
                .andExpect(jsonPath("$.presidentName").value(pendingClubRequest.getPresidentName()))
                .andExpect(jsonPath("$.presidentContact").value(pendingClubRequest.getPresidentContact()))
                .andDo(
                        document(
                                "pending/delete/deny",
                                pathParameters(
                                        parameterWithName("pendingClubId").description("거절할 요청 ID").attributes(example("123"))
                                ),
                                responseFields(
                                        fieldWithPath("pendingClubId").type(WireFormat.FieldType.INT64).description("거절된 생성 요청 ID").attributes(example("1")),
                                        fieldWithPath("requestTo").type(WireFormat.FieldType.STRING).description("거절한 생성 담당자").attributes(example(LINK_NON_USER)),
                                        fieldWithPath("requestedAt").type(WireFormat.FieldType.STRING).description("요청 생성 시점").attributes(example("yyyy-MM-dd'T'HH:mm")),
                                        fieldWithPath("clubName").type(WireFormat.FieldType.STRING).description("거절된 희망 동아리 이름").attributes(example("생성하려는동아리")),
                                        fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("동아리 분류 - 세부 활동").attributes(example("박물관견학")),
                                        fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("거절된 동아리 활동 설명").attributes(example("우리는 xxx, xxxx를 합니다")),
                                        fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("거절된 동아리 설명").attributes(example("우리는 xxx한 동아리입니다")),
                                        fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("신청자 이름").attributes(example("홍길동")),
                                        fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("신청자 연락처").attributes(example("010-1234-1234"))
                                )
                        )
                );

    }

    @Test
    public void denyPending_BadPendingClubId_PendingClubIdMisMatchException() throws Exception{
        //given
        Long pendingClubId = 123L;
        given(pendingClubService.denyRequest(pendingClubId)).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                        delete("/pending/{pendingClubId}/deny", pendingClubId)
                                .with(csrf())
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(PendingClubIdMisMatchException.class);
    }
}