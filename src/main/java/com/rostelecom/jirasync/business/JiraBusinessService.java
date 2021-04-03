package com.rostelecom.jirasync.business;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

import java.util.concurrent.ExecutionException;

public interface JiraBusinessService {
    Issue getIssue(String issueKey);

    JiraRestClient getRestClient();

    void getInfo() throws ExecutionException, InterruptedException;

    String createIssue(String projectKey, Long issueType, String issueSummary);

    void updateIssueDescription(String issueKey, String newDescription);

    void deleteIssue(String issueKey, boolean deleteSubtasks);
}
