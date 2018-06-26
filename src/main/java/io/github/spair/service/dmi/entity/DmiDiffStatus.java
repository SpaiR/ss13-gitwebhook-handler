package io.github.spair.service.dmi.entity;

import io.github.spair.byond.dmi.Dmi;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class DmiDiffStatus {

    private String filename;
    private int oldStatesNumber;
    private int newStatesNumber;
    private Set<String> oldDuplicatesNames;
    private Set<String> newDuplicatesNames;
    private List<DmiSpriteDiffStatus> spritesDiffStatuses;

    public DmiDiffStatus(final String filename) {
        this.filename = filename;
        this.oldDuplicatesNames = new HashSet<>();
        this.newDuplicatesNames = new HashSet<>();
        this.spritesDiffStatuses = new ArrayList<>();
    }

    public boolean isStateOverflow() {
        return oldStatesNumber > Dmi.MAX_STATES || newStatesNumber > Dmi.MAX_STATES;
    }

    public boolean isStateOverflowFixed() {
        return newStatesNumber <= Dmi.MAX_STATES;
    }

    public boolean isHasDuplicates() {
        return !oldDuplicatesNames.isEmpty() || !newDuplicatesNames.isEmpty();
    }

    public boolean isDuplicatesFixed() {
        return newDuplicatesNames.isEmpty();
    }
}
