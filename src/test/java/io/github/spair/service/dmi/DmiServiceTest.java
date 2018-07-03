package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.DmiComparator;
import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.ModifiedDmi;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.image.ImageUploaderService;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DmiComparator.class)
public class DmiServiceTest {

    @Mock
    private DmiLoader dmiLoader;
    @Mock
    private SpriteDiffStatusGenerator spriteDiffStatusGenerator;

    private DmiService dmiService;

    private static final String FILENAME = "test/icon.dmi";
    private static final String REALNAME = "icon.dmi";
    private static final String RAW_URL = "example.com";

    @Mock
    private Dmi oldMockedDmi;
    @Mock
    private Dmi newMockedDmi;

    @Before
    public void setUp() {
        dmiService = new DmiService(dmiLoader, spriteDiffStatusGenerator);

        when(dmiLoader.loadFromGitHub(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(Optional.of(oldMockedDmi)));
        when(dmiLoader.loadFromUrl(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(Optional.of(newMockedDmi)));

        PowerMockito.mockStatic(DmiComparator.class);
    }

    @Test
    public void testCreateModifiedDmiWhenModified() {
        assertEquals(new ModifiedDmi(FILENAME, oldMockedDmi, newMockedDmi), dmiService.createModifiedDmi(createPullRequestFile(PullRequestFile.Status.MODIFIED)));
        verify(dmiLoader).loadFromGitHub(REALNAME, FILENAME);
        verify(dmiLoader).loadFromUrl(REALNAME, RAW_URL);
    }

    @Test
    public void testCreateModifiedDmiWhenAdded() {
        assertEquals(new ModifiedDmi(FILENAME, null, newMockedDmi), dmiService.createModifiedDmi(createPullRequestFile(PullRequestFile.Status.ADDED)));
        verify(dmiLoader, never()).loadFromGitHub(REALNAME, FILENAME);
        verify(dmiLoader).loadFromUrl(REALNAME, RAW_URL);
    }

    @Test
    public void testCreateModifiedDmiWhenRemoved() {
        assertEquals(new ModifiedDmi(FILENAME, oldMockedDmi, null), dmiService.createModifiedDmi(createPullRequestFile(PullRequestFile.Status.REMOVED)));
        verify(dmiLoader).loadFromGitHub(REALNAME, FILENAME);
        verify(dmiLoader, never()).loadFromUrl(REALNAME, RAW_URL);
    }

    @Test
    public void testCreateDmiDiffStatusWhenNotSame() {
        Dmi oldDmiMocked = mock(Dmi.class, Answers.RETURNS_DEEP_STUBS);
        when(oldDmiMocked.getStates().size()).thenReturn(5);
        when(oldDmiMocked.isHasDuplicates()).thenReturn(false);
        Dmi newDmiMocked = mock(Dmi.class, Answers.RETURNS_DEEP_STUBS);
        when(newDmiMocked.getStates().size()).thenReturn(6);
        when(newDmiMocked.isHasDuplicates()).thenReturn(true);
        when(newDmiMocked.getDuplicateStatesNames()).thenReturn(Sets.newLinkedHashSet("icon1", "icon2"));

        ModifiedDmi modifiedDmi = mock(ModifiedDmi.class);
        when(modifiedDmi.getFilename()).thenReturn(FILENAME);
        when(modifiedDmi.getOldDmi()).thenReturn(Optional.of(oldDmiMocked));
        when(modifiedDmi.getNewDmi()).thenReturn(Optional.of(newDmiMocked));

        DmiDiff mockedDiff = mock(DmiDiff.class);
        when(mockedDiff.isSame()).thenReturn(false);
        when(DmiComparator.compare(any(), any())).thenReturn(mockedDiff);

        when(spriteDiffStatusGenerator.generate(any())).thenReturn(Collections.emptyList());

        Optional<DmiDiffStatus> diffStatusOpt = dmiService.createDmiDiffStatus(modifiedDmi);
        assertTrue(diffStatusOpt.isPresent());

        DmiDiffStatus diffStatus = diffStatusOpt.get();

        assertTrue(diffStatus.isHasDuplicates());
        assertEquals(diffStatus.getFilename(), FILENAME);
        assertTrue(diffStatus.getSpritesDiffStatuses().isEmpty());
        assertTrue(diffStatus.getOldDuplicatesNames().isEmpty());
        assertEquals(Sets.newLinkedHashSet("icon1", "icon2"), diffStatus.getNewDuplicatesNames());
        assertEquals(5, diffStatus.getOldStatesNumber());
        assertEquals(6, diffStatus.getNewStatesNumber());
    }

    @Test
    public void testCreateDmiDiffStatusWhenSame() {
        DmiDiff mockedDiff = mock(DmiDiff.class);
        when(mockedDiff.isSame()).thenReturn(true);
        when(DmiComparator.compare(any(), any())).thenReturn(mockedDiff);

        assertFalse(dmiService.createDmiDiffStatus(mock(ModifiedDmi.class)).isPresent());
    }

    private PullRequestFile createPullRequestFile(final PullRequestFile.Status status) {
        PullRequestFile dmiFile = new PullRequestFile();
        dmiFile.setFilename(FILENAME);
        dmiFile.setRawUrl(RAW_URL);
        dmiFile.setStatus(status);
        return dmiFile;
    }
}