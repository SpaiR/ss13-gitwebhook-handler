package io.github.spair.service.idmap;

import lombok.Builder;
import lombok.Data;

import java.awt.image.BufferedImage;

@Data
@Builder
final class MapImages {
    private BufferedImage fullLayer;
    private BufferedImage areaLayer;
}
