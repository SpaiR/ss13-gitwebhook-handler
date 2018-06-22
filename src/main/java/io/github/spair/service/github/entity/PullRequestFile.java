package io.github.spair.service.github.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class PullRequestFile {

    private String sha;
    private String filename;
    private String rawUrl;
    private Status status;

    @Setter(AccessLevel.NONE)
    private String realName;

    public String getRealName() {
        if (filename != null) {
            if (realName != null) {
                return realName;
            }
            String[] splicedFilename = filename.split("/");
            realName = splicedFilename[splicedFilename.length - 1];
            return realName;
        } else {
            return null;
        }
    }

    public enum Status {
        ADDED, MODIFIED, REMOVED, UNDEFINED
    }
}
