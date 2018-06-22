package io.github.spair.service.changelog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogRow {

    private String className;
    private boolean hasLink;
    private String changes;
}
