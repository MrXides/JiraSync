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
import com.rostelecom.jirasync.configs.JiraOmnichatConfig;
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
@Qualifier("JiraOmnichatService")
public class JiraOmnichatService implements JiraService {

    private final List<StatusTransition> statusTransitions = Arrays.asList(
            new StatusTransition(10000, "Сделать", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(21)),
                    new TransitionPath(3, "Исправление", Arrays.asList(11)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(11, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(11, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(11, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(11, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(11, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(11, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(21, 61)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(11, 31))
            )),
            new StatusTransition(3, "Исправление", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(31, 151)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(31, 151, 61)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(31))
            )),
            new StatusTransition(10300, "Code Review", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(171, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(171, 161)),
                    new TransitionPath(10500, "Update server", Arrays.asList(81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(171)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(171, 161, 31))
            )),
            new StatusTransition(10500, "Update server", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(91, 101, 131, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(91, 101, 131, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(91, 101, 131, 161, 71)),
                    new TransitionPath(10100, "Test case", Arrays.asList(91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(91, 101, 131)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(91, 101, 131, 161, 31))
            )),
            new StatusTransition(10100, "Test case", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(101, 131, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(101, 131, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(101, 131, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(101, 131, 161, 71, 81)),
                    new TransitionPath(10729, "Testing", Arrays.asList(101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(101, 131)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(91, 101, 131, 161, 31))
            )),
            new StatusTransition(10729, "Testing", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(131, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(131, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(131, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(131, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(131, 161, 71, 81, 91)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(111)),
                    new TransitionPath(10001, "Done", Arrays.asList(111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(131)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(91, 101, 131, 161, 31))
            )),
            new StatusTransition(11500, "Waiting merge", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(121, 141, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(121, 141, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(121, 141, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(121, 141, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(121, 141, 161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(121, 141, 161, 71, 81, 91, 101)),
                    new TransitionPath(10001, "Done", Arrays.asList(121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(121, 141)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(121, 141, 161, 31, 151))
            )),
            new StatusTransition(10001, "Done", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(141, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(141, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(141, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(141, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(141, 161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(141, 161, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(141, 161, 71, 81, 91, 101, 111)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(141)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(141, 161, 31))
            )),
            new StatusTransition(10305, "Переоткрыта", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(161, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(161, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(161, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(161, 31))
            )),
            new StatusTransition(10408, "В ожидании", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(51)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(51, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(51, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(51, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(51, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(51, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(51, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(151, 61))
            )),
            new StatusTransition(10405, "Отклонен", Arrays.asList(
                    new TransitionPath(3, "Исправление", Arrays.asList(61, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(61, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(61, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(61, 161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(61, 161, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(61, 161, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(61, 161, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(61)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(61, 161, 31))
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
    public Issue getIssue(String key) {
        return jiraClient.getIssueClient().getIssue(key).claim();
    }

    @Override
    public List<String> getIssueKeyList() {
        List<String> result = new ArrayList<>();
        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchParentResultPromise = jiraClient.getSearchClient()
                //TODO:Not only errors
                .searchJql("project = " + ProjectKey.MES2.name() + " AND issueType = \"Bug\"" + "AND issuekey = \"MES2-55\"", 5000, 0, set);
        searchParentResultPromise.claim().getIssues().forEach(issue -> result.add(issue.getKey()));
        return result;
    }

    @Override
    public BasicIssue duplicateIssue(Issue issue) {
        IssueRestClient issueClient = jiraClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder()
                .setProjectKey(ProjectKey.MES2.name())
                .setIssueTypeId(IssueTypeMapper.mapIHelp(issue.getIssueType().getId()))
                .setSummary(issue.getSummary())
                .setDescription(issue.getDescription())
                .setComponents(issue.getComponents())
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
                .setProjectKey(ProjectKey.MES2.name())
                .setIssueTypeId(IssueTypeMapper.mapIHelp(newerIssue.getIssueType().getId()))
                .setSummary(newerIssue.getSummary())
                .setDescription(newerIssue.getDescription())
                .setComponents(newerIssue.getComponents())
                .build();
        issueClient.updateIssue(issueToUpdate.getKey(), newIssue);
        updateStatus(issueToUpdate, issueToUpdate.getStatus().getId(), IssueStatusIdMapper.mapIHelp(newerIssue.getStatus().getId()));
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

    public JiraOmnichatService(JiraOmnichatConfig config) {
        this.jiraClient = new CustomAsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthenticationCustom(URI.create(config.getJiraUri()),
                        config.getUserName(),
                        config.getPassword(),
                        config.getSocketTimeout(),
                        config.getRequestTimeout());
    }
}
