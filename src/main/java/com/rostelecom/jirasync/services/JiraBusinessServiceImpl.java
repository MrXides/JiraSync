package com.rostelecom.jirasync.services;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.rostelecom.jirasync.clients.JiraClient;
import com.rostelecom.jirasync.enums.LogType;
import com.rostelecom.jirasync.events.EventPublisher;
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

@Service
public class JiraBusinessServiceImpl implements JiraBusinessService {
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private JiraClient jiraClient;

    private JiraRestClient restClient;

    @Autowired
    private JiraSettings jiraSettings;

    @Autowired
    private ChildJiraBusinessService childJiraBusinessService;

    private static final Logger logger = LoggerFactory.getLogger(JiraBusinessServiceImpl.class);
    private Object CreateIssueMetadataJsonParser;

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

        // REST Client

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

    public void errorReport(String message) {
        IssueRestClient client = restClient.getIssueClient();
        Issue issue = getIssue(System.getenv("Reporter"));
        client.addComment(issue.getCommentsUri(), Comment.valueOf(message));
    }

    public void stressTest() {
        eventPublisher.publishStandardEvent(this, "Stress Testing IHelp", LogType.INFO);
        Long startTime = System.currentTimeMillis();
        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchResultPromise = restClient.getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() + " AND issueType = \"Ошибка\" AND status not in (ЗАВЕРШЕНО, \"ГОТОВО К РЕЛИЗУ\", ОТКЛОНЁН) ORDER BY priority DESC", 500000, 0, set);
        List<Issue> issueList = (List<Issue>) searchResultPromise.claim().getIssues();
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

    public void ticketsBuy() {
        eventPublisher.publishStandardEvent(this, " Tickets", LogType.INFO);
        Long startTime = System.currentTimeMillis();
        Set<String> set = new HashSet<>();
        set.add("*all");
        Promise<SearchResult> searchResultPromise = restClient.getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() + " AND issueType = \"Ошибка\" AND status = \"ОТКЛОНЁН\" ORDER BY priority DESC", 500000, 0, set);
        List<Issue> issueList = (List<Issue>) searchResultPromise.claim().getIssues();

        eventPublisher.publishStandardEvent(this, String.format("Получено %s Issue's", issueList.size()), LogType.INFO);
        Long endTime = System.currentTimeMillis();
        Long timeSync = endTime - startTime;
        if (timeSync > 60000) {
            eventPublisher.publishStandardEvent(this, String.format("Время выполнения: %s минут(а).",
                    TimeUnit.MILLISECONDS.toMinutes(timeSync)), LogType.INFO);
        } else {
            eventPublisher.publishStandardEvent(this, String.format(" Время выполнения: %s секунд(ы).",
                    TimeUnit.MILLISECONDS.toSeconds(timeSync)), LogType.INFO);
        }
    }

    @Override
    public void createIssueFromTest2(Issue issue, String projectKey) {
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
    }

    private void copyComments(BasicIssue basicIssue, Issue issue) {
        for (Comment comment: issue.getComments()) {
            restClient.getIssueClient()
                    .addComment(restClient.getIssueClient()
                            .getIssue(basicIssue.getKey())
                            .claim().getCommentsUri(), comment);
        }
    }
// Изменить(вдруг)
    private Long mapIssueType(Long id) {
        Optional<Long> aLong = Optional.ofNullable(IssueTypeMapper.mapOmnichat(id));
        return aLong.orElseGet(TASK::getIHelpId);
    }
}