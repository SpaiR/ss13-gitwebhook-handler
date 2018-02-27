package io.github.spair.service.git.entities;

import lombok.Data;

import java.util.Objects;

@Data
public class PullRequestFile {

    private String sha;
    private String filename;
    private String rawUrl;
    private Status status;

    public String getRealName() {
        if (Objects.nonNull(filename)) {
            String[] splicedFilename = filename.split("/");
            return splicedFilename[splicedFilename.length - 1];
        } else {
            return null;
        }
    }

    public enum Status {
        ADDED, MODIFIED, REMOVED, UNDEFINED
    }
}
