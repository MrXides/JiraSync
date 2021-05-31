package com.rostelecom.jirasync.services;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.rostelecom.jirasync.clients.ChildJiraClient;
import com.rostelecom.jirasync.enums.LogType;
import com.rostelecom.jirasync.enums.OperationType;
import com.rostelecom.jirasync.events.EventPublisher;
import com.rostelecom.jirasync.pathFinding.PathSettings;
import com.rostelecom.jirasync.pathFinding.StatusTransition;
import com.rostelecom.jirasync.pathFinding.TransitionPath;
import com.rostelecom.jirasync.services.interfaces.ChildJiraBusinessService;
import com.rostelecom.jirasync.services.interfaces.JiraBusinessService;
import com.rostelecom.jirasync.settings.JiraSettings;
import com.rostelecom.jirasync.utils.IssueTypeMapper;
import io.atlassian.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.rostelecom.jirasync.enums.IssueTypeId.TASK;
import static java.util.stream.Collectors.toList;

@Service
public class ChildJiraBusinessServiceImpl implements ChildJiraBusinessService {
    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private ChildJiraClient jiraClient;

    private JiraRestClient restClient;

    @Autowired
    private JiraSettings jiraSettings;

    @Autowired
    private JiraBusinessService jiraBusinessService;

    @Autowired
    private PathSettings pathSettings;

    private static final Logger logger = LoggerFactory.getLogger(ChildJiraBusinessServiceImpl.class);

    @Override
    public Issue getIssue(String issueKey) {
        return restClient.getIssueClient()
                .getIssue(issueKey)
                .claim();
    }

    @Override
    public JiraRestClient getRestClient() {
        return restClient;
    }

    @Autowired
    private void setRestClient(ChildJiraClient jiraClient) {
        this.restClient = jiraClient.getJiraRestClient();
    }

    public void getInfo() throws ExecutionException, InterruptedException {
        Iterable<BasicProject> projects = restClient.getProjectClient().getAllProjects().claim();
        BasicProject myProject;
        for (BasicProject project : projects) {
            if (project.getName().equalsIgnoreCase("MES")) {
                myProject = project;
                logger.debug("Проект с именем {} - существует", myProject.getName());
            }
        }

        Promise<Project> project = restClient.getProjectClient().getProject(jiraSettings.getChildProjectKey());
        List<IssueType> list = new ArrayList<>();
        for (IssueType type : (project.get()).getIssueTypes()) {
            list.add(type);
            logger.debug("Присутствует IssueType - {}, {}", type.getName(), type.getId());
        }
    }

    @Override
    public String createIssue(String projectKey, Long issueType, String issueSummary) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(
                projectKey, issueType, issueSummary).build();
        newIssue.getFields().put("description", new FieldInput("description", "Тестовое описание"));
        return issueClient.createIssue(newIssue).claim().getKey();

    }

    @Override
    public String createIssue(Issue issue) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(
                issue.getProject(), issue.getIssueType(), issue.getSummary()).build();
        return issueClient.createIssue(newIssue).claim().getKey();

    }

    @Override
    public void updateIssueDescription(String issueKey, String newDescription) {
        IssueInput input = new IssueInputBuilder()
                .setDescription(newDescription)
                .build();
        restClient.getIssueClient()
                .updateIssue(issueKey, input)
                .claim();
    }

    @Override
    public void updateIssueCommentsAndStatus(String parentIssueKey, String childIssueKey,
                                             List<Comment> parentComments, List<Comment> childComments) {
        IssueRestClient client = restClient.getIssueClient();
        LinkedList<Comment> list = getCommentList(parentComments, childComments);
        Issue parentIssue = jiraBusinessService.getIssue(parentIssueKey);
        Issue childIssue = getIssue(childIssueKey);
        for (Comment comment : list) {
            try {
                Comment newComment = new Comment(comment.getSelf(),
                        comment.getAuthor().getDisplayName() + ": " + comment.getBody(), null, null,
                        comment.getCreationDate(), comment.getUpdateDate(), comment.getVisibility(), null);
                client.addComment(childIssue.getCommentsUri(), newComment).claim();
                eventPublisher.publishStandardEvent(this, String.format("Для Issue {0} был добавлен новый комментарий", childIssue.getKey()), LogType.INFO);
            } catch (Exception ex) {
                eventPublisher.publishEvent(this, childIssueKey, LogType.ERROR, OperationType.ADD_COMMENT_ERROR, "Ошибка при добавлении нового комментария"
                        + "\n" + ex.toString());
            }
        }


        try {
            int currentParentStatusId = getStatusId(parentIssue);
            int currentChildStatusId = childIssue.getStatus().getId().intValue();

            if (currentChildStatusId != currentParentStatusId) {
                List<Integer> path = getPath(currentChildStatusId, currentParentStatusId);
                for (Integer id : path) {
                    client.transition(childIssue, new TransitionInput(id)).claim();
                }
                logger.info("Для Issue {} был обновлён статус", childIssue.getKey());
            }
        } catch (Exception ex) {
            logger.error("Ошибка при установке нового статуса для Issue {}. {}", childIssue.getKey(), ex.getMessage());
        }
    }

    @Override
    public void deleteIssue(String issueKey, boolean deleteSubtasks) {
        restClient.getIssueClient()
                .deleteIssue(issueKey, deleteSubtasks)
                .claim();
    }

    @Override
    public void test() {
        IssueRestClient client = restClient.getIssueClient();
        Issue parentIssue = jiraBusinessService.getIssue("OMNIDEV-2");
        Issue childIssue = getIssue("MES-10746");

        try {
            int currentParentStatusId = getStatusId(parentIssue);
            int currentChildStatusId = childIssue.getStatus().getId().intValue();

            if (currentChildStatusId != currentParentStatusId) {
                List<Integer> path = getPath(currentChildStatusId, currentParentStatusId);
                for (Integer id : path) {
                    client.transition(childIssue, new TransitionInput(id)).claim();
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    private LinkedList<Comment> getCommentList(List<Comment> parentComments, List<Comment> childComments) {
        LinkedList<Comment> list = new LinkedList<>();
        list.addAll(parentComments
                .stream()
                .filter(pc -> {
                    for (Comment child : childComments) {
                        //boolean isBodyEquals = child.getBody().equals(pc.getBody());
                        boolean isBodyEquals = child.getBody().contains(pc.getBody());
                        if (isBodyEquals) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(toList()));
        return list;
    }

    private List<Integer> getPath(int currentStatusId, int expectedStatusId) {
        List<StatusTransition> statusPath = pathSettings.getStatusTransitions();
        for (StatusTransition statusTransition : statusPath) {
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

    private Integer getStatusId(Issue parentIssue) {
        Issue issue = parentIssue;
        Status status = issue.getStatus();
        switch (status.getName().toUpperCase()) {
            case "НОВЫЙ":
                return 10000;
            case "В РАБОТЕ":
                return 3;
            case "ПРОВЕРКА КОДА":
                return 10300;
            case "УСТАНОВКА НА ТЕСТ":
                return 10500;
            case "ГОТОВО К ТЕСТИРОВАНИЮ":
                return 10100;
            case "ТЕСТИРОВАНИЕ":
                return 10729;
            case "ГОТОВО К РЕЛИЗУ":
                return 11500;
            case "ЗАВЕРШЕНО":
                return 10001;
            case "ОТКЛОНЁН":
                return 10405;
            case "В ОЖИДАНИИ":
                return 10408;
            case "ПЕРЕОТКРЫТ":
                return 10305;
            default:
                throw new NullPointerException("ID статуса не существует в методе getStatusId");
        }
    }

    public void stressTest(){
        eventPublisher.publishStandardEvent(this,"Stress Testing Omnidev", LogType.INFO);
        Long startTime = System.currentTimeMillis();
        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchResultPromise = restClient.getSearchClient().searchJql("project = " + jiraSettings.getChildProjectKey() +" AND issueType = \"MES Ошибка\" AND status not in (DONE, \"WAITING MERGE\", ОТКЛОНЁН) ORDER BY priority DESC", 500000, 0, set);
        List<Issue> issueList =  (List<Issue>) searchResultPromise.claim().getIssues();
        eventPublisher.publishStandardEvent(this, String.format("Получено %s Issue's", issueList.size()), LogType.INFO);

        Long endTime = System.currentTimeMillis();
        Long timeSync = endTime - startTime;
        if (timeSync > 60000) {
            eventPublisher.publishStandardEvent(this, String.format("Stress Testing completed. Время выполнения: %s минут(а).",
                    TimeUnit.MILLISECONDS.toMinutes(timeSync)), LogType.INFO);
        } else {
            eventPublisher.publishStandardEvent(this, String.format("Stress Testing completed. Время выполнения: %s секунд(ы).",
                    TimeUnit.MILLISECONDS.toSeconds(timeSync)), LogType.INFO);
        }
    }

    @Override
    public String createIssueFromIhelp(Issue issue, String projectKey) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder()
                .setProjectKey(projectKey)
                .setIssueTypeId(mapIssueType(issue.getIssueType().getId()))
                .setSummary(issue.getSummary())
                .setDescription(issue.getDescription())
                .setComponents(issue.getComponents())
                .build();
        BasicIssue basicIssue = issueClient.createIssue(newIssue).claim();
        copyComments(basicIssue, issue);
        return basicIssue.getKey();
    }

    private void copyComments(BasicIssue basicIssue, Issue issue) {
        for (Comment comment: issue.getComments()) {
            restClient.getIssueClient()
                    .addComment(restClient.getIssueClient()
                            .getIssue(basicIssue.getKey())
                            .claim().getCommentsUri(), comment);
        }
    }

    private Long mapIssueType(Long id) {
        Optional<Long> aLong = Optional.ofNullable(IssueTypeMapper.mapIHelp(id));
        return aLong.orElseGet(TASK::getOmnichatId);
    }
}
