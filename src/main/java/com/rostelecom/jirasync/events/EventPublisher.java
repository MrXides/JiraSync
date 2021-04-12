package com.rostelecom.jirasync.events;

import com.rostelecom.jirasync.enums.LogType;
import com.rostelecom.jirasync.enums.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author Jake Morgan {@literal <aleksey.tarasenkov@rt.ru>}
 */
@Service
public class EventPublisher {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object sender, String mes, LogType logType, OperationType operationType, String message){
        applicationEventPublisher.publishEvent(
                new GeneralEvent(sender, mes, operationType, logType, message));
    }
    public void publishStandardEvent(Object sender, String message, LogType logType){
        applicationEventPublisher.publishEvent(
                new GeneralEvent(sender, message, logType)
        );
    }
}
