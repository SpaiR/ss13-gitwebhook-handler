package io.github.spair.service.dmi.report;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.DmiComparator;
import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.service.DataGenerator;
import io.github.spair.service.dmi.DmiLoader;
import io.github.spair.service.dmi.entities.ReportEntry;
import io.github.spair.service.git.entities.PullRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class ReportEntryGenerator implements DataGenerator<PullRequestFile, Optional<ReportEntry>> {

    private final DmiLoader dmiLoader;
    private final StateDiffReportListGenerator stateDiffReportGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportEntryGenerator.class);

    @Autowired
    ReportEntryGenerator(final DmiLoader dmiLoader, final StateDiffReportListGenerator stateDiffReportGenerator) {
        this.dmiLoader = dmiLoader;
        this.stateDiffReportGenerator = stateDiffReportGenerator;
    }

    @Override
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
            ReportEntry.Metadata reportMetadata = reportEntry.getMetadata();
            ReportEntry.Duplication reportDuplication = reportEntry.getDuplication();

            if (!dmiDiff.isSame()) {
                reportEntry.setStateDiffReports(stateDiffReportGenerator.generate(dmiDiff));
            }

            oldDmi.ifPresent(dmi -> {
                reportMetadata.setOldMeta(dmi.getMetadata());
                reportEntry.setOldStatesNumber(dmi.getStates().size());

                if (dmi.isHasDuplicates()) {
                    reportDuplication.setOldDmiDuplicates(dmi.getDuplicateStatesNames());
                }
            });

            newDmi.ifPresent(dmi -> {
                reportMetadata.setNewMeta(dmi.getMetadata());
                reportEntry.setNewStatesNumber(dmi.getStates().size());

                if (dmi.isHasDuplicates()) {
                    reportDuplication.setNewDmiDuplicates(dmi.getDuplicateStatesNames());
                }
            });

            return Optional.of(reportEntry);
        } else {
            return Optional.empty();
        }
    }
}
