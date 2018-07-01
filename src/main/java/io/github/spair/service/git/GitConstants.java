package io.github.spair.service.git;

import org.eclipse.jgit.lib.Constants;

import java.io.File;

import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.lib.Constants.R_REMOTES;

@SuppressWarnings("WeakerAccess")
public final class GitConstants {

    public static final String MASTER = Constants.MASTER;

    public static final String REPOS_FOLDER = ".repos";
    public static final String MASTER_REPO_FOLDER = ".master";
    public static final String MASTER_REPO_PATH = REPOS_FOLDER + File.separator + MASTER_REPO_FOLDER;

    public static final String MASTER_REMOTE = "master-remote";
    public static final String MASTER_REMOTE_URL = "../" + MASTER_REPO_FOLDER;
    public static final String MASTER_REMOTE_FETCH = "+" + R_HEADS + MASTER + ":" + R_REMOTES + MASTER;

    private GitConstants() {
    }
}
