package com.skklub.admin.service;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.DeletedClub;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.exception.deprecated.error.exception.CannotDownGradeClubException;
import com.skklub.admin.exception.deprecated.error.exception.CannotUpGradeClubException;
import com.skklub.admin.repository.ActivityImageRepository;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.DeletedClubRepository;
import com.skklub.admin.repository.LogoRepository;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.skklub.admin.TestUtils.setIdReflection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@Slf4j
@Import(TestDataRepository.class)
@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @InjectMocks
    private TestDataRepository testDataRepository;
    @InjectMocks
    private ClubService clubService;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private LogoRepository logoRepository;
    @Mock
    private ActivityImageRepository activityImageRepository;
    @Mock
    private DeletedClubRepository deletedClubRepository;

    @AfterEach
    public void afterEach() {
        testDataRepository = new TestDataRepository();
    }

    /**
     * stub : save() id setting
     * check : relation mapping
     */
    @Test
    public void createClub_Default_Success() throws Exception {
        //given
        Long logoId = -9999L;
        Long clubId = -1111L;
        int clubIndex = 0;
        Club club = testDataRepository.getClubs().get(clubIndex);
        Logo logo = testDataRepository.getLogos().get(clubIndex);
        Assertions.assertThat(logo.getId()).isNull();
        doAnswer(invocation -> {
            setIdReflection(clubId, club);
            setIdReflection(logoId, logo);
            return null;
        }).when(clubRepository).save(club);


        //when
        Long returnId = clubService.createClub(club, logo);

        //then
        Assertions.assertThat(returnId).isEqualTo(clubId);
        Assertions.assertThat(club.getLogo().getId()).isEqualTo(logoId);
        Assertions.assertThat(club.getLogo()).isEqualTo(logo);
    }

    /**
     * @stub findById, saveAll id setting
     * @check return value, relation(id, count, names)
     */
    @Test
    public void appendActivityImages_Default_Success() throws Exception {
        //given
        Long clubId = 0L;
        int clubIndex = 0;
        List<ActivityImage> activityImages = testDataRepository.getActivityImgFileNames(clubIndex).stream().map(FileNames::toActivityImageEntity).collect(Collectors.toList());
        activityImages.stream().forEach(a -> Assertions.assertThat(a.getId()).isNull());
        Club club = testDataRepository.getClubs().get(clubIndex);
        club.getActivityImages().clear();
        setIdReflection(clubId, club);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        doAnswer(invocation -> {
            log.info("invocation : {}", invocation);
            for (long i = 0; i < activityImages.size(); i++)
                setIdReflection(i, activityImages.get((int) i));
            return null;
        }).when(activityImageRepository).saveAll(activityImages);

        //when
        Optional<String> clubName = clubService.appendActivityImages(clubId, activityImages);

        //then
        Assertions.assertThat(clubName.get()).isEqualTo(club.getName());
        Assertions.assertThat(club.getActivityImages()).hasSize(activityImages.size());
        for (long i = 0; i < activityImages.size(); i++) {
            ActivityImage activityImage = activityImages.get((int) i);
            ActivityImage clubActivityImg = club.getActivityImages().get((int) i);

            Assertions.assertThat(activityImage.getId()).isEqualTo(i);
            Assertions.assertThat(clubActivityImg.getOriginalName()).isEqualTo(activityImage.getOriginalName());
            Assertions.assertThat(clubActivityImg.getUploadedName()).isEqualTo(activityImage.getUploadedName());
            Assertions.assertThat(clubActivityImg.getClub()).isEqualTo(club);
        }
    }

    /**
     * @stub findById return null
     * @check activityImg no id(no saveAll Called), return Optional.empty
     */
    @Test
    public void appendActivityImages_ClubNotFound_ReturnEmpty() throws Exception {
        //given
        Long clubId = -1L;
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());
        List<ActivityImage> activityImages = testDataRepository.getActivityImgFileNames(0).stream().map(FileNames::toActivityImageEntity).collect(Collectors.toList());

        //when
        Optional<String> clubName = clubService.appendActivityImages(clubId, activityImages);

        //then
        activityImages.stream().forEach(a -> {
            Assertions.assertThat(a.getId()).isNull();
            Assertions.assertThat(a.getClub()).isNull();
        });
        Assertions.assertThat(clubName).isEmpty();
    }

    @Test
    public void getClubPrevsByCategories_belongsNot전체_fullQuery() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.준중앙동아리;
        String belongs = "취미교양";
        PageRequest request = PageRequest.of(0, 5, Sort.Direction.ASC, "name");

        lenient().when(clubRepository.findClubByCampusAndClubType(campus, clubType, request)).thenThrow(IllegalArgumentException.class);
        lenient().when(clubRepository.findClubByCampus(campus, request)).thenThrow(IllegalArgumentException.class);

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> clubService.getClubPrevsByCategories(campus, clubType, belongs, request));

    }

    @Test
    public void getClubPrevsByCategories_clubTypeNot전체AndbelongsIs전체_findClubByCampusAndClubTypeOrderByName() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.준중앙동아리;
        String belongs = "전체";
        PageRequest request = PageRequest.of(0, 5, Sort.Direction.ASC, "name");

        lenient().when(clubRepository.findClubByCampusAndClubTypeAndBelongs(campus, clubType, belongs, request)).thenThrow(AssertionError.class);
        lenient().when(clubRepository.findClubByCampus(campus, request)).thenThrow(AssertionError.class);

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> clubService.getClubPrevsByCategories(campus, clubType, belongs, request));

        //then

    }

    @Test
    public void getClubPrevsByCategories_Both전체_findClubByCampusOrderByName() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "전체";
        PageRequest request = PageRequest.of(0, 5, Sort.Direction.ASC, "name");

        lenient().when(clubRepository.findClubByCampusAndClubTypeAndBelongs(campus, clubType, belongs, request)).thenThrow(AssertionError.class);
        lenient().when(clubRepository.findClubByCampusAndClubType(campus, clubType, request)).thenThrow(AssertionError.class);

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> clubService.getClubPrevsByCategories(campus, clubType, belongs, request));

        //then

    }

    @Test
    public void getRandomClubsByCategories_belongsNot전체_3Param() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.준중앙동아리;
        String belongs = "취미교양";

        lenient().when(clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString())).thenThrow(IllegalArgumentException.class);
        lenient().when(clubRepository.findClubRandomByCategories(campus.toString())).thenThrow(IllegalArgumentException.class);

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> clubService.getRandomClubsByCategories(campus, clubType, belongs));

    }

    @Test
    public void getRandomClubsByCategories_clubTypeNot전체AndbelongsIs전체_2Param() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.준중앙동아리;
        String belongs = "전체";

        lenient().when(clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString(), belongs)).thenThrow(AssertionError.class);
        lenient().when(clubRepository.findClubRandomByCategories(campus.toString())).thenThrow(AssertionError.class);

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> clubService.getRandomClubsByCategories(campus, clubType, belongs));

        //then

    }

    @Test
    public void getRandomClubsByCategories_Both전체_1Param() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "전체";

        lenient().when(clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString(), belongs)).thenThrow(AssertionError.class);
        lenient().when(clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString())).thenThrow(AssertionError.class);

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> clubService.getRandomClubsByCategories(campus, clubType, belongs));

        //then

    }

    @Test
    public void updateClub_Default_SuccessAndKeepRelationsAndAliveness() throws Exception {
        //given
        Long clubId = 0L;
        Long updateInfoClubId = 1L;
        Club club = testDataRepository.getClubs().get(clubId.intValue());
        Club clubUpdateInfo = testDataRepository.getClubs().get(updateInfoClubId.intValue());
        setIdReflection(clubId, club);
        ClubDetailInfoDto base = new ClubDetailInfoDto(club);
        setIdReflection(clubId, base);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        Optional<String> updatedName = clubService.updateClub(clubId, clubUpdateInfo);

        //then
        Assertions.assertThat(club.getName()).isEqualTo(updatedName.get()).isEqualTo(clubUpdateInfo.getName());
        Assertions.assertThat(club.getActivityDescription()).isEqualTo(clubUpdateInfo.getActivityDescription());
        Assertions.assertThat(club.getCampus()).isEqualTo(club.getCampus());
        Assertions.assertThat(club.getClubType()).isEqualTo(club.getClubType());
        Assertions.assertThat(club.getBelongs()).isEqualTo(club.getBelongs());
        Assertions.assertThat(club.getBriefActivityDescription()).isEqualTo(clubUpdateInfo.getBriefActivityDescription());
        Assertions.assertThat(club.getClubDescription()).isEqualTo(clubUpdateInfo.getClubDescription());
        Assertions.assertThat(club.getEstablishAt()).isEqualTo(clubUpdateInfo.getEstablishAt());
        Assertions.assertThat(club.getHeadLine()).isEqualTo(clubUpdateInfo.getHeadLine());
        Assertions.assertThat(club.getMandatoryActivatePeriod()).isEqualTo(clubUpdateInfo.getMandatoryActivatePeriod());
        Assertions.assertThat(club.getMemberAmount()).isEqualTo(clubUpdateInfo.getMemberAmount());
        Assertions.assertThat(club.getLogo()).isEqualTo(base.getLogo().toLogoEntity()).isNotEqualTo(clubUpdateInfo.getLogo());
        Assertions.assertThat(club.getActivityImages().size()).isEqualTo(base.getActivityImages().size());
        Assertions.assertThat(club.getActivityImages()).isEqualTo(base.getActivityImages().stream().map(FileNames::toActivityImageEntity).collect(Collectors.toList())).isNotEqualTo(clubUpdateInfo.getActivityImages());
        Assertions.assertThat(club.getPresident()).isNotEqualTo(clubUpdateInfo.getPresident());
        Assertions.assertThat(club.getPresident().getName()).isEqualTo(base.getPresidentName());
        Assertions.assertThat(club.getPresident().getContact()).isEqualTo(base.getPresidentContact());
        Assertions.assertThat(new RecruitDto(club.getRecruit())).isEqualTo(base.getRecruit().get()).isNotEqualTo(new RecruitDto(clubUpdateInfo.getRecruit()));
    }

    @Test
    public void updateClub_badClubId_ReturnOptionalEmpty() throws Exception {
        //given
        Long clubId = 0L;
        Long updateInfoClubId = 1L;
        Club clubUpdateInfo = testDataRepository.getClubs().get(updateInfoClubId.intValue());
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        //when
        Optional<String> NameShouldNull = clubService.updateClub(clubId, clubUpdateInfo);

        //then
        Assertions.assertThat(NameShouldNull).isEmpty();
    }

    @Test
    public void updateLogo_Default_ChangeOnlyNames() throws Exception {
        //given
        Long clubId = 0L;
        Logo logo = testDataRepository.getClubs().get(clubId.intValue()).getLogo();
        Logo baseLogo = new Logo(logo.getOriginalName(), logo.getUploadedName());
        setIdReflection(clubId, logo);
        setIdReflection(clubId, baseLogo);
        Long updateLogoInfoClubId = 1L;
        Logo logoUpdateInfo = testDataRepository.getClubs().get(updateLogoInfoClubId.intValue()).getLogo();
        given(logoRepository.findByClubId(clubId)).willReturn(Optional.ofNullable(logo));

        //when
        Optional<String> oldSavedName = clubService.updateLogo(clubId, logoUpdateInfo);

        //then
        Assertions.assertThat(baseLogo.getId()).isEqualTo(logo.getId()).isNotEqualTo(logoUpdateInfo.getId());
        Assertions.assertThat(logo.getOriginalName()).isEqualTo(logoUpdateInfo.getOriginalName()).isNotEqualTo(baseLogo.getOriginalName());
        Assertions.assertThat(logo.getUploadedName()).isEqualTo(logoUpdateInfo.getUploadedName()).isNotEqualTo(baseLogo.getUploadedName());
        Assertions.assertThat(oldSavedName).isNotEmpty();
        Assertions.assertThat(oldSavedName.get()).isEqualTo(baseLogo.getUploadedName());
    }

    @Test
    public void updateLogo_badClubId_ReturnOptionalEmpty() throws Exception {
        //given
        Long clubId = 0L;
        given(logoRepository.findByClubId(clubId)).willReturn(Optional.empty());
        Logo logoUpdateInfo = testDataRepository.getClubs().get(clubId.intValue()).getLogo();

        //when
        Optional<String> nameShouldEmpty = clubService.updateLogo(clubId, logoUpdateInfo);

        //then
        Assertions.assertThat(nameShouldEmpty).isEmpty();
    }

    @Test
    public void deleteClub_Default_ReturnName() throws Exception {
        //given
        Long clubId = 0L;
        Club club = testDataRepository.getClubs().get(clubId.intValue());
        given(clubRepository.findById(clubId)).willReturn(Optional.ofNullable(club));

        //when
        Optional<String> deletedClubName = clubService.deleteClub(clubId);

        //then
        Assertions.assertThat(deletedClubName).isNotEmpty();
        Assertions.assertThat(deletedClubName.get()).isEqualTo(club.getName());
    }

    @Test
    public void deleteClub_AlreadyRemoved_ReturnEmpty() throws Exception {
        //given
        Long clubId = 0L;
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        //when
        Optional<String> shouldBeEmpty = clubService.deleteClub(clubId);

        //then
        Assertions.assertThat(shouldBeEmpty).isEmpty();
    }

    @Test
    public void reviveClub_Default_ReturnName() throws Exception {
        //given
        Long clubId = 0L;
        Constructor<DeletedClub> constructor = DeletedClub.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DeletedClub deletedClub = constructor.newInstance();

        Field declaredField = deletedClub.getClass().getDeclaredField("name");
        declaredField.setAccessible(true);
        declaredField.set(deletedClub, "testName");
        given(deletedClubRepository.findById(clubId)).willReturn(Optional.of(deletedClub));
        doNothing().when(deletedClubRepository).delete(deletedClub);

        //when
        Optional<String> reviveClubName = clubService.reviveClub(clubId);

        //then
        Assertions.assertThat(reviveClubName).isNotEmpty();
        Assertions.assertThat(reviveClubName.get()).isEqualTo("testName");
    }

    @Test
    public void reviveClub_BadClubId_ReturnOptionalEmtpy() throws Exception {
        //given
        Long clubId = 0L;
        lenient().when(deletedClubRepository.findById(clubId)).thenReturn(Optional.empty());

        //when
        Optional<String> nameShouldEmpty = clubService.reviveClub(clubId);

        //then
        Assertions.assertThat(nameShouldEmpty).isEmpty();
    }

    @Test
    public void deleteActivityImage_Default_ReduceClubActivityImgsSizeAndNotFound() throws Exception {
        //given
        Long clubId = 0L;
        Club club = testDataRepository.getClubs().get(clubId.intValue());
        int activityImageIndex = 0;
        ActivityImage activityImage = club.getActivityImages().get(activityImageIndex);
        int size = club.getActivityImages().size();
        setIdReflection(0L, activityImage);
        given(activityImageRepository.findByClubIdAndOriginalName(clubId, activityImage.getOriginalName())).willReturn(Optional.of(activityImage));
        doAnswer(invocation -> {
            club.getActivityImages().remove(activityImage);
            return null;
        }).when(activityImageRepository).delete(activityImage);

        //when
        Optional<String> deletedActivityImageOriginalName = clubService.deleteActivityImage(clubId, activityImage.getOriginalName());

        //then
        Assertions.assertThat(deletedActivityImageOriginalName).isNotEmpty();
        Assertions.assertThat(deletedActivityImageOriginalName.get()).isEqualTo(activityImage.getUploadedName());
        Assertions.assertThat(club.getActivityImages().size()).isEqualTo(size - 1);
        Assertions.assertThat(club.getActivityImages().contains(activityImage)).isFalse();
    }

    @Test
    public void deleteActivityImage_badClubIdOrNoMatchAcImgName_ReturnOptionalEmpty() throws Exception {
        //given
        Long clubId = 0L;
        String badActivityImgName = "badActivityImgName";
        given(activityImageRepository.findByClubIdAndOriginalName(clubId, badActivityImgName)).willReturn(Optional.empty());

        //when
        Optional<String> nameShouldEmpty = clubService.deleteActivityImage(clubId, badActivityImgName);

        //then
        Assertions.assertThat(nameShouldEmpty).isEmpty();
    }

    @Test
    public void downGrade_Given중앙동아리_ChangeTo준중앙동아리() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(0);
        changeClubType(club, ClubType.중앙동아리);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        Optional<Club> clubAfterDownGrade = clubService.downGrade(clubId);

        //then
        Assertions.assertThat(clubAfterDownGrade).isNotEmpty();
        Assertions.assertThat(clubAfterDownGrade.get().getClubType()).isEqualTo(ClubType.준중앙동아리);
    }

    @Test
    public void downGrade_Given준중앙동아리_CannotDownGradeClubException() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(0);
        changeClubType(club, ClubType.준중앙동아리);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        org.junit.jupiter.api.Assertions.assertThrows(
                CannotDownGradeClubException.class,
                () -> clubService.downGrade(clubId)
        );

        //then
    }

    @Test
    public void downGrade_Given기타동아리_CannotDownGradeClubException() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(0);
        changeClubType(club, ClubType.기타동아리);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        org.junit.jupiter.api.Assertions.assertThrows(
                CannotDownGradeClubException.class,
                () -> clubService.downGrade(clubId)
        );

    }

    @Test
    public void upGrade_Given준중앙동아리_ChangeTo중앙동아리() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(0);
        changeClubType(club, ClubType.준중앙동아리);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        Optional<Club> clubAfterDownGrade = clubService.upGrade(clubId);

        //then
        Assertions.assertThat(clubAfterDownGrade).isNotEmpty();
        Assertions.assertThat(clubAfterDownGrade.get().getClubType()).isEqualTo(ClubType.중앙동아리);
    }

    @Test
    public void upGrade_Given중앙동아리_CannotUpGradeClubException() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(0);
        changeClubType(club, ClubType.중앙동아리);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        org.junit.jupiter.api.Assertions.assertThrows(
                CannotUpGradeClubException.class,
                () -> clubService.upGrade(clubId)
        );

        //then

    }

    @Test
    public void upGrade_Given기타동아리_CannotUpGradeClubException() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(0);
        changeClubType(club, ClubType.기타동아리);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        //when
        org.junit.jupiter.api.Assertions.assertThrows(
                CannotUpGradeClubException.class,
                () -> clubService.upGrade(clubId)
        );
        //then

    }

    private void changeClubType(Club club, ClubType clubType) throws NoSuchFieldException, IllegalAccessException {
        Field clubTypeField = club.getClass().getDeclaredField("clubType");
        clubTypeField.setAccessible(true);
        clubTypeField.set(club, clubType);
    }
}