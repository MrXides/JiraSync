package com.rostelecom.jirasync.scheduled;


import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.rostelecom.jirasync.services.interfaces.ChildJiraBusinessService;
import com.rostelecom.jirasync.services.interfaces.JiraBusinessService;
import com.rostelecom.jirasync.enums.LogType;
import com.rostelecom.jirasync.events.EventPublisher;
import com.rostelecom.jirasync.settings.JiraSettings;
import io.atlassian.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

// ttttt
@Service
//@EnableScheduling
public class Scheduler {
    @Autowired
    private EventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    @Autowired
    JiraSettings jiraSettings;
    @Autowired
    private JiraBusinessService jiraBusinessService;
    @Autowired
    private ChildJiraBusinessService childJiraBusinessService;

    private List<String> inappropriateStatuses = Arrays.asList("DONE", "WAITING MERGE", "ОТКЛОНЁН");

    /**
     * Синхронизация комментариев и статусов каждые 4 часа
     */
    //@Scheduled(fixedRate = 5400000)
    private void Synchronization() {
        eventPublisher.publishStandardEvent(this,"Начало синхронизации", LogType.INFO);
        Long startTime = System.currentTimeMillis();

        List<Issue> issueParentList = getIssueParentList(jiraSettings.getParentProjectKey(), 5000, 0);

        eventPublisher.publishStandardEvent(this,String.format("Количество Issue для проверки и синхронизации - {}", issueParentList.size()), LogType.INFO);
        if(issueParentList.size() == 0){
            eventPublisher.publishStandardEvent(this, "Завершение синхронизации из-за непредвиденной ошибки:", LogType.ERROR);
            eventPublisher.publishStandardEvent(this, "Не удалось получить Issue из IHelp." +
                    " Проверьте Url на наличие https или обратитесь к администратору", LogType.ERROR);
            return;
        }

        for (Issue issue : issueParentList) {

            Issue childIssue = getChildIssue(issue);

            if (childIssue != null) {
                updateChildIssue(childIssue, issue);
            } else {
                cloneParentIssue(issue);
            }
        }
        Long endTime = System.currentTimeMillis();
        Long timeSync = endTime - startTime;
        if (timeSync > 60000) {
            eventPublisher.publishStandardEvent(this, String.format("Синхронизация завершена. Время выполнения: {0} минут(а).",
                    TimeUnit.MILLISECONDS.toMinutes(timeSync)), LogType.INFO);
        } else {
            eventPublisher.publishStandardEvent(this, String.format("Синхронизация завершена. Время выполнения: {0} секунд(ы).",
                    TimeUnit.MILLISECONDS.toSeconds(timeSync)), LogType.INFO);
        }
    }

    private void cloneParentIssue(Issue issue) {
        if (/*issue.getIssueType().getName().contains("Ошибка") &&*/
                !inappropriateStatuses.contains(issue.getStatus().getName())) {
            String key = childJiraBusinessService.createIssueFromIhelp(issue, "MES2");
            jiraBusinessService.getRestClient().getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(key)).claim();
        } else {
            eventPublisher.publishStandardEvent(this, String.format("Для Issue {0} не найден дочерний Issue из Omnichat",
                    issue.getKey()), LogType.ERROR);
        }
    }

    private void updateChildIssue(Issue childIssue, Issue issue) {
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

    private Issue getChildIssue(Issue issue) {
        String childKey = "";
        Iterable<Comment> comments = issue.getComments();
        for (Comment comment : comments) {
            if (comment.getBody().contains("MES")) {
                childKey = comment.getBody().toUpperCase().replaceAll("\\s+", "");
            }
        }

        if(childKey.isEmpty()) {
            eventPublisher.publishStandardEvent(this,
                    String.format("Для Issue {0} из IHelp отсутствует метка на Issue из Omnichat",
                    issue.getKey()),
                    LogType.ERROR);
            return null;
        }

        try {
            return childJiraBusinessService
                    .getRestClient()
                    .getSearchClient()
                    .searchJql("project = MES2 AND issuekey = " + childKey)
                    .claim().getIssues().iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Issue> getIssueParentList(String parentProjectKey, int maxResults, int startsAt) {
        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchParentResultPromise = jiraBusinessService.getRestClient()
                .getSearchClient()
                //TODO:Not only errors
                .searchJql("project = " + parentProjectKey + " AND issueType = \"Ошибка\"", maxResults, startsAt, set);
        return (List<Issue>) searchParentResultPromise.claim().getIssues();
    }
}
