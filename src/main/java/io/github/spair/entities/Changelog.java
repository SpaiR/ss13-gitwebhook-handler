package io.github.spair.entities;

import lombok.Data;

import java.util.List;

@Data
public class Changelog {

    private String author;
    private List<ChangelogRow> changelogRows;
}
