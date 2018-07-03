package io.github.spair.service.dmm.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DmmChunkDiff {

    private List<String> oldChunkImagesLinks;
    private List<String> newChunkImagesLinks;
    private List<String> diffImagesLinks;

    private List<String> oldChunkAreasImagesLinks;
    private List<String> newChunkAreasImagesLinks;
    private List<String> diffAreasImagesLinks;
}
