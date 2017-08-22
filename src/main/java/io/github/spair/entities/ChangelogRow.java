package io.github.spair.entities;

import lombok.Data;

@Data
public class ChangelogRow {

    private String className;
    private boolean hasLink;
    private String changes;
}
