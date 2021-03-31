package com.rostelecom.jirasync.schedule;


import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.rostelecom.jirasync.business.ChildJiraBusinessService;
import com.rostelecom.jirasync.business.JiraBusinessService;
import com.rostelecom.jirasync.settings.JiraSettings;
import io.atlassian.util.concurrent.Promise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@EnableScheduling
public class Schedule {
    @Autowired
    JiraSettings jiraSettings;
    @Autowired
    private JiraBusinessService jiraBusinessService;
    @Autowired
    private ChildJiraBusinessService childJiraBusinessService;

    /**
     * Синхронизация комментариев и статусов каждые 4 часа
     */
    @Scheduled(fixedRate = 14400000)
    private void Synchronization() {
        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchParentResultPromise = jiraBusinessService.getRestClient().getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() + " AND issueType = \"Ошибка\"", 50000, 0, set);
        Promise<SearchResult> searchChildResultPromise = childJiraBusinessService.getRestClient().getSearchClient().searchJql("project = " + jiraSettings.getChildProjectKey() + " AND issueType = \"MES Ошибка\"", 50000, 0, set);

        List<Issue> issueParentList;
        List<Issue> issueChildList;

        issueParentList = (List<Issue>) searchParentResultPromise.claim().getIssues();

        issueChildList = (List<Issue>) searchChildResultPromise.claim().getIssues();

        for (Issue issue : issueParentList) {
            for (Issue childIssue : issueChildList) {
                if (childIssue.getSummary().equalsIgnoreCase(issue.getSummary())) {

                    List<Comment> parentComment = new ArrayList<>();
                    List<Comment> childComment = new ArrayList<>();

                    Iterator<Comment> parentIterator = issue.getComments().iterator();
                    Iterator<Comment> childIterator = childIssue.getComments().iterator();

                    while (parentIterator.hasNext()) {
                        parentComment.add(parentIterator.next());
                    }

                    while (childIterator.hasNext()) {
                        childComment.add(childIterator.next());
                    }

                    childJiraBusinessService.updateIssueCommentsAndStatus(issue.getKey(), childIssue.getKey(), parentComment, childComment);
                }
            }
        }
    }
}
