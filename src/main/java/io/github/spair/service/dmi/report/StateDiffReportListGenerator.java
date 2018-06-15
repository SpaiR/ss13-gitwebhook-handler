package io.github.spair.service.dmi.report;

import io.github.spair.byond.dmi.Diff;
import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.byond.dmi.DmiMeta;
import io.github.spair.byond.dmi.DmiSprite;
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
class StateDiffReportListGenerator {

    private final ImageUploaderService imageUploader;

    private static final Logger LOGGER = LoggerFactory.getLogger(StateDiffReportListGenerator.class);

    @Autowired
    StateDiffReportListGenerator(final ImageUploaderService imageUploader) {
        this.imageUploader = imageUploader;
    }

    @Nonnull
    List<StateDiffReport> generate(@Nonnull final DmiDiff dmiDiff) {
        final List<Diff> diffs = dmiDiff.getDiffs();
        final ExecutorService executor = createExecutor(diffs.size());
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
                    stateDiffReports, diffs, e);
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

        dmiDiff.getDiffs().forEach(diff ->
                callableList.add(() -> {
                    StateDiffReport stateDiffReport = new StateDiffReport();

                    final String stateName = diff.getStateName();
                    final DmiMeta oldMeta = dmiDiff.getOldMeta();
                    final DmiMeta newMeta = dmiDiff.getNewMeta();
                    final DmiSprite oldSprite = diff.getOldSprite();
                    final DmiSprite newSprite = diff.getNewSprite();

                    stateDiffReport.setName(stateName);
                    stateDiffReport.setSpriteWidth(determineValue(oldMeta, newMeta, DmiMeta::getSpritesWidth));
                    stateDiffReport.setSpriteHeight(determineValue(oldMeta, newMeta, DmiMeta::getSpritesHeight));
                    stateDiffReport.setDir(determineValue(oldSprite, newSprite, DmiSprite::getDir));
                    stateDiffReport.setFrameNumber(determineValue(oldSprite, newSprite, DmiSprite::getFrameNum));
                    stateDiffReport.setOldDmiLink(getSpriteLink(oldSprite));
                    stateDiffReport.setNewDmiLink(getSpriteLink(newSprite));
                    stateDiffReport.setStatus(diff.getStatus());

                    return stateDiffReport;
                })
        );

        return callableList;
    }

    private <T, V> V determineValue(@Nullable final T oldOne, @Nullable final T newOne, final Function<T, V> function) {
        if (Objects.nonNull(oldOne)) {
            return function.apply(oldOne);
        } else if (Objects.nonNull(newOne)) {
            return function.apply(newOne);
        } else {
            LOGGER.error("Unhandled case during value determining. Both objects are null");
            throw new IllegalArgumentException("Both objects can't be null");
        }
    }

    private String getSpriteLink(@Nullable final DmiSprite sprite) {
        if (Objects.nonNull(sprite)) {
            return imageUploader.uploadImage(sprite.getSpriteAsBase64());
        } else {
            return "";
        }
    }
}
