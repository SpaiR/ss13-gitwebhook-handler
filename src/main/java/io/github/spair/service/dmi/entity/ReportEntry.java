package io.github.spair.service.dmi.entity;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.DmiMeta;
import lombok.Data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ReportEntry {

    @Nonnull private String filename;
    private int oldStatesNumber;
    private int newStatesNumber;
    @Nonnull private Duplication duplication = new Duplication();
    @Nonnull private List<StateDiffReport> stateDiffReports = new ArrayList<>();
    @Nonnull private Metadata metadata = new Metadata();

    public ReportEntry(@Nonnull final String filename) {
        this.filename = filename;
    }

    public boolean isStateOverflow() {
        return oldStatesNumber > Dmi.MAX_STATES || newStatesNumber > Dmi.MAX_STATES;
    }

    public void setNewMetadata(final DmiMeta newDmiMeta) {
        metadata.newMeta = newDmiMeta;
    }

    public void setOldMetadata(final DmiMeta oldDmiMeta) {
        metadata.oldMeta = oldDmiMeta;
    }

    public void setNewDmiDuplicates(final Set<String> newDmiDuplicates) {
        duplication.newDmiDuplicates = newDmiDuplicates;
    }

    public void setOldDmiDuplicates(final Set<String> oldDmiDuplicates) {
        duplication.oldDmiDuplicates = oldDmiDuplicates;
    }

    @Data
    public static class Duplication {
        @Nonnull private Set<String> oldDmiDuplicates = new HashSet<>();
        @Nonnull private Set<String> newDmiDuplicates = new HashSet<>();

        public boolean isHasDuplicates() {
            return !oldDmiDuplicates.isEmpty() || !newDmiDuplicates.isEmpty();
        }
    }

    @Data
    public static class Metadata {
        @Nullable private DmiMeta oldMeta;
        @Nullable private DmiMeta newMeta;
    }
}
