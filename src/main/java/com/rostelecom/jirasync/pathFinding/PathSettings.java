package com.rostelecom.jirasync.pathFinding;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jake Morgan {@literal <aleksey.tarasenkov@rt.ru>}
 */
@Service
@Getter
public class PathSettings {

    private final List<StatusTransition> statusTransitions = Arrays.asList(
            new StatusTransition(10000, "Сделать", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(21)),
                    new TransitionPath(3, "Исправление", Arrays.asList(11)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(11, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(11, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(11, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(11, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(11, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(11, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(21, 61)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(11, 31))
            )),

            new StatusTransition(3, "Исправление", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(31, 151)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(31, 151, 61)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(31))
            )),


            new StatusTransition(10300, "Code Review", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(171, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(171, 161)),
                    new TransitionPath(10500, "Update server", Arrays.asList(81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(171)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(171, 161, 31))
            )),
            new StatusTransition(10500, "Update server", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(91, 101, 131, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(91, 101, 131, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(91, 101, 131, 161, 71)),
                    new TransitionPath(10100, "Test case", Arrays.asList(91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(91, 101, 131)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(91, 101, 131, 161, 31))
            )),
            new StatusTransition(10100, "Test case", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(101, 131, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(101, 131, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(101, 131, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(101, 131, 161, 71, 81)),
                    new TransitionPath(10729, "Testing", Arrays.asList(101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(101, 131)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(91, 101, 131, 161, 31))
            )),
            new StatusTransition(10729, "Testing", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(131, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(131, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(131, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(131, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(131, 161, 71, 81, 91)),

                    new TransitionPath(11500, "Waiting merge", Arrays.asList(111)),
                    new TransitionPath(10001, "Done", Arrays.asList(111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(131)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(91, 101, 131, 161, 31))
            )),
            new StatusTransition(11500, "Waiting merge", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(121, 141, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(121, 141, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(121, 141, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(121, 141, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(121, 141, 161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(121, 141, 161, 71, 81, 91, 101)),
                    new TransitionPath(10001, "Done", Arrays.asList(121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(121, 141)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(121, 141, 161, 31, 151))
            )),
            new StatusTransition(10001, "Done", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(141, 161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(141, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(141, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(141, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(141, 161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(141, 161, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(141, 161, 71, 81, 91, 101, 111)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(141)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(141, 161, 31))
            )),
            new StatusTransition(10305, "Переоткрыта", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(161, 31, 151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(161, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(161, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(161, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(161, 31))
            )),
            new StatusTransition(10408, "В ожидании", Arrays.asList(
                    new TransitionPath(10405, "Отклонён", Arrays.asList(151)),
                    new TransitionPath(3, "Исправление", Arrays.asList(51)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(51, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(51, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(51, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(51, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(51, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(51, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(151, 61))
            )),
            new StatusTransition(10405, "Отклонен", Arrays.asList(
                    new TransitionPath(3, "Исправление", Arrays.asList(61, 161)),
                    new TransitionPath(10300, "Code Review", Arrays.asList(61, 161, 71)),
                    new TransitionPath(10500, "Update server", Arrays.asList(61, 161, 71, 81)),
                    new TransitionPath(10100, "Test case", Arrays.asList(61, 161, 71, 81, 91)),
                    new TransitionPath(10729, "Testing", Arrays.asList(61, 161, 71, 81, 91, 101)),
                    new TransitionPath(11500, "Waiting merge", Arrays.asList(61, 161, 71, 81, 91, 101, 111)),
                    new TransitionPath(10001, "Done", Arrays.asList(61, 161, 71, 81, 91, 101, 111, 121)),
                    new TransitionPath(10305, "Переоткрыта", Arrays.asList(61)),
                    new TransitionPath(10408, "В ожидании", Arrays.asList(61, 161, 31))
            ))
    );


}