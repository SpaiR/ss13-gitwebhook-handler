package io.github.spair.service.pr.entity;

public enum PullRequestType {
    UNDEFINED,
    OPENED,
    LABELED,
    UNLABELED,
    EDITED,
    SYNCHRONIZE,
    MERGED,
    CLOSED
}
