package io.github.spair.service.dmi.entities;

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

    @Nonnull
    private String filename;
    @Nonnull
    private Duplication duplication = new Duplication();
    @Nonnull
    private List<StateDiffReport> stateDiffReports = new ArrayList<>();
    @Nonnull
    private Metadata metadata = new Metadata();

    public ReportEntry(final @Nonnull String filename) {
        this.filename = filename;
    }

    @Data
    public static class Duplication {
        @Nonnull
        private Set<String> oldDmiDuplicates = new HashSet<>();
        @Nonnull
        private Set<String> newDmiDuplicates = new HashSet<>();

        public boolean isHasDuplicates() {
            return !oldDmiDuplicates.isEmpty() || !newDmiDuplicates.isEmpty();
        }
    }

    @Data
    public static class Metadata {
        @Nullable
        private DmiMeta oldMeta;
        @Nullable
        private DmiMeta newMeta;
        @Nonnull
        private String metadataDiff = "";
    }
}
