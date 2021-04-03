package com.rostelecom.jirasync.testControllers;

import com.rostelecom.jirasync.business.ChildJiraBusinessService;
import com.rostelecom.jirasync.business.JiraBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        return jiraBusinessService.createIssue(projectKey, issueType, issueSummary);
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

    @GetMapping("/testChild")
    protected void testChild() {
        childJiraBusinessService.test();
    }


}
