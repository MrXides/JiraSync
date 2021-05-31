package com.rostelecom.jirasync.services;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.rostelecom.jirasync.asyncFactory.CustomAsynchronousJiraRestClientFactory;
import com.rostelecom.jirasync.configs.JiraIHelpConfig;
import com.rostelecom.jirasync.enums.ProjectKey;
import com.rostelecom.jirasync.pathFinding.StatusTransition;
import com.rostelecom.jirasync.pathFinding.TransitionPath;
import com.rostelecom.jirasync.services.interfaces.JiraService;
import com.rostelecom.jirasync.utils.IssueStatusIdMapper;
import com.rostelecom.jirasync.utils.IssueTypeMapper;
import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@Service
@Slf4j
@Qualifier("JiraIHelpService")
public class JiraIHelpService implements JiraService {

    //TODO: Значения стутсов для Ihelp
    private final List<StatusTransition> statusTransitions = Arrays.asList(
            new StatusTransition(10081, "Новый", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(221)),
                    new TransitionPath(4, "В работе", Arrays.asList(241)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList(241, 391)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList(241, 391, 401)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList(241, 391, 401, 341)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList(241, 391, 401, 341, 171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList(241, 391, 401, 341, 171, 411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList(241, 391, 401, 341, 171, 411, 421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(221, 231)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList(241, 351))
            )),
            new StatusTransition(4, "В работе", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(351,371)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList( 391)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList( 391, 401)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList( 391, 401, 341)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList( 391, 401, 341, 171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList( 391, 401, 341, 171, 411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList( 391, 401, 341, 171, 411, 421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(351, 371,231)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList( 351))
            )),
            //TODO ДОДЕЛАТЬ
            new StatusTransition(10690, "Проверка кода", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(401,341, 171,301,381,351,371)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList(  401)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList( 401, 341)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList( 401, 341, 171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList(  401, 341, 171, 411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList(  401, 341, 171, 411, 421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(401, 341, 171,301)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList( 401, 341, 171,301,381,351)),
                    new TransitionPath(4, "В работе", Arrays.asList(401, 341, 171,301,381))
            )),
            new StatusTransition(10500, "Установка на тест", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(341, 171,301,381,351,371)),
                    new TransitionPath(4, "В работе", Arrays.asList(341, 171,301,381)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList(341, 171,301,381,391)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList(341)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList( 341, 171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList( 341, 171, 411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList( 341, 171, 411, 421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(341, 171,301)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList(341, 171,301,381,351))

            )),
            new StatusTransition(14187, "Готово к тестированию", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(171,301,381,351,371)),
                    new TransitionPath(4, "В работе", Arrays.asList(171,301,381)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList(171,301,381,391)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList(171,301,381,391,401)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList(171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList(171,411 )),
                    new TransitionPath(10501, "Завершено", Arrays.asList(171,411,421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(171,301)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList(171,301,381,351))
            )),
            new StatusTransition(10034, "Тестирование", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(301,381,351,371)),
                    new TransitionPath(4, "В работе", Arrays.asList(301,381)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList(301,381,391)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList(301,381,391,401)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList(301,381,391,401,341)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList(411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList(411,421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(301)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList(301,381,351))
            )),
            new StatusTransition(13812, "Готово к релизу", Arrays.asList(
                    new TransitionPath(10501, "Завершено", Arrays.asList( 421))
            )),
            new StatusTransition(12789, "Переоткрыт", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(381,351,371)),
                    new TransitionPath(4, "В работе", Arrays.asList(381)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList(381,391)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList(381,391,401)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList(381,391,401,341)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList(381,391,401,341,171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList(381,391,401,341,171,411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList(381,391,401,341,171,411,421)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList(381,351))
            )),
            new StatusTransition(10488, "В ожидании", Arrays.asList(
                    new TransitionPath(10016, "Отклонён", Arrays.asList(371)),
                    new TransitionPath(4, "В работе", Arrays.asList(241)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList(241, 391)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList(241, 391, 401)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList(241, 391, 401, 341)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList(241, 391, 401, 341, 171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList(241, 391, 401, 341, 171, 411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList(241, 391, 401, 341, 171, 411, 421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(371,231))
            )),
            new StatusTransition(10016, "Отклонён", Arrays.asList(
                    new TransitionPath(4, "В работе", Arrays.asList(231,381)),
                    new TransitionPath(10690, "Проверка кода", Arrays.asList(231,381,391)),
                    new TransitionPath(11199, "Установка на тест", Arrays.asList(231,381,391,401)),
                    new TransitionPath(14187, "Готово к тестированию", Arrays.asList(231,381,391,401,341)),
                    new TransitionPath(10034, "Тестирование", Arrays.asList(231,381,391,401,341,171)),
                    new TransitionPath(13812, "Готово к релизу", Arrays.asList(231,381,391,401,341,171, 411)),
                    new TransitionPath(10501, "Завершено", Arrays.asList(231,381,391,401,341,171, 411, 421)),
                    new TransitionPath(12789, "Переоткрыт", Arrays.asList(231)),
                    new TransitionPath(10488, "В ожидании", Arrays.asList(231,381,351))
            ))
    );

    private final JiraRestClient jiraClient;

    @Override
    public String createIssue(String projectKey, Long issueType, String issueSummary) {
        IssueRestClient issueClient = jiraClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(projectKey, issueType, issueSummary).build();
        return issueClient.createIssue(newIssue).claim().getKey();
    }

    @Override
    public Issue getIssue (String key) {
        return jiraClient.getIssueClient().getIssue(key).claim();
    }

    @Override
    public List<String> getIssueKeyList() {
        List<String> result = new ArrayList<>();
        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchParentResultPromise = jiraClient.getSearchClient()
                //TODO:Not only errors
                .searchJql("project = " + ProjectKey.OMNIDEV.name() + " AND issueType = \"Ошибка\"" + "AND issuekey = \"OMNIDEV-125\"", 5000, 0, set);
        searchParentResultPromise.claim().getIssues().forEach(issue -> result.add(issue.getKey()));
        return result;
    }

    @Override
    public BasicIssue duplicateIssue(Issue issue) {
        IssueRestClient issueClient = jiraClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder()
                .setProjectKey(ProjectKey.OMNIDEV.name())
                .setIssueTypeId(IssueTypeMapper.mapOmnichat(issue.getIssueType().getId()))
                .setSummary(issue.getSummary())
                .setDescription(issue.getDescription())
                .build();
        BasicIssue basicIssue = issueClient.createIssue(newIssue).claim();
        copyComments(basicIssue, issue);
        return basicIssue;
    }

    private void copyComments(BasicIssue basicIssue, Issue issue) {
        for (Comment comment: issue.getComments()) {
            jiraClient.getIssueClient()
                    .addComment(jiraClient.getIssueClient()
                            .getIssue(basicIssue.getKey())
                            .claim().getCommentsUri(), comment);
        }
        jiraClient.getIssueClient()
                .addComment(jiraClient.getIssueClient()
                        .getIssue(basicIssue.getKey())
                        .claim().getCommentsUri(), Comment.valueOf(issue.getKey() + "_" + issue.getSelf().toString()));
    }

    @Override
    public void updateIssue(Issue issueToUpdate, Issue newerIssue) {
        IssueRestClient issueClient = jiraClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder()
                .setProjectKey(ProjectKey.OMNIDEV.name())
                .setIssueTypeId(IssueTypeMapper.mapIHelp(newerIssue.getIssueType().getId()))
                .setSummary(newerIssue.getSummary())
                .setDescription(newerIssue.getDescription())
                .setComponents(newerIssue.getComponents())
                .build();
        issueClient.updateIssue(issueToUpdate.getKey(), newIssue);
        updateStatus(issueToUpdate, issueToUpdate.getStatus().getId(), IssueStatusIdMapper.mapOmnichat(newerIssue.getStatus().getId()));
    }

    private void updateStatus(Issue issueToUpdate, Long issueToUpdateStatusId, Long newerIssueStatusId) {
        try {
            if (!issueToUpdateStatusId.equals(newerIssueStatusId)) {
                List<Integer> path = getPath(issueToUpdateStatusId, newerIssueStatusId);
                if (path != null && !path.isEmpty()) {
                    for (Integer id : path) {
                        jiraClient.getIssueClient().transition(issueToUpdate, new TransitionInput(id)).claim();
                    }
                    log.info("Для Issue {} был обновлён статус", issueToUpdate.getKey());
                } else {
                    log.info("Для Issue {} не найдена карта обновления статусов", issueToUpdate.getKey());
                }
            }
        } catch (Exception ex) {
            log.error("Ошибка при установке нового статуса для Issue {}. {}", issueToUpdate.getKey(), ex.getMessage());
        }
    }

    @Override
    public void updateComments(List<Comment> commentList, URI commentsUri) {
        if (commentList != null && !commentList.isEmpty()) {
            for (Comment comment : commentList) {
                try {
                    Comment newComment = new Comment(comment.getSelf(),
                            comment.getBody(),
                            null,
                            null,
                            comment.getCreationDate(),
                            comment.getUpdateDate(),
                            comment.getVisibility(),
                            null);
                    jiraClient.getIssueClient().addComment(commentsUri, newComment).claim();
                } catch (Exception ex) {
                    log.error("Ошибка при записи комментария");
                    ex.printStackTrace();
                }
            }
        }
    }

    private List<Integer> getPath(Long currentStatusId, Long expectedStatusId) {
        for (StatusTransition statusTransition : statusTransitions) {
            if (statusTransition.getId() == currentStatusId) {
                List<TransitionPath> transitionPaths = statusTransition.getTransitionPaths();
                for (TransitionPath transitionPath : transitionPaths) {
                    if (transitionPath.getId() == expectedStatusId) {
                        return transitionPath.getPath();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public JiraRestClient getJiraRestClient() {
        return jiraClient;
    }

    public JiraIHelpService(JiraIHelpConfig config) {
        this.jiraClient = new CustomAsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthenticationCustom(URI.create(config.getJiraUri()),
                        config.getUserName(),
                        config.getPassword(),
                        config.getSocketTimeout(),
                        config.getRequestTimeout());
    }
}
