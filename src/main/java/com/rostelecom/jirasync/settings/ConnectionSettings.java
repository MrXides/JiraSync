package com.rostelecom.jirasync.settings;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ConnectionSettings {
    /*
        Jira родитель
     */
    private final String parentUserName;
    private final String parentPassword;
    private final String parentJiraUrl;

    /*
        Jira наследник
     */
    private final String childUserName;
    private final String childPassword;
    private final String childJiraUrl;

    public ConnectionSettings(){
        this.parentUserName = System.getenv("ParentJiraUserName");
        this.parentPassword = System.getenv("ParentJiraPassword");
        this.parentJiraUrl = System.getenv("ParentJiraUrl");

        this.childUserName = System.getenv("ChildJiraUserName");
        this.childPassword = System.getenv("ChildJiraPassword");
        this.childJiraUrl = System.getenv("ChildJiraUrl");
    }
}
