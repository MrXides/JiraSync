package com.rostelecom.jirasync.utils;

import com.rostelecom.jirasync.enums.IssueTypeId;

import java.util.Optional;

public class IssueTypeMapper {

    public static Long mapIHelp (Long id) {
        return Optional.ofNullable(IssueTypeId.getByIHelpId(id))
                .map(IssueTypeId::getOmnichatId)
                .orElseGet(IssueTypeId.TASK::getOmnichatId);
    }

    public static Long mapOmnichat (Long id) {
        return Optional.ofNullable(IssueTypeId.getByOmnichat(id))
                .map(IssueTypeId::getIHelpId)
                .orElseGet(IssueTypeId.TASK::getIHelpId);
    }
}