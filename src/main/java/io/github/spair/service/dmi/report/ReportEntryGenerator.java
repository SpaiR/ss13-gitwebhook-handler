package io.github.spair.service.dmi.report;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.DmiComparator;
import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.service.dmi.DmiLoader;
import io.github.spair.service.dmi.entity.ReportEntry;
import io.github.spair.service.git.entity.PullRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class ReportEntryGenerator {

    private final DmiLoader dmiLoader;
    private final StateDiffReportListGenerator stateDiffReportGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportEntryGenerator.class);

    @Autowired
    public ReportEntryGenerator(
            final DmiLoader dmiLoader, final StateDiffReportListGenerator stateDiffReportGenerator) {
        this.dmiLoader = dmiLoader;
        this.stateDiffReportGenerator = stateDiffReportGenerator;
    }

    public Optional<ReportEntry> generate(@Nonnull final PullRequestFile dmiFile) {
        final Optional<Dmi> oldDmi;
        final Optional<Dmi> newDmi;

        final String realName = dmiFile.getRealName();
        final String filename = dmiFile.getFilename();
        final String fileRawUrl = dmiFile.getRawUrl();

        try {
            CompletableFuture<Optional<Dmi>> oldDmiFuture = CompletableFuture.completedFuture(Optional.empty());
            CompletableFuture<Optional<Dmi>> newDmiFuture = CompletableFuture.completedFuture(Optional.empty());

            switch (dmiFile.getStatus()) {
                case ADDED:
                    oldDmiFuture = dmiLoader.loadFromUrl(realName, fileRawUrl);
                    break;
                case MODIFIED:
                    newDmiFuture = dmiLoader.loadFromGitHub(realName, filename);
                    oldDmiFuture = dmiLoader.loadFromUrl(realName, fileRawUrl);
                    break;
                case REMOVED:
                    newDmiFuture = dmiLoader.loadFromGitHub(realName, filename);
                    break;
            }

            CompletableFuture.allOf(oldDmiFuture, newDmiFuture);

            oldDmi = newDmiFuture.get();
            newDmi = oldDmiFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error during downloading DMI's. DMI name: {}", realName, e);
            throw new RuntimeException(e);
        }

        if (oldDmi.isPresent() || newDmi.isPresent()) {
            final DmiDiff dmiDiff = DmiComparator.compare(oldDmi.orElse(null), newDmi.orElse(null));

            ReportEntry reportEntry = new ReportEntry(filename);

            if (!dmiDiff.isSame()) {
                reportEntry.setStateDiffReports(stateDiffReportGenerator.generate(dmiDiff));
            }

            oldDmi.ifPresent(dmi -> setOldDmiInfoToReport(reportEntry, dmi));
            newDmi.ifPresent(dmi -> setNewDmiInfoToReport(reportEntry, dmi));

            return Optional.of(reportEntry);
        } else {
            return Optional.empty();
        }
    }

    private void setOldDmiInfoToReport(final ReportEntry report, final Dmi oldDmi) {
        report.setOldMetadata(oldDmi.getMetadata());
        report.setOldStatesNumber(oldDmi.getStates().size());

        if (oldDmi.isHasDuplicates()) {
            report.setOldDmiDuplicates(oldDmi.getDuplicateStatesNames());
        }
    }

    private void setNewDmiInfoToReport(final ReportEntry report, final Dmi newDmi) {
        report.setNewMetadata(newDmi.getMetadata());
        report.setNewStatesNumber(newDmi.getStates().size());

        if (newDmi.isHasDuplicates()) {
            report.setNewDmiDuplicates(newDmi.getDuplicateStatesNames());
        }
    }
}
