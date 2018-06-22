package io.github.spair.service.issue.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    private int number;
    private String title;
    private IssueType type;
}
