package com.skklub.admin.service;

import com.querydsl.core.group.GroupBy;
import com.skklub.admin.controller.error.exception.AlreadyRecruitingException;
import com.skklub.admin.controller.error.exception.NotRecruitingException;
import com.skklub.admin.controller.error.exception.RecruitIdMisMatchException;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.RecruitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitService {

    private final ClubRepository clubRepository;
    private final RecruitRepository recruitRepository;

    public Optional<String> startRecruit(Long clubId, Recruit recruit) throws AlreadyRecruitingException {
        return clubRepository.findById(clubId)
                .map(club -> {
                    if(club.onRecruit()) throw new AlreadyRecruitingException();
                    recruitRepository.save(recruit);
                    club.startRecruit(recruit);
                    return club.getName();
                });
    }


    public Optional<Long> updateRecruit(Long recruitId, Recruit recruit) {
        return recruitRepository.findById(recruitId)
                .map(recruitBase -> {
                    recruitBase.update(recruit);
                    return recruit.getId();
                });
    }

    public void endRecruit(Long recruitId) throws RecruitIdMisMatchException{
        recruitRepository.findById(recruitId)
                .ifPresentOrElse(recruitRepository::delete, RecruitIdMisMatchException::new);
    }
}
