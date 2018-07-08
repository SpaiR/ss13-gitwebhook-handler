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
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@RunWith(PowerMockRunner.class)
@PrepareForTest({DmmParser.class, DmmComparator.class})
public class DmmServiceTest {

    @Mock
    private ChunkDiffGenerator chunkDiffGenerator;

    @Mock
    private Dme mockedOldDme;
    @Mock
    private Dme mockedNewDme;
    @Mock
    private Dmm mockedOldDmm;
    @Mock
    private Dmm mockedNewDmm;

    private DmmService dmmService;

    private File oldDmmFile = new File("oldRoot" + File.separator + "dmmFile");
    private File newDmmFile = new File("newRoot" + File.separator + "dmmFile");

    private Optional<List<MapRegion>> oldCompareToNullList = Optional.of(Lists.newArrayList(mock(MapRegion.class)));
    private Optional<List<MapRegion>> newCompareToNullList = Optional.of(Lists.newArrayList(mock(MapRegion.class)));
    private Optional<List<MapRegion>> oldCompareToNewList = Optional.of(Lists.newArrayList(mock(MapRegion.class)));

    private List<DmmChunkDiff> oldWithNullChunkDiffList = Lists.newArrayList(mock(DmmChunkDiff.class));
    private List<DmmChunkDiff> newWithNullChunkDiffList = Lists.newArrayList(mock(DmmChunkDiff.class));
    private List<DmmChunkDiff> oldWithNewChunkDiffList = Lists.newArrayList(mock(DmmChunkDiff.class));

    private static final String FILENAME = "filename";

    @Before
    public void setUp() {
        dmmService = new DmmService(chunkDiffGenerator);

        when(mockedOldDme.getAbsoluteRootPath()).thenReturn("oldRoot");
        when(mockedNewDme.getAbsoluteRootPath()).thenReturn("newRoot");

        PowerMockito.mockStatic(DmmParser.class);
        PowerMockito.when(DmmParser.parse(eq(oldDmmFile), any(Dme.class))).thenReturn(mockedOldDmm);
        PowerMockito.when(DmmParser.parse(eq(newDmmFile), any(Dme.class))).thenReturn(mockedNewDmm);

        PowerMockito.mockStatic(DmmComparator.class);
        PowerMockito.when(DmmComparator.compareByChunks(mockedOldDmm, null)).thenReturn(oldCompareToNullList);
        PowerMockito.when(DmmComparator.compareByChunks(mockedNewDmm, null)).thenReturn(newCompareToNullList);
        PowerMockito.when(DmmComparator.compareByChunks(mockedOldDmm, mockedNewDmm)).thenReturn(oldCompareToNewList);

        when(chunkDiffGenerator.generate(anyList(), eq(mockedOldDmm), eq(null))).thenReturn(oldWithNullChunkDiffList);
        when(chunkDiffGenerator.generate(anyList(), eq(mockedNewDmm), eq(null))).thenReturn(newWithNullChunkDiffList);
        when(chunkDiffGenerator.generate(anyList(), eq(mockedOldDmm), eq(mockedNewDmm))).thenReturn(oldWithNewChunkDiffList);
    }

    @Test
    public void testCreateModifiedDmmWhenAdded() {
        ModifiedDmm result = dmmService.createModifiedDmm(getPullRequestFile(PullRequestFile.Status.ADDED), mockedOldDme, mockedNewDme);
        assertEquals(Optional.empty(), result.getOldDmm());
        assertNotEquals(Optional.empty(), result.getNewDmm());
    }

    @Test
    public void testCreateModifiedDmmWhenModified() {
        ModifiedDmm result = dmmService.createModifiedDmm(getPullRequestFile(PullRequestFile.Status.MODIFIED), mockedOldDme, mockedNewDme);
        assertNotEquals(Optional.empty(), result.getOldDmm());
        assertNotEquals(Optional.empty(), result.getNewDmm());
    }

    @Test
    public void testCreateModifiedDmmWhenRemoved() {
        ModifiedDmm result = dmmService.createModifiedDmm(getPullRequestFile(PullRequestFile.Status.REMOVED), mockedOldDme, mockedNewDme);
        assertNotEquals(Optional.empty(), result.getOldDmm());
        assertEquals(Optional.empty(), result.getNewDmm());
    }

    @Test
    public void testCreateDmmDiffStatusWhenOldPresent() {
        DmmDiffStatus actual = dmmService.createDmmDiffStatus(getModifiedDmm(mockedOldDmm, null));
        assertEquals(FILENAME, actual.getFilename());
        assertEquals(oldWithNullChunkDiffList, actual.getDmmDiffChunks());
    }

    @Test
    public void testCreateDmmDiffStatusWhenNewPresent() {
        DmmDiffStatus actual = dmmService.createDmmDiffStatus(getModifiedDmm(mockedNewDmm, null));
        assertEquals(FILENAME, actual.getFilename());
        assertEquals(newWithNullChunkDiffList, actual.getDmmDiffChunks());
    }

    @Test
    public void testCreateDmmDiffStatusWhenOldAndNewPresent() {
        DmmDiffStatus actual = dmmService.createDmmDiffStatus(getModifiedDmm(mockedOldDmm, mockedNewDmm));
        assertEquals(FILENAME, actual.getFilename());
        assertEquals(oldWithNewChunkDiffList, actual.getDmmDiffChunks());
    }

    private PullRequestFile getPullRequestFile(PullRequestFile.Status status) {
        PullRequestFile pullRequestFile = new PullRequestFile();
        pullRequestFile.setFilename("dmmFile");
        pullRequestFile.setStatus(status);
        return pullRequestFile;
    }

    private ModifiedDmm getModifiedDmm(Dmm oldDmm, Dmm newDmm) {
        return new ModifiedDmm(FILENAME, oldDmm, newDmm);
    }
}