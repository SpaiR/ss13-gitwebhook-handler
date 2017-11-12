package io.github.spair.services.changelog.entities;

import lombok.Data;

import java.util.List;

@Data
public class Changelog {

    private String author;
    private List<ChangelogRow> changelogRows;

    public boolean isEmpty() {
        return changelogRows == null || changelogRows.size() == 0;
    }
}
