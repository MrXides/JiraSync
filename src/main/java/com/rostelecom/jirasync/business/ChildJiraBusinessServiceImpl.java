package com.rostelecom.jirasync.business;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.rostelecom.jirasync.clients.ChildJiraClient;
import com.rostelecom.jirasync.pathFinding.PathSettings;
import com.rostelecom.jirasync.pathFinding.StatusTransition;
import com.rostelecom.jirasync.pathFinding.TransitionPath;
import com.rostelecom.jirasync.settings.JiraSettings;
import io.atlassian.util.concurrent.Promise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

@Service
public class ChildJiraBusinessServiceImpl implements ChildJiraBusinessService {
    @Autowired
    private ChildJiraClient jiraClient;

    private JiraRestClient restClient;

    @Autowired
    private JiraSettings jiraSettings;

    @Autowired
    private JiraBusinessService jiraBusinessService;


    @Autowired
    private PathSettings pathSettings;

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
            }
        }

        Promise<Project> project = restClient.getProjectClient().getProject(jiraSettings.getChildProjectKey());
        List<IssueType> list = new ArrayList<>();
        for (IssueType type : (project.get()).getIssueTypes()) {
            list.add(type);
        }
        int i = 0;
    }

    @Override
    public String createIssue(String projectKey, Long issueType, String issueSummary) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(
                projectKey, issueType, issueSummary).build();
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
            client.addComment(childIssue.getCommentsUri(), comment).claim();
        }

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
                        boolean isBodyEquals = child.getBody().equals(pc.getBody());
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


}
