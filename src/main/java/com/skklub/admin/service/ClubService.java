package com.skklub.admin.service;

import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.exception.deprecated.error.exception.CannotUpGradeClubException;
import com.skklub.admin.exception.deprecated.error.exception.CannotDownGradeClubException;
import com.skklub.admin.exception.deprecated.error.exception.MissingDeletedClubException;
import com.skklub.admin.exception.deprecated.error.exception.MissingAliveClubException;
import com.skklub.admin.repository.ActivityImageRepository;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.DeletedClubRepository;
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

    private final ClubRepository clubRepository;
    private final LogoRepository logoRepository;
    private final ActivityImageRepository activityImageRepository;
    private final DeletedClubRepository deletedClubRepository;


    public Long createClub(Club club, Logo logo) {
        club.changeLogo(logo);
        clubRepository.save(club);
        return club.getId();
    }

    public Optional<String> appendActivityImages(Long clubId, List<ActivityImage> activityImages) {
        return clubRepository.findById(clubId)
                .map(c -> {
                    c.appendActivityImages(activityImages);
                    activityImageRepository.saveAll(activityImages);
                    return c.getName();
                }
        );
    }

    public Page<Club> getClubPrevsByCategories(Campus campus, ClubType clubType, String belongs, Pageable pageable) {
        if (!belongs.equals("전체"))
            return clubRepository.findClubByCampusAndClubTypeAndBelongs(campus, clubType, belongs, pageable);
        if (!clubType.equals(ClubType.전체))
            return clubRepository.findClubByCampusAndClubType(campus, clubType, pageable);
        return clubRepository.findClubByCampus(campus, pageable);
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

    public Optional<String> deleteClub(Long clubId) throws MissingAliveClubException {
        return clubRepository.findById(clubId)
                .map(club -> {
                    clubRepository.delete(club);
                    return club.getName();
                });
    }

    public Optional<String> reviveClub(Long clubId) throws MissingDeletedClubException {
        return deletedClubRepository.findById(clubId)
                .map(club -> {
                    deletedClubRepository.delete(club);
                    return club.getName();
                });
    }

    public Optional<String> deleteActivityImage(Long clubId, String activityImageName) {
        return activityImageRepository.findByClubIdAndOriginalName(clubId, activityImageName)
                .map(img -> {
                    activityImageRepository.delete(img);
                    return img.getUploadedName();
                });
    }

    public Optional<Club> downGrade(Long clubId) {
        return clubRepository.findById(clubId)
                .map(
                        club -> {
                            if(!club.downGrade()) throw new CannotDownGradeClubException();
                            return club;
                        }
                );
    }

    public Optional<Club> upGrade(Long clubId) {
        return clubRepository.findById(clubId)
                .map(
                        club -> {
                            if (!club.upGrade()) throw new CannotUpGradeClubException();
                            return club;
                        }
                );
    }
}
