package com.rostelecom.jirasync.scheduled;


import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.rostelecom.jirasync.business.ChildJiraBusinessService;
import com.rostelecom.jirasync.business.JiraBusinessService;
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
        logger.info("Начало синхронизации");
        Long startTime = System.currentTimeMillis();

        Set<String> set = new HashSet<>();
        set.add("*all");

        Promise<SearchResult> searchParentResultPromise = jiraBusinessService.getRestClient().getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() + " AND issueType = \"Ошибка\"", 50000, 0, set);

        List<Issue> issueParentList = (List<Issue>) searchParentResultPromise.claim().getIssues();

        logger.info("Количество Issue для возможного обновления - {}.", issueParentList.size());
        if(issueParentList.size() == 0){
            logger.info("Не удалось получить Issue из IHelp, проверьте Url на наличие https");
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
                logger.error("Для Issue Ihelp {} отсутствует метка Issue на Omnichat", issue.getKey());
                return;
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
                logger.error("Дочерний Issue для {} не найден, либо присутствует больше одного Issue с одинаковым описанием", issue.getKey());
            }
        }
        Long endTime = System.currentTimeMillis();
        Long timeSync = endTime - startTime;
        if (timeSync > 60000) {
            logger.info("Синхронизация завершена. Время выполнения: {} минут(а).", TimeUnit.MILLISECONDS.toMinutes(timeSync));
        } else {
            logger.info("Синхронизация завершена. Время выполнения: {} секунд(ы).", TimeUnit.MILLISECONDS.toSeconds(timeSync));
        }
    }
}
