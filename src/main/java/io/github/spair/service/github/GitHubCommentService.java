package io.github.spair.service.github;

import io.github.spair.service.github.entity.IssueComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

@Service
public class GitHubCommentService {

    private final GitHubService gitHubService;

    @Autowired
    public GitHubCommentService(final GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public void removeCommentWithId(final int issueNum, final String id) {
        List<IssueComment> issueComments = gitHubService.listIssueComments(issueNum);
        Integer commentId = getCommentId(id, issueComments);
        if (commentId != null) {
            gitHubService.removeComment(commentId);
        }
    }

    public void sendCommentOrUpdate(final int issueNum, final String comment, final String id) {
        List<IssueComment> issueComments = gitHubService.listIssueComments(issueNum);
        Integer commentId = getCommentId(id, issueComments);

        if (commentId != null) {
            gitHubService.editIssueComment(commentId, comment);
        } else {
            gitHubService.createIssueComment(issueNum, comment);
        }
    }

    @Nullable
    private Integer getCommentId(final String id, final List<IssueComment> issueComments) {
        for (IssueComment prComment : issueComments) {
            if (prComment.getBody().contains(id)) {
                return prComment.getId();
            }
        }
        return null;
    }
}
