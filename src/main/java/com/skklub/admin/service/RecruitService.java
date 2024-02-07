package com.skklub.admin.service;

import com.skklub.admin.exception.deprecated.error.exception.AlreadyRecruitingException;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.NotRecruitingException;
import com.skklub.admin.exception.deprecated.error.exception.RecruitIdMisMatchException;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.RecruitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RecruitService {

    private final ClubRepository clubRepository;
    private final RecruitRepository recruitRepository;

    public Optional<String> startRecruit(Long clubId, Recruit recruit) throws AlreadyRecruitingException {
        return clubRepository.findById(clubId)
                .map(club -> {
                    if(club.onRecruit()) throw new AlreadyRecruitingException();
                    club.startRecruit(recruit);
                    recruitRepository.save(recruit);
                    return club.getName();
                });
    }


    public Optional<Long> updateRecruit(Long clubId, Recruit updateInfo) {
        return recruitRepository.findByClubId(clubId)
                .map(recruitBase -> {
                    recruitBase.update(updateInfo);
                    return clubId;
                });
    }

    public void endRecruit(Long clubId) throws RecruitIdMisMatchException, NotRecruitingException{
        clubRepository.findById(clubId)
                .ifPresentOrElse(
                        club -> {
                            if (!club.onRecruit()) throw new NotRecruitingException();
                            club.endRecruit();
                        },
                        () -> {
                            throw new ClubIdMisMatchException();
                        });
    }
}
