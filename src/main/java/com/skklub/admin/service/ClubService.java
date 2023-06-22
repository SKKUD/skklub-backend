package com.skklub.admin.service;

import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.error.exception.AlreadyAliveClubException;
import com.skklub.admin.error.exception.DoubleClubDeletionException;
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

    public Long createClub(Club club, Logo logo) {
        club.changeLogo(logo);
        clubRepository.save(club);
        return club.getId();
    }

    public Optional<String> appendActivityImages(Long clubId, List<ActivityImage> activityImages) {
        return clubRepository.findById(clubId)
                .map(c -> {
                    activityImageRepository.saveAll(activityImages);
                    c.appendActivityImages(activityImages);
                    return c.getName();
                }
        );
    }

    public Optional<ClubDetailInfoDto> getClubDetailInfoById(Long clubId) {
        return clubRepository.findDetailClubById(clubId)
                .map(ClubDetailInfoDto::new);
    }

    public Optional<ClubDetailInfoDto> getClubDetailInfoByName(String name) {
        return clubRepository.findDetailClubByName(name)
                .map(c -> new ClubDetailInfoDto(c));
    }

    public Page<ClubPrevDTO> getClubPrevsByCategories(Campus campus, ClubType clubType, String belongs, Pageable pageable) {
        if (!belongs.equals("전체"))
            return clubRepository.findClubPrevByCampusAndClubTypeAndBelongsOrderByName(campus, clubType, belongs, pageable).map(ClubPrevDTO::fromEntity);
        if (!clubType.equals(ClubType.전체))
            return clubRepository.findClubPrevByCampusAndClubTypeOrderByName(campus, clubType, pageable).map(ClubPrevDTO::fromEntity);
        return clubRepository.findClubPrevByCampusOrderByName(campus, pageable).map(ClubPrevDTO::fromEntity);
    }

    public Page<ClubPrevDTO> getClubPrevsByKeyword(String keyword, Pageable pageable) {
        return clubRepository.findClubPrevByNameContainingOrderByName(keyword, pageable).map(ClubPrevDTO::fromEntity);
    }

    public Optional<String> updateClub(Long clubId, Club club) {
        return clubRepository.findDetailClubById(clubId)
                .map(base -> {
                    base.update(club);
                    return base.getName();
                });
    }

    public List<ClubPrevDTO> getRandomClubsByCategories(Campus campus, ClubType clubType, String belongs) {
        List<Club> clubs;
        if (!belongs.equals("전체"))
            clubs = clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString(), belongs);
        else if (!clubType.equals(ClubType.전체))
            clubs = clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString());
        else clubs = clubRepository.findClubRandomByCategories(campus.toString());
        return clubs.stream()
                .map(ClubPrevDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<String> deleteClub(Long clubId) throws DoubleClubDeletionException {
        return clubRepository.findById(clubId)
                .map(club -> {
                    if (club.remove()) return club.getName();
                    throw new DoubleClubDeletionException();
                });
    }

    public Optional<String> reviveClub(Long clubId) throws AlreadyAliveClubException {
        return clubRepository.findById(clubId)
                .map(club -> {
                    if (club.revive()) return club.getName();
                    throw new AlreadyAliveClubException();
                });
    }

    public Optional<String> deleteActivityImage(Long clubId, String activityImageName) {
        return activityImageRepository.findByOriginalNameAndClubId(activityImageName, clubId)
                .map(img -> {
                    activityImageRepository.delete(img);
                    return img.getUploadedName();
                });
    }

    public Optional<String> updateLogo(Long clubId, FileNames fileNames) {
        return logoRepository.findByClubId(clubId)
                .map(logo -> logo.update(fileNames.getOriginalName(), fileNames.getSavedName()));
    }

}
