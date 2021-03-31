package com.rostelecom.jirasync.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Getter
public class ConnectionSettings {
    /*
        Jira родитель
     */
    private final String parentUserName = "omnichat-jira";
    private final String parentPassword = "D5wZuCbZ";
    private final String parentJiraUrl = "http://ihelp.rt.ru";

    /*
        Jira наследник
     */
    private final String childUserName = "t.jira";
    private final String childPassword = "FlD3MPBt";
    private final String childJiraUrl = "http://jira.omnichat.tech";


}
