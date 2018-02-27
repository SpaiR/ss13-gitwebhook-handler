package io.github.spair.service.dmi.entities;

import lombok.Data;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Data
public class DmiDiffReport {

    public static final String TITLE = "## DMI Diff Report";
    @Nonnull
    private List<ReportEntry> reportEntries = new ArrayList<>();
}
