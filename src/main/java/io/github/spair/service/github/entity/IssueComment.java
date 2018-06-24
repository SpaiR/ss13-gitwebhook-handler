package io.github.spair.service.github.entity;

import lombok.Data;

@Data
public class IssueComment {

    private int id;
    private String userName;
    private String body;
}
