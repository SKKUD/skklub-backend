package com.skklub.admin.service;

import com.querydsl.core.group.GroupBy;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ActivityImageRepository;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.LogoRepository;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.ClubPrevDTO;
import com.skklub.admin.service.dto.FileNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
        club.changeLogo(logo);
        clubRepository.save(club);
        return club.getId();
    }

    public Optional<String> appendActivityImages(Long clubId, List<FileNames> activityImageDtos) {
        Optional<Club> club = clubRepository.findById(clubId);
        club.ifPresent(c -> {
                    List<ActivityImage> activityImages = activityImageDtos.stream()
                            .map(FileNames::toActivityImageEntity)
                            .collect(Collectors.toList());
                    activityImageRepository.saveAll(activityImages);
                    c.appendActivityImages(activityImages);
                }
        );
        return club.map(Club::getName);
    }

    public Optional<ClubDetailInfoDto> getClubDetailInfoById(Long clubId) {
        return clubRepository.findDetailClubById(clubId)
                .map(c -> new ClubDetailInfoDto(c, c.getLogo(), c.getActivityImages(), c.getRecruit(), c.getPresident()));
    }

    public Optional<ClubDetailInfoDto> getClubDetailInfoByName(String name) {
        return clubRepository.findDetailClubByName(name)
                .map(c -> new ClubDetailInfoDto(c, c.getLogo(), c.getActivityImages(), c.getRecruit(), c.getPresident()));
    }

    public Page<ClubPrevDTO> getClubPrevsByCategories(Campus campus, Optional<ClubType> clubType, Optional<String> belongs, Pageable pageable) {
        if (belongs.isPresent())
            return clubRepository.findClubPrevByCampusAndClubTypeAndBelongsOrderByName(campus, clubType.get(), belongs.get(), pageable).map(ClubPrevDTO::fromEntity);
        if (clubType.isPresent())
            return clubRepository.findClubPrevByCampusAndClubTypeOrderByName(campus, clubType.get(), pageable).map(ClubPrevDTO::fromEntity);
        return clubRepository.findClubPrevByCampusOrderByName(campus, pageable).map(ClubPrevDTO::fromEntity);
    }

    public Page<ClubPrevDTO> getClubPrevsByKeyword(String keyword, Pageable pageable) {
        return clubRepository.findClubPrevByNameContainingOrderByName(keyword, pageable).map(ClubPrevDTO::fromEntity);
    }

    public Optional<String> updateClub(Long clubId, ClubCreateRequestDTO clubCreateRequestDTO) {
        Club changeInfo = clubCreateRequestDTO.toEntity();
        return clubRepository.findDetailClubById(clubId)
                .map(club -> {
                    club.update(changeInfo);
                    return club.getName();
                });
    }

    public List<ClubPrevDTO> getRandomClubsByCategories(Campus campus, Optional<ClubType> clubType, Optional<String> belongs) {
        if(belongs.isPresent()) return convertEntityToPrevDTO(clubRepository.findClubRandomByCategories(campus.toString(), clubType.get().toString(), belongs.get()));
        if(clubType.isPresent()) return convertEntityToPrevDTO(clubRepository.findClubRandomByCategories(campus.toString(), clubType.get().toString()));
        return convertEntityToPrevDTO(clubRepository.findClubRandomByCategories(campus.toString()));
    }

    private List<ClubPrevDTO> convertEntityToPrevDTO(List<Club> clubRepository) {
        return clubRepository.stream().map(ClubPrevDTO::fromEntity).collect(Collectors.toList());
    }

    public Optional<String> deleteClub(Long clubId) {
        return clubRepository.findById(clubId)
                .map(club -> {
                    if(club.remove()) return club.getName();
                    return null;
                });
    }

    public Optional<String> reviveClub(Long clubId) {
        return clubRepository.findById(clubId)
                .map(club -> {
                    if(club.revive()) return club.getName();
                    return null;
                });
    }

    public Optional<String> deleteActivityImage(Long clubId, String activityImageName) {
        Optional<ActivityImage> activityImage = activityImageRepository.findByOriginalName(activityImageName);
        if(activityImage.isEmpty()) return Optional.empty();

        Optional<Club> club = clubRepository.findById(clubId);
        return club.map(c -> {
            c.removeActivityImages(activityImage.get());
            activityImageRepository.delete(activityImage.get());
            return activityImage.get().getUploadedName();
        });

    }
}
