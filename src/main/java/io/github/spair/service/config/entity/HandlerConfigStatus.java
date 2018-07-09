package io.github.spair.service.config.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@SuppressWarnings("checkstyle:VisibilityModifier")
public class HandlerConfigStatus {

    public final boolean allOk;
    public final boolean gitHubOk;
    public final boolean changelogOk;
    public final boolean botOk;
}
