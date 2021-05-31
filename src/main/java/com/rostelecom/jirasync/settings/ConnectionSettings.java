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
        this.parentUserName = "ParentJiraUserName";
        this.parentPassword = "ParentJiraPassword";
        this.parentJiraUrl = "ParentJiraUrl";

        this.childUserName = "ChildJiraUserName";
        this.childPassword = "ChildJiraPassword";
        this.childJiraUrl = "ChildJiraUrl";
    }
}
