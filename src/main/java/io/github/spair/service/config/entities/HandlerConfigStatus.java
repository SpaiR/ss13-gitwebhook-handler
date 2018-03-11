package io.github.spair.service.config.entities;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@SuppressWarnings("checkstyle:VisibilityModifier")
public class HandlerConfigStatus {

    public final boolean allOk;
    public final boolean gitHubOk;
    public final boolean changelogOk;
}
