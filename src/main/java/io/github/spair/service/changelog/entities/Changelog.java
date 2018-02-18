package io.github.spair.service.changelog.entities;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Changelog {

    private String author;
    private List<ChangelogRow> changelogRows;

    public boolean isEmpty() {
        return Objects.isNull(changelogRows) || changelogRows.isEmpty();
    }
}
