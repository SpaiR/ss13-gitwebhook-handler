package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.Diff;
import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.byond.dmi.DmiMeta;
import io.github.spair.byond.dmi.DmiSprite;
import io.github.spair.service.dmi.entity.DmiSpriteDiffStatus;
import io.github.spair.service.image.ImageUploaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

final class DmiSpriteDiffStatusGenerator {

    private final ImageUploaderService imageUploader;

    private static final Logger LOGGER = LoggerFactory.getLogger(DmiSpriteDiffStatusGenerator.class);

    DmiSpriteDiffStatusGenerator(final ImageUploaderService imageUploader) {
        this.imageUploader = imageUploader;
    }

    List<DmiSpriteDiffStatus> generate(final DmiDiff dmiDiff) {
        final List<Diff> diffs = dmiDiff.getDiffs();
        final ExecutorService executor = createExecutor(diffs.size());
        final List<Callable<DmiSpriteDiffStatus>> callableList = createCallableList(dmiDiff);

        List<DmiSpriteDiffStatus> dmiSpriteDiffStatuses = Collections.emptyList();

        try {
            dmiSpriteDiffStatuses = executor.invokeAll(callableList, 2, TimeUnit.MINUTES)
                    .stream()
                    .map(dmiSpriteDiffStatusFuture -> {
                        try {
                            return dmiSpriteDiffStatusFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
        } catch (InterruptedException e) {
            LOGGER.error("Exception on creating DmiSpriteDiffStatus. Created reports: {}. Total diff: {}",
                    dmiSpriteDiffStatuses, diffs, e);
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        return dmiSpriteDiffStatuses;
    }

    private List<Callable<DmiSpriteDiffStatus>> createCallableList(final DmiDiff dmiDiff) {
        List<Callable<DmiSpriteDiffStatus>> callableList = new ArrayList<>();
        dmiDiff.getDiffs().forEach(diff -> callableList.add(() -> createDmiSpriteDiffStatus(dmiDiff, diff)));
        return callableList;
    }

    private DmiSpriteDiffStatus createDmiSpriteDiffStatus(final DmiDiff dmiDiff, final Diff diff) {
        DmiSpriteDiffStatus spriteDiffStatus = new DmiSpriteDiffStatus();

        final String stateName = diff.getStateName();
        final DmiMeta oldMeta = dmiDiff.getOldMeta();
        final DmiMeta newMeta = dmiDiff.getNewMeta();
        final DmiSprite oldSprite = diff.getOldSprite();
        final DmiSprite newSprite = diff.getNewSprite();

        spriteDiffStatus.setName(stateName);
        spriteDiffStatus.setSpriteWidth(determineValue(oldMeta, newMeta, DmiMeta::getSpritesWidth));
        spriteDiffStatus.setSpriteHeight(determineValue(oldMeta, newMeta, DmiMeta::getSpritesHeight));
        spriteDiffStatus.setDir(determineValue(oldSprite, newSprite, DmiSprite::getDir));
        spriteDiffStatus.setFrameNumber(determineValue(oldSprite, newSprite, DmiSprite::getFrameNum));
        spriteDiffStatus.setOldSpriteImageLink(getSpriteLink(oldSprite));
        spriteDiffStatus.setNewSpriteImageLink(getSpriteLink(newSprite));
        spriteDiffStatus.setStatus(diff.getStatus());

        return spriteDiffStatus;
    }

    private <T, V> V determineValue(@Nullable final T oldOne, @Nullable final T newOne, final Function<T, V> function) {
        if (oldOne != null) {
            return function.apply(oldOne);
        } else if (newOne != null) {
            return function.apply(newOne);
        } else {
            LOGGER.error("Unhandled case during value determining. Both objects are null");
            throw new IllegalArgumentException("Both objects can't be null");
        }
    }

    private String getSpriteLink(@Nullable final DmiSprite sprite) {
        if (sprite != null) {
            return imageUploader.uploadImage(sprite.getSpriteAsBase64());
        } else {
            return "";
        }
    }

    private ExecutorService createExecutor(final int diffEntriesSize) {
        if (diffEntriesSize > 1) {
            return Executors.newFixedThreadPool(diffEntriesSize / 2);
        } else {
            return Executors.newSingleThreadExecutor();
        }
    }
}
