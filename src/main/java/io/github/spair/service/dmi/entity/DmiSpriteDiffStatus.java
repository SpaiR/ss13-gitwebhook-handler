package io.github.spair.service.dmi.entity;

import io.github.spair.byond.dmi.DiffStatus;
import io.github.spair.byond.dmi.SpriteDir;
import lombok.Data;

@Data
public class DmiSpriteDiffStatus {

    private String name;
    private SpriteDir dir;
    private int spriteWidth;
    private int spriteHeight;
    private int frameNumber;
    private String oldSpriteImageLink = "";
    private String newSpriteImageLink = "";
    private String status;

    public void setStatus(final DiffStatus status) {
        this.status = status.name().charAt(0) + status.name().substring(1).toLowerCase();
    }
}
