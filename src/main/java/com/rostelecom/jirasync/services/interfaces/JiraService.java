package com.rostelecom.jirasync.services.interfaces;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;

import java.net.URI;
import java.util.List;

public interface JiraService {
    //TODO: Добавить везде javadoc

    String createIssue(String projectKey, Long issueType, String issueSummary);

    Issue getIssue(String key);

    List<String> getIssueKeyList();

    BasicIssue duplicateIssue(Issue issue);

    void updateComments(List<Comment> commentList, URI commentsUri);

    JiraRestClient getJiraRestClient();

    void updateIssue(Issue issueToUpdate, Issue newerIssue);
}
