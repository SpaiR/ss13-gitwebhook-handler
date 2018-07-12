package io.github.spair.service.dmm;

import io.github.spair.byond.dme.Dme;
import io.github.spair.byond.dmm.DmmComparator;
import io.github.spair.byond.dmm.MapRegion;
import io.github.spair.byond.dmm.parser.Dmm;
import io.github.spair.byond.dmm.parser.DmmParser;
import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.dmm.entity.ModifiedDmm;
import io.github.spair.service.github.entity.PullRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class DmmService {

    private final ChunkDiffGenerator chunkDiffGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(DmmService.class);

    @Autowired
    public DmmService(final ChunkDiffGenerator chunkDiffGenerator) {
        this.chunkDiffGenerator = chunkDiffGenerator;
    }

    public ModifiedDmm createModifiedDmm(final PullRequestFile dmmFile, final Dme oldDme, final Dme newDme) {
        CompletableFuture<Dmm> oldDmmFuture = CompletableFuture.completedFuture(null);
        CompletableFuture<Dmm> newDmmFuture = CompletableFuture.completedFuture(null);

        File oldDmmFile = new File(oldDme.getAbsoluteRootPath() + File.separator + dmmFile.getFilename());
        File newDmmFile = new File(newDme.getAbsoluteRootPath() + File.separator + dmmFile.getFilename());

        switch (dmmFile.getStatus()) {
            case ADDED:
                newDmmFuture = CompletableFuture.supplyAsync(() -> DmmParser.parse(newDmmFile, newDme));
                break;
            case MODIFIED:
                oldDmmFuture = CompletableFuture.supplyAsync(() -> DmmParser.parse(oldDmmFile, oldDme));
                newDmmFuture = CompletableFuture.supplyAsync(() -> DmmParser.parse(newDmmFile, newDme));
                break;
            case REMOVED:
                oldDmmFuture = CompletableFuture.supplyAsync(() -> DmmParser.parse(oldDmmFile, oldDme));
                break;
        }

        try {
            CompletableFuture.allOf(oldDmmFuture, newDmmFuture).get();
            return new ModifiedDmm(dmmFile.getFilename(), oldDmmFuture.get(), newDmmFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error during DMM's parsing. DMM name: {}", dmmFile.getRealName());
            throw new RuntimeException(e);
        }
    }

    public DmmDiffStatus createDmmDiffStatus(final ModifiedDmm modifiedDmm) {
        final Optional<Dmm> oldDmm = modifiedDmm.getOldDmm();
        final Optional<Dmm> newDmm = modifiedDmm.getNewDmm();

        final Dmm toCompare;
        final Dmm compareWith;

        if (oldDmm.isPresent() && newDmm.isPresent()) {
            toCompare = oldDmm.get();
            compareWith = newDmm.get();
        } else if (oldDmm.isPresent()) {
            toCompare = oldDmm.get();
            compareWith = Dmm.EMPTY_MAP;
        } else if (newDmm.isPresent()) {
            toCompare = newDmm.get();
            compareWith = Dmm.EMPTY_MAP;
        } else {
            throw new IllegalArgumentException("One of DMM's should exist");
        }

        List<MapRegion> chunks = DmmComparator.compareByChunks(toCompare, compareWith).orElse(Collections.emptyList());
        List<DmmChunkDiff> dmmDiffChunks = chunkDiffGenerator.generate(
                chunks, oldDmm.orElse(null), newDmm.orElse(null)
        );

        DmmDiffStatus dmmDiffStatus = new DmmDiffStatus(modifiedDmm.getFilename());
        dmmDiffStatus.setDmmDiffChunks(dmmDiffChunks);
        return dmmDiffStatus;
    }
}
