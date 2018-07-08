package io.github.spair.service.dme;

import io.github.spair.byond.dme.Dme;
import io.github.spair.byond.dme.DmeParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DmeParser.class)
public class DmeServiceTest {

    @Mock
    private Dme mockedDme;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(DmeParser.class);
        PowerMockito.when(DmeParser.parse(any(File.class))).thenReturn(mockedDme);
    }

    @Test
    public void testParseDme() {
        DmeService service = new DmeService();
        Dme result = service.parseDme(new File("fakeFile"));
        assertEquals(mockedDme, result);
    }
}