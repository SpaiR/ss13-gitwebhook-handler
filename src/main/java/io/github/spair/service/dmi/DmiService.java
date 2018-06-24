package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.DmiComparator;
import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.ModifiedDmi;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.image.ImageUploaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class DmiService {

    private final DmiLoader dmiLoader;
    private final SpriteDiffStatusGenerator spriteDiffStatusGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(DmiService.class);

    @Autowired
    public DmiService(final DmiLoader dmiLoader,
                      final ImageUploaderService imageUploaderService) {
        this.dmiLoader = dmiLoader;
        this.spriteDiffStatusGenerator = new SpriteDiffStatusGenerator(imageUploaderService);
    }

    public ModifiedDmi createModifiedDmi(final PullRequestFile dmiFile) {
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

            final Optional<Dmi> oldDmi = oldDmiFuture.get();
            final Optional<Dmi> newDmi = newDmiFuture.get();

            return new ModifiedDmi(filename, oldDmi.orElse(null), newDmi.orElse(null));
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error during downloading DMI's. DMI name: {}", realName, e);
            throw new RuntimeException(e);
        }
    }

    public Optional<DmiDiffStatus> createDmiDiffStatus(final ModifiedDmi modifiedDmi) {
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
