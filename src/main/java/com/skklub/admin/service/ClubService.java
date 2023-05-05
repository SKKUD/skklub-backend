package com.skklub.admin.service;

import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ActivityImageRepository;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.LogoRepository;
import com.skklub.admin.repository.dto.ClubPrevDTO;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClubService {

    public final ClubRepository clubRepository;
    public final LogoRepository logoRepository;
    public final ActivityImageRepository activityImageRepository;

    public Long createClub(Club club, String logoOriginalName, String logoSavedName) {
        Logo logo = new Logo(logoOriginalName, logoSavedName);
        logoRepository.save(logo);
        club.matchLogo(logo);
        clubRepository.save(club);
        return club.getId();
    }

    public String appendActivityImages(Long clubId, List<FileNames> activityImageDtos) {
        Club club = clubRepository.findById(clubId).get();
        List<ActivityImage> activityImages = activityImageDtos.stream()
                .map(FileNames::toActivityImageEntity)
                .collect(Collectors.toList());
        activityImageRepository.saveAll(activityImages);
        club.appendActivityImages(activityImages);
        return club.getName();
    }

    public ClubDetailInfoDto getClubDetailInfo(Long clubId) {
        Club club = clubRepository.findDetailClubById(clubId).get();
        log.info("=========================================");
        return new ClubDetailInfoDto(club, club.getLogo(), club.getActivityImages(), club.getRecruit(), club.getPresident());
    }

    public Page<ClubPrevDTO> getClubPrevs(Campus campus, ClubType clubType, String belongs, Pageable pageable) {
        if(belongs.equals("전체")) return clubRepository.findClubPrevs(campus, clubType, pageable);
        if(clubType.equals("전체")) return clubRepository.findClubPrevs(campus, pageable);
        return clubRepository.findClubPrevs(campus, clubType, belongs, pageable);
    }
}
