package com.rostelecom.jirasync.business;

import com.atlassian.jira.rest.client.api.*;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.rostelecom.jirasync.clients.JiraClient;
import com.rostelecom.jirasync.settings.JiraSettings;
import io.atlassian.util.concurrent.Promise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@EnableScheduling
public class JiraBusinessServiceImpl implements JiraBusinessService{
    @Autowired
    private JiraClient jiraClient;

    private JiraRestClient restClient;

    @Autowired
    private void setRestClient(JiraClient jiraClient){
        this.restClient = jiraClient.getJiraRestClient();
    }

    @Autowired
    private JiraSettings jiraSettings;
    @Autowired
    private ChildJiraBusinessService childJiraBusinessService;

    @Override
    public JiraRestClient getRestClient(){
        return restClient;
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

    public void updateIssueCommentsAndStatus(String issueKey, IssueInput input){
        restClient.getIssueClient()
                .updateIssue(issueKey, input)
                .claim();
    }

    @Override
    public Issue getIssue(String issueKey) {
        return restClient.getIssueClient()
                .getIssue(issueKey)
                .claim();
    }

    @Override
    public void deleteIssue(String issueKey, boolean deleteSubtasks) {
        restClient.getIssueClient()
                .deleteIssue(issueKey, deleteSubtasks)
                .claim();
    }

    public void copyIssue(){
        Set<String> set = new HashSet<String>();
        set.add("*all");
        Promise<SearchResult> searchParentResultPromise =  restClient.getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() +" AND issueType = \"Ошибка\"", 10000, 0, set);
        Promise<SearchResult> searchChildResultPromise =  childJiraBusinessService.getRestClient().getSearchClient().searchJql("project = " + jiraSettings.getChildProjectKey() +" AND issueType = \"MES Ошибка\"", 10000, 0, set);

        List<Issue> issueParentList;
        List<Issue> issueChildList;

        issueParentList = (List<Issue>) searchParentResultPromise.claim().getIssues();

        issueChildList = (List<Issue>) searchChildResultPromise.claim().getIssues();


        for(Issue issue : issueParentList){
            for(Issue childIssue : issueChildList){
                if(childIssue.getSummary().equalsIgnoreCase(issue.getSummary())){

                    List<Comment> parentComment = new ArrayList<>();
                    List<Comment> childComment = new ArrayList<>();

                    Iterator<Comment> parentIterator = issue.getComments().iterator();
                    Iterator<Comment> childIterator = childIssue.getComments().iterator();

                    while(parentIterator.hasNext()){
                        parentComment.add(parentIterator.next());
                    }

                    while(childIterator.hasNext()){
                        childComment.add(childIterator.next());
                    }

                    childJiraBusinessService.updateIssueCommentsAndStatus(childIssue.getKey(), parentComment, childComment);
                }
            }
        }

    }

    public void getInfo() throws ExecutionException, InterruptedException {
        Iterable<BasicProject> projects = restClient.getProjectClient().getAllProjects().claim();
        BasicProject myProject;
        for(BasicProject project : projects){
            if(project.getName().equalsIgnoreCase("Омничат Dev")){
                myProject = project;
            }
        }

        Promise<Project> project = restClient.getProjectClient().getProject(jiraSettings.getParentProjectKey());
        List<IssueType> list = new ArrayList<>();
        for(IssueType type : (project.get()).getIssueTypes()){
            list.add(type);
        }


        Set<String> set = new HashSet<String>();
        set.add("*all");
        Promise<SearchResult> searchParentResultPromise =  restClient.getSearchClient().searchJql("project = " + jiraSettings.getParentProjectKey() +" AND issueType = \"Ошибка\"", 500000, 0, set);
        List<Issue> issueParentList;
        issueParentList = (List<Issue>) searchParentResultPromise.claim().getIssues();
        IssueRestClient client = restClient.getIssueClient();
        Promise<Iterable<Transition>> transitionList = client.getTransitions(issueParentList.get(0));
        List<Transition> transitions = new ArrayList<>();
        for(; ;){
            if(transitionList.claim().iterator().hasNext()){
                transitions.add(transitionList.claim().iterator().next());
            }else{
                break;
            }
        }
        int i = 0;
    }
}
