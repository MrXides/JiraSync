package com.rostelecom.jirasync.testControllers;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.rostelecom.jirasync.services.interfaces.JiraService;
import com.rostelecom.jirasync.services.interfaces.Synchronizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/restIssue")
@Slf4j
public class RestIssue {

    @Autowired
    @Qualifier("JiraOmnichatService")
    private JiraService omnichatService;
    @Autowired
    @Qualifier("JiraIHelpService")
    private JiraService iHelpService;
    @Autowired
    private Synchronizer synchronizer;

    @PostMapping("/createIssue")
    protected String createIssue(@RequestParam("projectKey") String projectKey,
                                      @RequestParam("issueType") Long issueType,
                                      @RequestParam("issueSummary") String issueSummary) {
        return omnichatService.createIssue(projectKey, issueType, issueSummary);
    }

    @PostMapping("/synchronize")
    protected void synchronize() {
        synchronizer.synchronize();
    }
}
