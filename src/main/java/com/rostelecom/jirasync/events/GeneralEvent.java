package com.rostelecom.jirasync.events;

import com.rostelecom.jirasync.enums.LogType;
import com.rostelecom.jirasync.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

/**
 * @author Jake Morgan {@literal <aleksey.tarasenkov@rt.ru>}
 */
@Getter
@Setter
@AllArgsConstructor
public class GeneralEvent<T> {
    private T sender;
    protected String mes;
    protected OperationType operationType;
    protected LogType logType;
    protected String message;

    public GeneralEvent(T sender, String message, LogType logType){
        this.sender = sender;
        this.message = message;
        this.logType = logType;
    }

    @Override
    public String toString() {
        if(mes == null){
            return message;
        }else
        return new StringJoiner(", ", GeneralEvent.class.getSimpleName() + "[", "]")
                .add("Sender = " + sender)
                .add("MES =" + mes)
                .add("Operation Type = " + operationType)
                .add("Message = " + message)
                .toString();
    }
}
