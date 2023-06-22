package com.skklub.admin.service;

import com.skklub.admin.ClubTestDataRepository;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ActivityImageRepository;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.LogoRepository;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.awt.print.Pageable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;


@Slf4j
@Import(ClubTestDataRepository.class)
@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @InjectMocks
    private ClubTestDataRepository clubTestDataRepository;
    @InjectMocks
    private ClubService clubService;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private LogoRepository logoRepository;
    @Mock
    private ActivityImageRepository activityImageRepository;

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
        Club club = clubTestDataRepository.getClubs().get(clubIndex);
        Logo logo = clubTestDataRepository.getLogos().get(clubIndex);
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
        List<ActivityImage> activityImages = clubTestDataRepository.getActivityImgFileNames(clubIndex)
                .stream()
                .map(FileNames::toActivityImageEntity)
                .collect(Collectors.toList());
        activityImages.stream().forEach(a -> Assertions.assertThat(a.getId()).isNull());
        Club club = clubTestDataRepository.getClubs().get(clubIndex);
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
        List<ActivityImage> activityImages = clubTestDataRepository.getActivityImgFileNames(0)
                .stream()
                .map(FileNames::toActivityImageEntity)
                .collect(Collectors.toList());

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

        lenient().when(clubRepository.findClubByCampusAndClubTypeOrderByName(campus, clubType, request)).thenThrow(IllegalArgumentException.class);
        lenient().when(clubRepository.findClubByCampusOrderByName(campus, request)).thenThrow(IllegalArgumentException.class);

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

        lenient().when(clubRepository.findClubByCampusAndClubTypeAndBelongsOrderByName(campus, clubType, belongs, request)).thenThrow(AssertionError.class);
        lenient().when(clubRepository.findClubByCampusOrderByName(campus, request)).thenThrow(AssertionError.class);

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

        lenient().when(clubRepository.findClubByCampusAndClubTypeAndBelongsOrderByName(campus, clubType, belongs, request)).thenThrow(AssertionError.class);
        lenient().when(clubRepository.findClubByCampusAndClubTypeOrderByName(campus, clubType, request)).thenThrow(AssertionError.class);

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> clubService.getClubPrevsByCategories(campus, clubType, belongs, request));

        //then

    }


    @Test
    public void getClubPrevsByKeyword_belongsNot전체_3Param() throws Exception {
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
    public void getClubPrevsByKeyword_clubTypeNot전체AndbelongsIs전체_2Param() throws Exception {
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
    public void getClubPrevsByKeyword_Both전체_1Param() throws Exception {
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


    private <T> void setIdReflection(Long idVal, T obj) throws Exception {
        Field logoIdField = obj.getClass().getDeclaredField("id");
        logoIdField.setAccessible(true);
        logoIdField.set(obj, idVal);
    }
}