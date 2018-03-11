package io.github.spair.service.dmi.entities;

import io.github.spair.byond.dmi.DmiDiff;
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

    public void setStatus(final DmiDiff.Status status) {
        this.status = status.name().charAt(0) + status.name().substring(1).toLowerCase();
    }
}
