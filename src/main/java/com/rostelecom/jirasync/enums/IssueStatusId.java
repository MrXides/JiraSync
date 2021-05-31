package com.rostelecom.jirasync.enums;

import lombok.Getter;

@Getter
public enum IssueStatusId {
    TODO (10081L, 10000L),
    FIX (3L, 3L),
    CODE_REVIEW (10690L, 10300L),
    UPDATE_SERVER (11199L, 10500L),
    TEST_CASE (14187L, 10100L),
    TESTING (10034L, 10729L),
    WAITING_MERGE (13812L, 11500L),
    DONE (10501L, 10001L),
    REOPEN (12789L, 10305L),
    WAITING (10488L, 10408L),
    DECLINED (12789L, 10405L);

    private final Long IHelpStatusId;
    private final Long omnichatStatusId;

    IssueStatusId (Long val1, Long val2) {
        this.IHelpStatusId = val1;
        this.omnichatStatusId = val2;
    }

    public static IssueStatusId getByIHelpStatusId (Long id) {
        if (TODO.IHelpStatusId.equals(id)) {
            return TODO;
        }
        if (FIX.IHelpStatusId.equals(id)) {
            return FIX;
        }
        if (CODE_REVIEW.IHelpStatusId.equals(id)) {
            return CODE_REVIEW;
        }
        if (UPDATE_SERVER.IHelpStatusId.equals(id)) {
            return UPDATE_SERVER;
        }
        if (TEST_CASE.IHelpStatusId.equals(id)) {
            return TEST_CASE;
        }
        if (TESTING.IHelpStatusId.equals(id)) {
            return TESTING;
        }
        if (WAITING_MERGE.IHelpStatusId.equals(id)) {
            return WAITING_MERGE;
        }
        if (DONE.IHelpStatusId.equals(id)) {
            return DONE;
        }
        if (REOPEN.IHelpStatusId.equals(id)) {
            return REOPEN;
        }
        if (WAITING.IHelpStatusId.equals(id)) {
            return WAITING;
        }
        if (DECLINED.IHelpStatusId.equals(id)) {
            return DECLINED;
        }
        return null;
    }

    public static IssueStatusId getByOmnichat(Long id) {
        if (TODO.omnichatStatusId.equals(id)) {
            return TODO;
        }
        if (FIX.omnichatStatusId.equals(id)) {
            return FIX;
        }
        if (CODE_REVIEW.omnichatStatusId.equals(id)) {
            return CODE_REVIEW;
        }
        if (UPDATE_SERVER.omnichatStatusId.equals(id)) {
            return UPDATE_SERVER;
        }
        if (TEST_CASE.omnichatStatusId.equals(id)) {
            return TEST_CASE;
        }
        if (TESTING.omnichatStatusId.equals(id)) {
            return TESTING;
        }
        if (WAITING_MERGE.omnichatStatusId.equals(id)) {
            return WAITING_MERGE;
        }
        if (DONE.omnichatStatusId.equals(id)) {
            return DONE;
        }
        if (REOPEN.omnichatStatusId.equals(id)) {
            return REOPEN;
        }
        if (WAITING.omnichatStatusId.equals(id)) {
            return WAITING;
        }
        if (DECLINED.omnichatStatusId.equals(id)) {
            return DECLINED;
        }
        return null;
    }
}
