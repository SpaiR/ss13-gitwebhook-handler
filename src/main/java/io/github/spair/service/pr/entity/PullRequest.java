package io.github.spair.service.pr.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequest {

    private String author;
    private String branchName;
    private String sourceHeadName;
    private int number;
    private String title;
    private PullRequestType type;
    private String link;
    private String diffLink;
    private String body;
    private Set<String> labels;
    private String sender;
    private String touchedLabel;
}
