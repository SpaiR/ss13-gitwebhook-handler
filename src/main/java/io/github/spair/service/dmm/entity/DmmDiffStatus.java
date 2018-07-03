package io.github.spair.service.dmm.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DmmDiffStatus {

    private String filename;
    private List<DmmChunkDiff> dmmDiffChunks;

    public DmmDiffStatus(String filename) {
        this.filename = filename;
        this.dmmDiffChunks = new ArrayList<>();
    }
}
