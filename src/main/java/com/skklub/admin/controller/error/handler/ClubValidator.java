package com.skklub.admin.controller.error.handler;

import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.controller.error.exception.AllTimeRecruitTimeFormattingException;
import com.skklub.admin.controller.error.exception.InvalidBelongsException;
import com.skklub.admin.domain.enums.BelongsSeoulCentral;
import com.skklub.admin.domain.enums.BelongsSuwonCentral;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.validation.Valid;

public interface ClubValidator {
    static void validateBelongs(Campus campus, ClubType clubType, String belongs) throws InvalidBelongsException {
        if (campus.equals(Campus.명륜) && clubType.equals(ClubType.중앙동아리)) {
            validateSeoulCentralClubBelongs(belongs);
            return;
        }
        if (campus.equals(Campus.율전) && clubType.equals(ClubType.중앙동아리)) {
            validateSuwonCentralClubBelongs(belongs);
            return;
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

    static void validateRecruitTimeFormat(@Valid RecruitDto recruit) throws AllTimeRecruitTimeFormattingException {
        if (recruit.getRecruitStartAt() == null ^ recruit.getRecruitEndAt() == null)
            throw new AllTimeRecruitTimeFormattingException();
    }
}
