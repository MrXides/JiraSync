package com.rostelecom.jirasync.business;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.rostelecom.jirasync.clients.JiraClient;
import com.rostelecom.jirasync.settings.JiraSettings;
import io.atlassian.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class JiraBusinessServiceImpl implements JiraBusinessService {
    @Autowired
    private JiraClient jiraClient;

    private JiraRestClient restClient;

    @Autowired
    private JiraSettings jiraSettings;

    @Autowired
    private ChildJiraBusinessService childJiraBusinessService;

    private static final Logger logger = LoggerFactory.getLogger(JiraBusinessServiceImpl.class);

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
    private void setRestClient(JiraClient jiraClient) {
        this.restClient = jiraClient.getJiraRestClient();
    }

    public void getInfo() throws ExecutionException, InterruptedException {
        Iterable<BasicProject> projects = restClient.getProjectClient().getAllProjects().claim();
        BasicProject myProject;
        for (BasicProject project : projects) {
            if (project.getName().equalsIgnoreCase("Омничат Dev")) {
                myProject = project;
                logger.debug("Проект с именем {} - существует", myProject.getName());
            }
        }

        Promise<Project> project = restClient.getProjectClient().getProject(jiraSettings.getParentProjectKey());
        List<IssueType> list = new ArrayList<>();
        for (IssueType type : (project.get()).getIssueTypes()) {
            list.add(type);
            logger.debug("Присутствует IssueType - {}", type.getName());
        }


        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchParentResultPromise = restClient.getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() + " AND issueType = \"Ошибка\"", 500000, 0, set);
        List<Issue> issueParentList;
        issueParentList = (List<Issue>) searchParentResultPromise.claim().getIssues();
        IssueRestClient client = restClient.getIssueClient();
        Promise<Iterable<Transition>> transitionList = client.getTransitions(issueParentList.get(0));
        List<Transition> transitions = new ArrayList<>();
        for (; ; ) {
            if (transitionList.claim().iterator().hasNext()) {
                transitions.add(transitionList.claim().iterator().next());
            } else {
                break;
            }
        }
    }

    @Override
    public String createIssue(String projectKey, Long issueType, String issueSummary) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(
                projectKey, issueType, issueSummary).build();
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
    public void deleteIssue(String issueKey, boolean deleteSubtasks) {
        restClient.getIssueClient()
                .deleteIssue(issueKey, deleteSubtasks)
                .claim();
    }

}
