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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public Page<Club> getClubPrevsByCategories(Campus campus, ClubType clubType, String belongs, Pageable pageable) {
        if (!belongs.equals("전체"))
            return clubRepository.findClubByCampusAndClubTypeAndBelongsOrderByName(campus, clubType, belongs, pageable);
        if (!clubType.equals(ClubType.전체))
            return clubRepository.findClubByCampusAndClubTypeOrderByName(campus, clubType, pageable);
        return clubRepository.findClubByCampusOrderByName(campus, pageable);
    }

    public List<Club> getRandomClubsByCategories(Campus campus, ClubType clubType, String belongs) {
        if (!belongs.equals("전체"))
            return clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString(), belongs);
        if (!clubType.equals(ClubType.전체))
            return clubRepository.findClubRandomByCategories(campus.toString(), clubType.toString());
        return clubRepository.findClubRandomByCategories(campus.toString());
    }

    public Optional<String> updateClub(Long clubId, Club clubUpdateInfo) {
        return clubRepository.findById(clubId)
                .map(baseClub -> {
                    baseClub.update(clubUpdateInfo);
                    return baseClub.getName();
                });
    }

    public Optional<String> updateLogo(Long clubId, Logo logoUpdateInfo) {
        return logoRepository.findByClubId(clubId)
                .map(baseLogo -> {
                    String oldUploadedName = baseLogo.getUploadedName();
                    baseLogo.update(logoUpdateInfo);
                    return oldUploadedName;
                });
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
        return activityImageRepository.findByClubIdAndOriginalName(clubId, activityImageName)
                .map(img -> {
                    activityImageRepository.delete(img);
                    return img.getUploadedName();
                });
    }

}
