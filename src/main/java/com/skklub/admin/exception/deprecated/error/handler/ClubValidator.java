package com.skklub.admin.exception.deprecated.error.handler;

import com.skklub.admin.controller.dto.RecruitRequest;
import com.skklub.admin.exception.deprecated.error.exception.AllTimeRecruitTimeFormattingException;
import com.skklub.admin.exception.deprecated.error.exception.InvalidBelongsException;
import com.skklub.admin.domain.enums.BelongsSeoulCentral;
import com.skklub.admin.domain.enums.BelongsSuwonCentral;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;

public interface ClubValidator {
    static void validateBelongs(Campus campus, ClubType clubType, String belongs) throws InvalidBelongsException {
        if(clubType.equals(ClubType.중앙동아리) || clubType.equals(ClubType.준중앙동아리))
            switch (campus) {
                case 명륜 -> validateSeoulCentralClubBelongs(belongs);
                case 율전 -> validateSuwonCentralClubBelongs(belongs);
            }
    }

    private static void validateSuwonCentralClubBelongs(String belongs) {
        try {
            BelongsSuwonCentral.valueOf(belongs);
        } catch (IllegalArgumentException e) {
            throw new InvalidBelongsException();
        }
    }

    private static void validateSeoulCentralClubBelongs(String belongs) throws InvalidBelongsException{
        try {
            BelongsSeoulCentral.valueOf(belongs);
        } catch (IllegalArgumentException e) {
            throw new InvalidBelongsException();
        }
    }

    static void validateRecruitTimeFormat(RecruitRequest recruit) throws AllTimeRecruitTimeFormattingException {
        if (recruit.getRecruitEndAt() == null ^ recruit.getRecruitStartAt() == null)
            throw new AllTimeRecruitTimeFormattingException();
    }
}
