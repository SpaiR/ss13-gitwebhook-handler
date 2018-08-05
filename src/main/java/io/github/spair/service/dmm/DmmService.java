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
import io.github.spair.util.FutureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DmmService {

    private final ChunkDiffGenerator chunkDiffGenerator;

    @Autowired
    public DmmService(final ChunkDiffGenerator chunkDiffGenerator) {
        this.chunkDiffGenerator = chunkDiffGenerator;
    }

    public List<ModifiedDmm> listModifiedDmms(final List<PullRequestFile> prFiles, final Dme oldDme, final Dme newDme) {
        return prFiles.stream()
                .map(dmmPrFile -> createModifiedDmm(dmmPrFile, oldDme, newDme))
                .collect(Collectors.toList());
    }

    private ModifiedDmm createModifiedDmm(final PullRequestFile dmmFile, final Dme oldDme, final Dme newDme) {
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

        FutureUtil.completeFutures(oldDmmFuture, newDmmFuture);

        Dmm oldDmm = FutureUtil.extractFuture(oldDmmFuture);
        Dmm newDmm = FutureUtil.extractFuture(newDmmFuture);

        return new ModifiedDmm(dmmFile.getFilename(), oldDmm, newDmm);
    }

    public List<DmmDiffStatus> listDmmDiffStatuses(final List<ModifiedDmm> modifiedDmms) {
        return modifiedDmms.stream().map(this::createDmmDiffStatus).collect(Collectors.toList());
    }

    public MapRegion createMapRegion(final ModifiedDmm modifiedDmm) {
        final ComparePair comparePair = resolveModifiedDmm(modifiedDmm);
        final Dmm toCompare = comparePair.getToCompare();
        final Dmm compareWith = comparePair.getCompareWith();
        return DmmComparator.compare(toCompare, compareWith).orElseThrow(RuntimeException::new);
    }

    private DmmDiffStatus createDmmDiffStatus(final ModifiedDmm modifiedDmm) {
        List<MapRegion> chunks = listMapRegions(modifiedDmm);
        List<DmmChunkDiff> dmmDiffChunks = chunkDiffGenerator.generate(
                chunks, modifiedDmm.getOldDmm().orElse(null), modifiedDmm.getNewDmm().orElse(null)
        );

        DmmDiffStatus dmmDiffStatus = new DmmDiffStatus(modifiedDmm.getFilename());
        dmmDiffStatus.setDmmDiffChunks(dmmDiffChunks);
        return dmmDiffStatus;
    }

    private List<MapRegion> listMapRegions(final ModifiedDmm modifiedDmm) {
        final ComparePair compairPair = resolveModifiedDmm(modifiedDmm);
        final Dmm toCompare = compairPair.getToCompare();
        final Dmm compareWith = compairPair.getCompareWith();
        return DmmComparator.compareByChunks(toCompare, compareWith).orElse(Collections.emptyList());
    }

    private ComparePair resolveModifiedDmm(final ModifiedDmm modifiedDmm) {
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

        return new ComparePair(toCompare, compareWith);
    }
}
