package io.github.spair.services.changelog;

import lombok.Data;

@Data
class ChangelogValidationStatus {

    private Status status = Status.VALID;
    private String message;

    enum Status {
        VALID, INVALID
    }
}
