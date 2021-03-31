package com.rostelecom.jirasync.business;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ChildJiraBusinessService {
    String createIssue(String projectKey, Long issueType, String issueSummary);
    String createIssue(Issue issue);
    void updateIssueDescription(String issueKey, String newDescription);
    Issue getIssue(String issueKey);
    void deleteIssue(String issueKey, boolean deleteSubtasks);
    JiraRestClient getRestClient();
    void getInfo() throws ExecutionException, InterruptedException;
    void updateIssueCommentsAndStatus(String issueKey, List<Comment> parentComments, List<Comment> childComments);
}
