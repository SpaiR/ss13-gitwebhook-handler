package io.github.spair.service.changelog.entities;

import lombok.Data;

@Data
public class ChangelogValidationStatus {

    private Status status = Status.VALID;
    private String message;

    public enum Status {
        VALID, INVALID
    }
}
