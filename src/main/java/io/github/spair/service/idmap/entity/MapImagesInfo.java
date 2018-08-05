package io.github.spair.service.idmap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapImagesInfo {
    private MapImageSize size;
    private List<MapHash> hashes;
}
