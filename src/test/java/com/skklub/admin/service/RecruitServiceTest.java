package com.skklub.admin.service;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.exception.deprecated.error.exception.AlreadyRecruitingException;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.RecruitRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Field;
import java.util.Optional;

import static com.skklub.admin.TestUtils.setIdReflection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;

@Slf4j
@ExtendWith(MockitoExtension.class)
@Import(TestDataRepository.class)
class RecruitServiceTest {
    @InjectMocks
    private RecruitService recruitService;
    @InjectMocks
    private TestDataRepository testDataRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private RecruitRepository recruitRepository;

    @AfterEach
    public void afterEach() {
        testDataRepository = new TestDataRepository();
    }

    @Test
    public void startRecruit_Default_ClubRelationAndRecruitId() throws Exception {
        //given
        Long clubId = 0L;
        Long recruitId = 9999L;
        Club club = testDataRepository.getClubs().get(clubId.intValue());
        Field recruitField = club.getClass().getDeclaredField("recruit");
        recruitField.setAccessible(true);
        recruitField.set(club, null);
        Assertions.assertThat(club.getRecruit()).isNull();
        Recruit recruit = testDataRepository.getRecruits().get(0);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        doAnswer(invocation -> {
            setIdReflection(recruitId, recruit);
            return null;
        }).when(recruitRepository).save(recruit);

        //when
        Optional<String> clubName = recruitService.startRecruit(clubId, recruit);

        //then
        Assertions.assertThat(clubName).isNotEmpty();
        Assertions.assertThat(clubName.get()).isEqualTo(club.getName());
        Assertions.assertThat(club.getRecruit()).isNotNull();
        Assertions.assertThat(club.getRecruit()).isEqualTo(recruit);
        Assertions.assertThat(club.getRecruit().getId()).isEqualTo(recruitId);
    }

    @Test
    public void startRecruit_BadClubId_ReturnOptionalEmpty() throws Exception {
        //given
        Long clubId = -1L;
        Recruit recruit = testDataRepository.getRecruits().get(0);
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());


        //when
        Optional<String> nameShouldNull = recruitService.startRecruit(clubId, recruit);

        //
        Assertions.assertThat(nameShouldNull).isEmpty();
    }

    @Test
    public void startRecruit_AlreadyRecruiting_AlreadyRecruitingException() throws Exception {
        //given
        Long clubId = 0L;
        Club club = testDataRepository.getClubs().get(clubId.intValue());
        Assertions.assertThat(club.getRecruit()).isNotNull();
        Long recruitId = 2L;
        Recruit recruit = testDataRepository.getRecruits().get(recruitId.intValue());
        Assertions.assertThat(club.getRecruit()).isNotEqualTo(recruit);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        org.junit.jupiter.api.Assertions.assertThrows(AlreadyRecruitingException.class, () -> recruitService.startRecruit(clubId, recruit));
        //then
    }

    @Test
    public void updateRecruit_Default_ChangedExceptId() throws Exception {
        //given
        Long baseRecruitId = 0L;
        Long updateRecruitInfoId = 1L;
        Recruit baseRecruit = testDataRepository.getRecruits().get(baseRecruitId.intValue());
        setIdReflection(baseRecruitId, baseRecruit);
        Recruit updateRecruitInfo = testDataRepository.getRecruits().get(updateRecruitInfoId.intValue());
        setIdReflection(null, updateRecruitInfo);
        given(recruitRepository.findByClubId(baseRecruitId)).willReturn(Optional.ofNullable(baseRecruit));

        //when
        Optional<Long> recruitId = recruitService.updateRecruit(baseRecruitId, updateRecruitInfo);

        //then
        Assertions.assertThat(recruitId).isNotEmpty();
        Assertions.assertThat(recruitId.get()).isEqualTo(baseRecruit.getId()).isNotEqualTo(updateRecruitInfo.getId());
        Assertions.assertThat(baseRecruit.getStartAt()).isEqualTo(updateRecruitInfo.getStartAt());
        Assertions.assertThat(baseRecruit.getEndAt()).isEqualTo(updateRecruitInfo.getEndAt());
        Assertions.assertThat(baseRecruit.getQuota()).isEqualTo(updateRecruitInfo.getQuota());
        Assertions.assertThat(baseRecruit.getProcessDescription()).isEqualTo(updateRecruitInfo.getProcessDescription());
        Assertions.assertThat(baseRecruit.getContact()).isEqualTo(updateRecruitInfo.getContact());
        Assertions.assertThat(baseRecruit.getWebLink()).isEqualTo(updateRecruitInfo.getWebLink());
    }

    @Test
    public void updateRecruit_BadClubId_ReturnOptionalEmpty() throws Exception{
        //given
        Long clubId = -1L;
        given(recruitRepository.findByClubId(clubId)).willReturn(Optional.empty());

        //when
        Optional<Long> idShouldNull = recruitService.updateRecruit(clubId, null);

        //then
        Assertions.assertThat(idShouldNull).isEmpty();
    }

    @Test
    public void endRecruit_BadRecruitId_ClubIdMisMatchException() throws Exception{
        //given
        Long recruitId = -1L;
        lenient().when(recruitRepository.findById(recruitId)).thenReturn(Optional.empty());

        //when
        org.junit.jupiter.api.Assertions.assertThrows(ClubIdMisMatchException.class,
                () -> recruitService.endRecruit(recruitId));

        //then

    }
}