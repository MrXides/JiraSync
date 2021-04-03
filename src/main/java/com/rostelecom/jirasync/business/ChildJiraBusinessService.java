package com.rostelecom.jirasync.business;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ChildJiraBusinessService {
    Issue getIssue(String issueKey);

    JiraRestClient getRestClient();

    void getInfo() throws ExecutionException, InterruptedException;

    String createIssue(String projectKey, Long issueType, String issueSummary);

    String createIssue(Issue issue);

    void updateIssueDescription(String issueKey, String newDescription);

    void updateIssueCommentsAndStatus(String parentIssueKey, String childIssueKey, List<Comment> parentComments, List<Comment> childComments);

    void deleteIssue(String issueKey, boolean deleteSubtasks);

    void test();
}
