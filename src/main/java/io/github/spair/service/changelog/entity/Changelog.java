package io.github.spair.service.changelog.entity;

import lombok.Data;

import java.util.List;

@Data
public class Changelog {

    private String author;
    private List<ChangelogRow> changelogRows;

    public boolean isEmpty() {
        return changelogRows == null || changelogRows.isEmpty();
    }
}
