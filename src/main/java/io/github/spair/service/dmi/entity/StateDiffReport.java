package io.github.spair.service.dmi.entity;

import io.github.spair.byond.dmi.DiffStatus;
import io.github.spair.byond.dmi.SpriteDir;
import lombok.Data;

import javax.annotation.Nonnull;

@Data
public class StateDiffReport {
    private String name;
    private SpriteDir dir;
    private int spriteWidth;
    private int spriteHeight;
    private int frameNumber;
    @Nonnull
    private String oldDmiLink = "";
    @Nonnull
    private String newDmiLink = "";
    private String status;

    public void setStatus(final DiffStatus status) {
        this.status = status.name().charAt(0) + status.name().substring(1).toLowerCase();
    }
}
