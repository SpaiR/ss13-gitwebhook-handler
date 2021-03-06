package io.github.spair.service.dmm;

import io.github.spair.byond.dme.Dme;
import io.github.spair.byond.dmm.Dmm;
import io.github.spair.byond.dmm.MapRegion;
import io.github.spair.byond.dmm.comparator.DmmComparator;
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

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({DmmParser.class, DmmComparator.class})
public class DmmServiceTest {

    @Mock
    private ChunkDiffGenerator chunkDiffGenerator;

    private static final Dmm EMPTY_MAP = new Dmm();

    @Mock
    private Dme mockedOldDme;
    @Mock
    private Dme mockedNewDme;
    @Mock
    private Dmm mockedOldDmm;
    @Mock
    private Dmm mockedNewDmm;

    private DmmService dmmService;

    private File oldDmmFile = new File("oldRoot" + File.separator + "dmmFile.dmm");
    private File newDmmFile = new File("newRoot" + File.separator + "dmmFile.dmm");

    private Optional<List<MapRegion>> oldCompareToNullList = Optional.of(Lists.newArrayList(mock(MapRegion.class)));
    private Optional<List<MapRegion>> newCompareToNullList = Optional.of(Lists.newArrayList(mock(MapRegion.class)));
    private Optional<List<MapRegion>> oldCompareToNewList = Optional.of(Lists.newArrayList(mock(MapRegion.class)));

    private List<DmmChunkDiff> oldWithNullChunkDiffList = Lists.newArrayList(mock(DmmChunkDiff.class));
    private List<DmmChunkDiff> newWithNullChunkDiffList = Lists.newArrayList(mock(DmmChunkDiff.class));
    private List<DmmChunkDiff> oldWithNewChunkDiffList = Lists.newArrayList(mock(DmmChunkDiff.class));

    private static final String FILENAME = "/filename.dmm";

    @Before
    public void setUp() {
        dmmService = new DmmService(chunkDiffGenerator);

        when(mockedOldDme.getAbsoluteRootPath()).thenReturn("oldRoot");
        when(mockedNewDme.getAbsoluteRootPath()).thenReturn("newRoot");

        PowerMockito.mockStatic(DmmParser.class);
        PowerMockito.when(DmmParser.parse(eq(oldDmmFile), any(Dme.class))).thenReturn(mockedOldDmm);
        PowerMockito.when(DmmParser.parse(eq(newDmmFile), any(Dme.class))).thenReturn(mockedNewDmm);

        PowerMockito.mockStatic(DmmComparator.class);
        PowerMockito.when(DmmComparator.compareByChunks(eq(mockedOldDmm), eq(EMPTY_MAP))).thenReturn(oldCompareToNullList);
        PowerMockito.when(DmmComparator.compareByChunks(eq(mockedNewDmm), eq(EMPTY_MAP))).thenReturn(newCompareToNullList);
        PowerMockito.when(DmmComparator.compareByChunks(eq(mockedOldDmm), eq(mockedNewDmm))).thenReturn(oldCompareToNewList);

        when(chunkDiffGenerator.generate(anyList(), eq(mockedOldDmm), eq(null))).thenReturn(oldWithNullChunkDiffList);
        when(chunkDiffGenerator.generate(anyList(), eq(null), eq(mockedNewDmm))).thenReturn(newWithNullChunkDiffList);
        when(chunkDiffGenerator.generate(anyList(), eq(mockedOldDmm), eq(mockedNewDmm))).thenReturn(oldWithNewChunkDiffList);
    }

    @Test
    public void testListModifiedDmmsWhenAdded() {
        List<ModifiedDmm> result = dmmService.listModifiedDmms(Lists.newArrayList(getPullRequestFile(PullRequestFile.Status.ADDED)), mockedOldDme, mockedNewDme);
        assertEquals(Optional.empty(), result.get(0).getOldDmm());
        assertNotEquals(Optional.empty(), result.get(0).getNewDmm());
    }

    @Test
    public void testListModifiedDmmsWhenModified() {
        List<ModifiedDmm> result = dmmService.listModifiedDmms(Lists.newArrayList(getPullRequestFile(PullRequestFile.Status.MODIFIED)), mockedOldDme, mockedNewDme);
        assertNotEquals(Optional.empty(), result.get(0).getOldDmm());
        assertNotEquals(Optional.empty(), result.get(0).getNewDmm());
    }

    @Test
    public void testListModifiedDmmsWhenRemoved() {
        List<ModifiedDmm> result = dmmService.listModifiedDmms(Lists.newArrayList(getPullRequestFile(PullRequestFile.Status.REMOVED)), mockedOldDme, mockedNewDme);
        assertNotEquals(Optional.empty(), result.get(0).getOldDmm());
        assertEquals(Optional.empty(), result.get(0).getNewDmm());
    }

    @Test
    public void testListDmmDiffStatusesWhenOldPresent() {
        List<DmmDiffStatus> actual = dmmService.listDmmDiffStatuses(Lists.newArrayList(getModifiedDmm(mockedOldDmm, null)));
        assertEquals(FILENAME, actual.get(0).getFilename());
        assertEquals(oldWithNullChunkDiffList, actual.get(0).getDmmDiffChunks());
    }

    @Test
    public void testListDmmDiffStatusesWhenNewPresent() {
        List<DmmDiffStatus> actual = dmmService.listDmmDiffStatuses(Lists.newArrayList(getModifiedDmm(null, mockedNewDmm)));
        assertEquals(FILENAME, actual.get(0).getFilename());
        assertEquals(newWithNullChunkDiffList, actual.get(0).getDmmDiffChunks());
    }

    @Test
    public void testListDmmDiffStatusesWhenOldAndNewPresent() {
        List<DmmDiffStatus> actual = dmmService.listDmmDiffStatuses(Lists.newArrayList(getModifiedDmm(mockedOldDmm, mockedNewDmm)));
        assertEquals(FILENAME, actual.get(0).getFilename());
        assertEquals(oldWithNewChunkDiffList, actual.get(0).getDmmDiffChunks());
    }

    private PullRequestFile getPullRequestFile(PullRequestFile.Status status) {
        PullRequestFile pullRequestFile = new PullRequestFile();
        pullRequestFile.setFilename("/dmmFile.dmm");
        pullRequestFile.setStatus(status);
        return pullRequestFile;
    }

    private ModifiedDmm getModifiedDmm(Dmm oldDmm, Dmm newDmm) {
        return new ModifiedDmm(FILENAME, oldDmm, newDmm);
    }
}