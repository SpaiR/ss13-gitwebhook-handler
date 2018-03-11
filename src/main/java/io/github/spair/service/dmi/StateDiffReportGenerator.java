package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.byond.dmi.DmiMeta;
import io.github.spair.byond.dmi.DmiSprite;
import io.github.spair.service.DataGenerator;
import io.github.spair.service.dmi.entities.StateDiffReport;
import io.github.spair.service.image.ImageUploaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Component
class StateDiffReportGenerator implements DataGenerator<DmiDiff, List<StateDiffReport>> {

    private final ImageUploaderService imageUploader;

    private static final Logger LOGGER = LoggerFactory.getLogger(StateDiffReportGenerator.class);

    @Autowired
    StateDiffReportGenerator(final ImageUploaderService imageUploader) {
        this.imageUploader = imageUploader;
    }

    @Override
    @Nonnull
    public List<StateDiffReport> generate(final @Nonnull DmiDiff dmiDiff) {
        final List<DmiDiff.DiffEntry> diffEntries = dmiDiff.getDiffEntries();
        final ExecutorService executor = createExecutor(diffEntries.size());
        final List<Callable<StateDiffReport>> callableList = createCallableList(dmiDiff);

        List<StateDiffReport> stateDiffReports = new ArrayList<>();

        try {
            executor.invokeAll(callableList, 2, TimeUnit.MINUTES)
                    .stream()
                    .map(stateDiffReportFuture -> {
                        try {
                            return stateDiffReportFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(stateDiffReports::add);
        } catch (InterruptedException e) {
            LOGGER.error("Exception on creating StateDiffReport. Created reports: {}. Total diff: {}",
                    stateDiffReports, diffEntries, e);
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        return stateDiffReports;
    }

    private ExecutorService createExecutor(final int diffEntriesSize) {
        if (diffEntriesSize > 1) {
            return Executors.newFixedThreadPool(diffEntriesSize / 2);
        } else {
            return Executors.newSingleThreadExecutor();
        }
    }

    private List<Callable<StateDiffReport>> createCallableList(final DmiDiff dmiDiff) {
        List<Callable<StateDiffReport>> callableList = new ArrayList<>();

        dmiDiff.getDiffEntries().forEach(diffEntry ->
                callableList.add(() -> {
                    StateDiffReport stateDiffReport = new StateDiffReport();

                    final String stateName = diffEntry.getStateName();
                    final DmiMeta oldMeta = dmiDiff.getOldMeta();
                    final DmiMeta newMeta = dmiDiff.getNewMeta();
                    final DmiSprite oldSprite = diffEntry.getOldSprite();
                    final DmiSprite newSprite = diffEntry.getNewSprite();

                    stateDiffReport.setName(stateName);
                    stateDiffReport.setSpriteWidth(determineValue(oldMeta, newMeta, DmiMeta::getSpritesWidth));
                    stateDiffReport.setSpriteHeight(determineValue(oldMeta, newMeta, DmiMeta::getSpritesHeight));
                    stateDiffReport.setDir(determineValue(oldSprite, newSprite, DmiSprite::getSpriteDir));
                    stateDiffReport.setFrameNumber(determineValue(oldSprite, newSprite, DmiSprite::getSpriteFrameNum));
                    stateDiffReport.setOldDmiLink(getSpriteLink(oldSprite));
                    stateDiffReport.setNewDmiLink(getSpriteLink(newSprite));
                    stateDiffReport.setStatus(diffEntry.getStatus());

                    return stateDiffReport;
                })
        );

        return callableList;
    }

    private <T, V> V determineValue(final @Nullable T oldOne, final @Nullable T newOne, final Function<T, V> function) {
        if (Objects.nonNull(oldOne)) {
            return function.apply(oldOne);
        } else if (Objects.nonNull(newOne)) {
            return function.apply(newOne);
        } else {
            LOGGER.error("Unhandled case during value determining. Both objects are null");
            throw new IllegalArgumentException("Both objects can't be null");
        }
    }

    private String getSpriteLink(final @Nullable DmiSprite sprite) {
        if (Objects.nonNull(sprite)) {
            return imageUploader.uploadImage(sprite.getSpriteAsBase64());
        } else {
            return "";
        }
    }
}
