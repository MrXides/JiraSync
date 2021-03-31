package com.rostelecom.jirasync.business;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

import java.util.concurrent.ExecutionException;

public interface JiraBusinessService {
    JiraRestClient getRestClient();
    String createIssue(String projectKey, Long issueType, String issueSummary);
    void updateIssueDescription(String issueKey, String newDescription);
    Issue getIssue(String issueKey);
    void deleteIssue(String issueKey, boolean deleteSubtasks);
    void getInfo() throws ExecutionException, InterruptedException;
}
