package com.rostelecom.jirasync.utils;

import com.rostelecom.jirasync.enums.IssueStatusId;
import com.rostelecom.jirasync.enums.IssueTypeId;

import java.util.Optional;

public class IssueStatusIdMapper {
    public static Long mapIHelp (Long id) {
        return Optional.ofNullable(IssueStatusId.getByIHelpStatusId(id))
                .map(IssueStatusId::getOmnichatStatusId)
                .orElseGet(IssueStatusId.TODO::getOmnichatStatusId);
    }

    public static Long mapOmnichat (Long id) {
        return Optional.ofNullable(IssueStatusId.getByOmnichat(id))
                .map(IssueStatusId::getIHelpStatusId)
                .orElseGet(IssueStatusId.TODO::getIHelpStatusId);
    }
}
