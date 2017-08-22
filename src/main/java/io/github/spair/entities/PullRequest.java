package io.github.spair.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PullRequest {

    private String author;
    private int number;
    private PullRequestType type;
    private String link;
    private String body;
}
