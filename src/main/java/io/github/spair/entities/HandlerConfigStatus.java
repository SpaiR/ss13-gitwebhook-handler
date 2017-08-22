package io.github.spair.entities;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HandlerConfigStatus {

    public final boolean allOk;
    public final boolean gitHubOk;
    public final boolean changelogOk;
}
