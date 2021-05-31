package com.rostelecom.jirasync.enums;

public enum IssueTypeId {
    TASK (17L, 10711L),
    EPIC (10000L, 10004L),
    STORY (64L, 10000L),
    SUBTASK (18L, 10002L),
    ERROR (121L, 11300L);

    private final Long IHelpId;
    private final Long omnichatId;

    IssueTypeId (Long val1, Long val2) {
        this.IHelpId = val1;
        this.omnichatId = val2;
    }

    public static IssueTypeId getByIHelpId (Long id) {
        if (TASK.IHelpId.equals(id)) {
            return TASK;
        }
        if (EPIC.IHelpId.equals(id)) {
            return EPIC;
        }
        if (STORY.IHelpId.equals(id)) {
            return STORY;
        }
        if (SUBTASK.IHelpId.equals(id)) {
            return SUBTASK;
        }
        if (ERROR.IHelpId.equals(id)) {
            return ERROR;
        }
        return null;
    }

    public static IssueTypeId getByOmnichat(Long id) {
        if (TASK.omnichatId.equals(id)) {
            return TASK;
        }
        if (EPIC.omnichatId.equals(id)) {
            return EPIC;
        }
        if (STORY.omnichatId.equals(id)) {
            return STORY;
        }
        if (SUBTASK.omnichatId.equals(id)) {
            return SUBTASK;
        }
        if (ERROR.omnichatId.equals(id)) {
            return ERROR;
        }
        return null;
    }

    public Long getIHelpId() {
        return IHelpId;
    }

    public Long getOmnichatId() {
        return omnichatId;
    }
}