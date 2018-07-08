package io.github.spair.service.dme;

import io.github.spair.byond.dme.Dme;
import io.github.spair.byond.dme.DmeParser;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DmeService {

    public Dme parseDme(final File dme) {
        return DmeParser.parse(dme);
    }
}
