package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.comparator.DmiComparator;
import io.github.spair.byond.dmi.comparator.DmiDiff;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.ModifiedDmi;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.util.FutureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DmiService {

    private final DmiLoader dmiLoader;
    private final SpriteDiffStatusGenerator spriteDiffStatusGenerator;

    @Autowired
    public DmiService(final DmiLoader dmiLoader,
                      final SpriteDiffStatusGenerator spriteDiffStatusGenerator) {
        this.dmiLoader = dmiLoader;
        this.spriteDiffStatusGenerator = spriteDiffStatusGenerator;
    }

    public List<ModifiedDmi> listModifiedDmis(final List<PullRequestFile> dmiPrFiles) {
        return dmiPrFiles.stream().map(this::createModifiedDmi).collect(Collectors.toList());
    }

    private ModifiedDmi createModifiedDmi(final PullRequestFile dmiFile) {
        final String realName = dmiFile.getRealName();
        final String filename = dmiFile.getFilename();
        final String fileRawUrl = dmiFile.getRawUrl();

        CompletableFuture<Optional<Dmi>> oldDmiFuture = CompletableFuture.completedFuture(Optional.empty());
        CompletableFuture<Optional<Dmi>> newDmiFuture = CompletableFuture.completedFuture(Optional.empty());

        switch (dmiFile.getStatus()) {
            case ADDED:
                newDmiFuture = dmiLoader.loadFromUrl(realName, fileRawUrl);
                break;
            case MODIFIED:
                oldDmiFuture = dmiLoader.loadFromGitHub(realName, filename);
                newDmiFuture = dmiLoader.loadFromUrl(realName, fileRawUrl);
                break;
            case REMOVED:
                oldDmiFuture = dmiLoader.loadFromGitHub(realName, filename);
                break;
        }

        FutureUtil.completeFutures(oldDmiFuture, newDmiFuture);

        final Optional<Dmi> oldDmi = FutureUtil.extractFuture(oldDmiFuture);
        final Optional<Dmi> newDmi = FutureUtil.extractFuture(newDmiFuture);

        return new ModifiedDmi(filename, oldDmi.orElse(null), newDmi.orElse(null));
    }

    public List<DmiDiffStatus> listDmiDiffStatuses(final List<ModifiedDmi> modifiedDmis) {
        return modifiedDmis.stream()
                .map(this::createDmiDiffStatus).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<DmiDiffStatus> createDmiDiffStatus(final ModifiedDmi modifiedDmi) {
        final Optional<Dmi> oldDmi = modifiedDmi.getOldDmi();
        final Optional<Dmi> newDmi = modifiedDmi.getNewDmi();

        final DmiDiff dmiDiff = DmiComparator.compare(oldDmi.orElse(null), newDmi.orElse(null));

        if (dmiDiff.isSame()) {
            return Optional.empty();
        }

        DmiDiffStatus dmiDiffStatus = new DmiDiffStatus(modifiedDmi.getFilename());
        dmiDiffStatus.setSpritesDiffStatuses(spriteDiffStatusGenerator.generate(dmiDiff));

        oldDmi.ifPresent(dmi -> setOldDmiInfoToStatus(dmiDiffStatus, dmi));
        newDmi.ifPresent(dmi -> setNewDmiInfoToStatus(dmiDiffStatus, dmi));

        return Optional.of(dmiDiffStatus);
    }

    private void setOldDmiInfoToStatus(final DmiDiffStatus dmiDiffStatus, final Dmi oldDmi) {
        dmiDiffStatus.setOldStatesNumber(oldDmi.getStates().size());
        if (oldDmi.isHasDuplicates()) {
            dmiDiffStatus.setOldDuplicatesNames(oldDmi.getDuplicateStatesNames());
        }
    }

    private void setNewDmiInfoToStatus(final DmiDiffStatus dmiDiffStatus, final Dmi newDmi) {
        dmiDiffStatus.setNewStatesNumber(newDmi.getStates().size());
        if (newDmi.isHasDuplicates()) {
            dmiDiffStatus.setNewDuplicatesNames(newDmi.getDuplicateStatesNames());
        }
    }
}
