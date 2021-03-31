package com.rostelecom.jirasync.settings;

import lombok.Getter;
import org.springframework.stereotype.Service;


//Из Ihelp в Omnichat
//Ihelp = OMNIDEV
//Omnichat = MES
@Service
@Getter
public class JiraSettings {
    private final String parentProjectKey = "OMNIDEV"; //Ihelp
    private final String childProjectKey = "MES"; //Omnichat
}
