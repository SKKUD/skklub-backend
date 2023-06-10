package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.dto.ClubPrevDTO;
import com.skklub.admin.service.dto.FileNames;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Component
public class ClubTestDataRepository {

    private final int clubCnt = 10;
    private final int activityImgPerClub = 5;
    private final List<Logo> logos = new ArrayList<>();
    private final List<ActivityImage> activityImages = new ArrayList<>();
    private final List<Club> clubs = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        readyLogo();
        readyActivityImages();
        readyClub();
    }

    public List<ClubPrevDTO> getClubPrevDTOs() {
        List<ClubPrevDTO> r = new ArrayList<>();
        for (int i = 0; i < clubs.size(); i++) {
            ClubPrevDTO prevDTO = ClubPrevDTO.fromEntity(clubs.get(i));
            prevDTO.setId((long) i);
            r.add(prevDTO);
        }
        return r;
    }

    public List<S3DownloadDto> getActivityImgS3DownloadDtos(int clubIndex) {
        List<ActivityImage> images = clubs.get(clubIndex).getActivityImages();
        List<S3DownloadDto> s3DownloadDtos = new ArrayList<>();
        for (int i = 0; i < activityImgPerClub; i++) {
            s3DownloadDtos.add(new S3DownloadDto((long) i, images.get(i).getOriginalName(), "activityImgBytes" + i));
        }
        return s3DownloadDtos;
    }

    public S3DownloadDto getLogoS3DownloadDto(int clubIndex) {
        return new S3DownloadDto(
                0L,
                clubs.get(clubIndex).getLogo().getOriginalName(),
                "logoBytes"
        );
    }

    public List<FileNames> getActivityImgFileNames(int clubIndex) {
        List<FileNames> fileNames = clubs.get(clubIndex).getActivityImages().stream()
                .map(FileNames::new)
                .collect(Collectors.toList());
        for(long i = 0; i < activityImgPerClub; i++)
            fileNames.get((int) i).setId(i);
        return fileNames;
    }

    public FileNames getLogoFileName(int clubIndex) {
        FileNames fileNames = new FileNames(clubs.get(clubIndex).getLogo());
        fileNames.setId(0L);
        return fileNames;
    }

    private void readyClub() {
        for (int i = 0; i < clubCnt; i++) {
            Club club = new Club(
                    "정상적인 클럽 SKKULOL" + i
                    , "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다."
                    , "취미교양"
                    , ClubType.중앙동아리
                    , "E-SPORTS"
                    , Campus.명륜
                    , "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^"
                    , "2023"
                    , "명륜 게임 동아리입니다"
                    , "4학기"
                    , 60
                    , "Thursday 19:00"
                    , "학생회관 80210"
                    , "www.skklol.com"
                    , "www.skkulol.edu");
            club.changeLogo(logos.get(i));
            List<ActivityImage> activityImagesTemp = new ArrayList<>();
            for (int j = 0; j < activityImgPerClub; j++) {
                activityImagesTemp.add(activityImages.get(j + i * activityImgPerClub));
            }
            club.appendActivityImages(activityImagesTemp);
            clubs.add(club);
        }
    }

    private void readyLogo() {
        for (int i = 0; i < clubCnt; i++) {
            logos.add(new Logo("logoOriginal" + i + ".png", "logoSaved" + i + ".png"));
        }
    }

    private void readyActivityImages() {
        for (int i = 0; i < clubCnt * activityImgPerClub; i++) {
            activityImages.add(new ActivityImage("activityOriginal" + i + ".png", "activitySaved" + i + ".png"));
        }
    }
}
