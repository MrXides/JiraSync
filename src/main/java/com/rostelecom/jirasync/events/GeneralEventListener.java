package com.rostelecom.jirasync.events;

import com.rostelecom.jirasync.services.interfaces.JiraBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * @author Jake Morgan {@literal <aleksey.tarasenkov@rt.ru>}
 */
@Service
public class GeneralEventListener {
    @Autowired
    private JiraBusinessService jiraBusinessService;
    private static final Logger logger = LoggerFactory.getLogger(GeneralEventListener.class);
    @EventListener
    public void eventListener(GeneralEvent event) {
        switch (event.logType.name()){
            case "INFO":
                logger.info(event.toString());
                break;
            case "ERROR":
                logger.error(event.toString());
                jiraBusinessService.errorReport(event.toString());
            case "DEBUG":
                logger.debug(event.toString());
        }
    }
}
