package com.rostelecom.jirasync.testControllers;

import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.rostelecom.jirasync.services.interfaces.ChildJiraBusinessService;
import com.rostelecom.jirasync.services.interfaces.JiraBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

//localhost:8080/issue/create
@RestController
@RequestMapping(path = "/issue")
public class JiraIssueController {

    @Autowired
    private JiraBusinessService jiraBusinessService;
    @Autowired
    private ChildJiraBusinessService childJiraBusinessService;

    @PostMapping("/createchild")
    protected String createChildIssue(@RequestParam("projectKey") String projectKey,
                                      @RequestParam("issueType") Long issueType,
                                      @RequestParam("issueSummary") String issueSummary) {
        return childJiraBusinessService.createIssue(projectKey, issueType, issueSummary);
    }

    @PostMapping("/createparent")
    protected String createParentIssue(@RequestParam("projectKey") String projectKey,
                                       @RequestParam("issueType") Long issueType,
                                       @RequestParam("issueSummary") String issueSummary) {
        return jiraBusinessService.createIssue(projectKey, issueType, issueSummary);
    }

    @GetMapping("/infoParent")
    protected void infoParent() throws ExecutionException, InterruptedException {
        jiraBusinessService.getInfo();
    }

    @GetMapping("/infoChild")
    protected void infoChild() throws ExecutionException, InterruptedException {
        childJiraBusinessService.getInfo();
    }

    @PostMapping("/deleteChildIssue")
    protected void deleteChildIssue(@RequestParam("issueKey") String issueKey,
                                    @RequestParam("deleteSubTasks") Boolean deleteSubTasks) {
        childJiraBusinessService.deleteIssue(issueKey, deleteSubTasks);
    }

    @GetMapping("/testChild")
    protected void testChild() {
        childJiraBusinessService.test();
    }

    @GetMapping("/stresstestihelp") // omnichat
    protected void stresstestihelp(){
        childJiraBusinessService.stressTest();
    }

    @GetMapping("/stresstestomnidev") // Ihelp
    protected void stresstestomnidev(){
        jiraBusinessService.stressTest();
    }

    @GetMapping("ticketsbuy") // get tickets ihelp
    protected void ticketsBuy(){jiraBusinessService.ticketsBuy();}

    @GetMapping("childTickets")
    protected void childTicketsBuy(){ChildJiraBusinessService.childTicketsBuy();}

    @GetMapping("/testFindChild")
    protected void testFindChild(@RequestParam("childKey") String childKey){
        Issue childIssue = childJiraBusinessService
                .getRestClient()
                .getSearchClient()
                .searchJql("project = MES2 AND labels = " + childKey)
                .claim().getIssues().iterator().next();
    }

    @PostMapping("/testCloneChild")
    protected void testCloneChild(@RequestParam("parentKey") String parentKey) {
        Issue parentIssue = getParentIssue(parentKey);
        Issue childIssue = getChildIssue(parentIssue);
        /*if (childIssue != null) {
            updateChildIssue(childIssue, parentIssue);
        } else {

        }*/
        cloneParentIssue(parentIssue);
    }

    @PostMapping("/testCloneIhelp")
    protected void testCloneIhelp(@RequestParam("key") String key) {
        Issue issue = getChildIssue(key);
        cloneChildIssue(issue);
    }

    private void cloneChildIssue(Issue issue) {
        jiraBusinessService.createIssueFromTest2(issue, "OMNIDEV");
    }

    private void cloneParentIssue(Issue issue) {
        String key = childJiraBusinessService.createIssueFromIhelp(issue, "MES2");
        jiraBusinessService.getRestClient().getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(key)).claim();
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

    private Issue getParentIssue(String issueKey) {
        Set<String> set = new HashSet<>();
        set.add("*all");
        return jiraBusinessService
                .getRestClient()
                .getSearchClient()
                .searchJql("project = OMNIDEV AND issuekey = " + issueKey,1,0,set)
                .claim().getIssues().iterator().next();
    }

    private Issue getChildIssue(String issueKey) {
        Set<String> set = new HashSet<>();
        set.add("*all");
        return childJiraBusinessService
                .getRestClient()
                .getSearchClient()
                .searchJql("project = MES2 AND issuekey = " + issueKey,1,0,set)
                .claim().getIssues().iterator().next();
    }

    private Issue getChildIssue(Issue issue) {
        String childKey = "";
        Iterable<Comment> comments = issue.getComments();
        for (Comment comment : comments) {
            if(comment.getBody().contains("MES")) {
                childKey = comment.getBody().toUpperCase().replaceAll("\\s+", "");
            }
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
}
