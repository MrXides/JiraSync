package com.rostelecom.jirasync.services;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.rostelecom.jirasync.enums.ProjectKey;
import com.rostelecom.jirasync.services.interfaces.JiraService;
import com.rostelecom.jirasync.services.interfaces.Synchronizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
//TODO: !!!
// 1. Пофиксить ссылки
// 5. Приделать БД с юзерами
public class SynchronizerImpl implements Synchronizer {

    @Autowired
    @Qualifier("JiraOmnichatService")
    private JiraService omnichatService;

    @Autowired
    @Qualifier("JiraIHelpService")
    private JiraService iHelpService;

    /**
     * Функция синронизации - синхронизирует все задачи из омничата и Ihelp друг с другом, дублирует задачи
     * из одной системы в другую, если необходимо.
     */
    @Override
    public void synchronize() {
        List<String> omnichatIssueKeyList = omnichatService.getIssueKeyList();
        List<String> iHelpIssueKeyList = iHelpService.getIssueKeyList();

        if (!omnichatIssueKeyList.isEmpty() && !iHelpIssueKeyList.isEmpty()) {
            for (String omnichatIssueKey : omnichatIssueKeyList) {
                for (String iHelpIssueKey : iHelpIssueKeyList) {
                    Issue iHelpIssue = iHelpService.getIssue(iHelpIssueKey);
                    Issue omnichatIssue = omnichatService.getIssue(omnichatIssueKey);
                    if (isDuplicatedEquals(iHelpIssue, omnichatIssue)) {
                        updateIssue(iHelpIssue, omnichatIssue);
                    }
                }
            }
        } else {
            if (!omnichatIssueKeyList.isEmpty()) {
                for (String omnichatIssueKey : omnichatIssueKeyList) {
                    Issue omnichatIssue = omnichatService.getIssue(omnichatIssueKey);
                    if (!Optional.ofNullable(findStr(ProjectKey.OMNIDEV.name(), omnichatIssue.getComments())).isPresent()) {
                        duplicateIssueInIHelp(omnichatIssue);
                    }
                }
            } else if (!iHelpIssueKeyList.isEmpty()) {
                for (String iHelpIssueKey : iHelpIssueKeyList) {
                    Issue iHelpIssue = iHelpService.getIssue(iHelpIssueKey);
                    if (!Optional.ofNullable(findStr(ProjectKey.MES2.name(), iHelpIssue.getComments())).isPresent()) {
                        duplicateIssueInOmnichat(iHelpIssue);
                    }
                }
            } else {
                log.error("Ошибка при синхронизации: списки задач пустые");
            }
        }
    }

    private void updateIssue(Issue iHelpIssue, Issue omnichatIssue) {
        if (iHelpIssue.getUpdateDate().isAfter(omnichatIssue.getUpdateDate())) {
            omnichatService.updateIssue(omnichatIssue, iHelpIssue);
        } else iHelpService.updateIssue(iHelpIssue, omnichatIssue);
        updateComments(iHelpIssue, omnichatIssue);
    }

    private void updateComments(Issue iHelpIssue, Issue omnichatIssue) {
        List<Comment> commonComments = getCommentList(iHelpIssue.getComments(), omnichatIssue.getComments());
        omnichatService.updateComments(filterCommentList(commonComments, omnichatIssue.getComments()), omnichatIssue.getCommentsUri());
        iHelpService.updateComments(filterCommentList(commonComments, iHelpIssue.getComments()), iHelpIssue.getCommentsUri());
    }

    private List<Comment> filterCommentList(List<Comment> commonComments, Iterable<Comment> comments) {
        return commonComments.stream()
                .filter(commonComment -> {
                    for (Comment comment : comments) {
                        if (commonComment.getBody().equals(comment.getBody())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<Comment> getCommentList(Iterable<Comment> iHelpComments, Iterable<Comment> omnichatComments) {
        ArrayList<Comment> result = new ArrayList<>();
        iHelpComments.forEach(result::add);
        result.removeAll(result.stream()
                .filter(iHelpComment -> {
                    for (Comment omnichatComment : omnichatComments) {
                        if (iHelpComment.getBody().equals(omnichatComment.getBody())) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList()));
        omnichatComments.forEach(result::add);
        result.removeIf(com -> isCommMatchStr(ProjectKey.OMNIDEV.name(), com) || isCommMatchStr(ProjectKey.MES2.name(), com));
        return result;
    }

    /**
     * Фукнция проверяет являются ли переданные задачи дупликатами.
     * В случае если нет и комменатрия типа "MES" или "OMNIDEV" в задаче нет, значит дублируем эту задачу.
     */
    private boolean isDuplicatedEquals(Issue iHelpIssue, Issue omnichatIssue) {
        Optional<String> comIHelp = Optional.ofNullable(findStr(ProjectKey.MES2.name(), iHelpIssue.getComments()));
        Optional<String> comOmnichat = Optional.ofNullable(findStr(ProjectKey.OMNIDEV.name(), omnichatIssue.getComments()));

        if (comIHelp.isPresent()) {
            return comIHelp.get().equals(omnichatIssue.getKey());
        } else duplicateIssueInOmnichat(iHelpIssue);

        if (comOmnichat.isPresent()) {
            return comOmnichat.get().equals(iHelpIssue.getKey());
        } else duplicateIssueInIHelp(omnichatIssue);

        return false;
    }

    /**
     * Копирует задачу из IHelp в омничат
     */
    private void duplicateIssueInOmnichat(Issue iHelpIssue) {
        BasicIssue basicIssue = omnichatService.duplicateIssue(iHelpIssue);
        iHelpService.getJiraRestClient().getIssueClient().addComment(iHelpIssue.getCommentsUri(), Comment.valueOf(basicIssue.getKey() + "_" + basicIssue.getSelf().toString())).claim();
    }

    /**
     * Копирует задачу из Омничата в IHelp
     */
    private void duplicateIssueInIHelp(Issue omnichatIssue) {
        BasicIssue basicIssue = iHelpService.duplicateIssue(omnichatIssue);
        omnichatService.getJiraRestClient().getIssueClient().addComment(omnichatIssue.getCommentsUri(), Comment.valueOf(basicIssue.getKey() + "_" + basicIssue.getSelf().toString())).claim();
    }

    /**
     * Находит среди комментариев к Issue строку вида *ПРОЕКТ*-*НОМЕР ЗАДАЧИ*_*ССЫЛКА НА ЗАДАЧУ* (прим. MES-231_http\\...)
     * и возвращает этот комментарий без ссылки на саму задачу
     * @param str - название проекта, указываемое в задаче
     * @param comments - комментарии к задаче
     */
    private String findStr(String str, Iterable<Comment> comments) {
        for (Comment comment : comments) {
            if (isCommMatchStr(str, comment)) {
                return comment.getBody().substring(0, comment.getBody().indexOf("_"));
            }
        }
        return null;
    }

    private boolean isCommMatchStr(String str, Comment comment) {
        return comment.getBody().matches(str + "-\\d+_.+");
    }
}
