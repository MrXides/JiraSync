package com.rostelecom.jirasync.scheduled;


import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.rostelecom.jirasync.business.ChildJiraBusinessService;
import com.rostelecom.jirasync.business.JiraBusinessService;
import com.rostelecom.jirasync.enums.LogType;
import com.rostelecom.jirasync.events.EventPublisher;
import com.rostelecom.jirasync.settings.JiraSettings;
import io.atlassian.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@EnableScheduling
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

    /**
     * Синхронизация комментариев и статусов каждые 4 часа
     */
    @Scheduled(fixedRate = 5400000)
    private void Synchronization() {
        eventPublisher.publishStandardEvent(this,"Начало синхронизации", LogType.INFO);
        Long startTime = System.currentTimeMillis();

        Set<String> set = new HashSet<>();
        set.add("*all");

        Promise<SearchResult> searchParentResultPromise = jiraBusinessService.getRestClient().getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() + " AND issueType = \"Ошибка\"", 50000, 0, set);

        List<Issue> issueParentList = (List<Issue>) searchParentResultPromise.claim().getIssues();

        eventPublisher.publishStandardEvent(this,String.format("Количество Issue для проверки и синхронизации - {0}", issueParentList.size()), LogType.INFO);

        if(issueParentList.size() == 0){
            eventPublisher.publishStandardEvent(this, "Завершение синхронизации из-за непредвиденной ошибки:", LogType.ERROR);
            eventPublisher.publishStandardEvent(this, "Не удалось получить Issue из IHelp." +
                    " Проверьте Url на наличие https или обратитесь к администратору", LogType.ERROR);
            return;
        }
        String childKey = "";
        for (Issue issue : issueParentList) {
            Set<String> labelsSet = issue.getLabels();
            for(String label : labelsSet){
                if(label.contains("MES"))
                    childKey = label;
            }
            if(childKey.isEmpty()){
                eventPublisher.publishStandardEvent(this, String.format("Для Issue {0} из IHelp отсутствует метка на Issue из Omnichat",
                        issue.getKey()), LogType.ERROR);
                continue;
            }
            Issue childIssue = childJiraBusinessService.getIssue(childKey);
            if (childIssue != null) {
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
            } else {
                eventPublisher.publishStandardEvent(this, String.format("Для Issue {0} не найден дочерний Issue {1} из Omnichat",
                        issue.getKey(), childKey), LogType.ERROR);
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
}
