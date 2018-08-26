package io.github.spair.service.idmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spair.byond.ByondTypes;
import io.github.spair.byond.dmm.MapRegion;
import io.github.spair.byond.dmm.Dmm;
import io.github.spair.byond.dmm.render.DmmRender;
import io.github.spair.service.dmm.entity.ModifiedDmm;
import io.github.spair.service.idmap.entity.MapHash;
import io.github.spair.service.idmap.entity.MapImageSize;
import io.github.spair.service.idmap.entity.MapImagesInfo;
import io.github.spair.service.image.ImageCompressorService;
import io.github.spair.util.FutureUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class IDMapService {

    private static final String INFO = ".info.json";

    private static final String MAPS_FOLDER = ".maps";
    private static final String PNG_SUFFIX = ".png";

    private static final String OLD_TYPE = "old";
    private static final String NEW_TYPE = "new";
    private static final String DIFF_TYPE = "diff";

    private static final String AREA_SUFFIX = "-area";
    private static final String FULL_SUFFIX = "-full";

    private static final String SPACE_TYPE = ByondTypes.TURF + "/space";

    private final ImageCompressorService compressorService;
    private final ObjectMapper objectMapper;

    @Autowired
    public IDMapService(final ImageCompressorService compressorService, final ObjectMapper objectMapper) {
        this.compressorService = compressorService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void initMapsFolder() throws IOException {
        File mapsFolder = new File(MAPS_FOLDER);
        if (!mapsFolder.exists() && !mapsFolder.mkdir()) {
            throw new IOException("Error on '.maps' directory creation");
        }
    }

    public void deletePrMapFolder(final int prNumber) {
        File prFolder = new File(MAPS_FOLDER + File.separator + prNumber);
        if (prFolder.exists() && !FileSystemUtils.deleteRecursively(prFolder)) {
            throw new RuntimeException("Error while deleting maps folder for pr " + prNumber);
        }
    }

    public void buildMap(final ModifiedDmm modifiedDmm, final MapRegion mapRegion, final int prNumber) {
        Optional<Dmm> oldDmm = modifiedDmm.getOldDmm();
        Optional<Dmm> newDmm = modifiedDmm.getNewDmm();

        MapImages oldDmmImages = null;
        MapImages newDmmImages = null;
        BufferedImage diffPointsImage = null;

        if (oldDmm.isPresent()) {
            oldDmmImages = createMapImages(oldDmm.get());
            diffPointsImage = DmmRender.renderDiffPoints(oldDmm.get(), mapRegion);
        }
        if (newDmm.isPresent()) {
            newDmmImages = createMapImages(newDmm.get());

            if (diffPointsImage == null) {
                diffPointsImage = DmmRender.renderDiffPoints(newDmm.get(), mapRegion);
            }
        }

        final File mapFolder = getNewMapFolder(prNumber, modifiedDmm.getSanitizedName());

        saveMapImagesIfExist(oldDmmImages, mapFolder, OLD_TYPE);
        saveMapImagesIfExist(newDmmImages, mapFolder, NEW_TYPE);
        saveImage(mapFolder, diffPointsImage, DIFF_TYPE);

        compressImages(mapFolder);
        addImagesHash(mapFolder);
        saveImagesInfo(mapFolder);
    }

    public Optional<MapImagesInfo> getMapImageInfo(final int prNumber, final String mapName) {
        File mapFolder = getMapFolder(prNumber, mapName);

        if (!mapFolder.exists()) {
            return Optional.empty();
        }

        try {
            File info = new File(mapFolder.getPath() + File.separator + INFO);
            return Optional.of(objectMapper.readValue(info, MapImagesInfo.class));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<byte[]> getMapImage(final int prNumber, final String mapName, final String type, final long hash) {
        String imageName = type + '.' + hash + PNG_SUFFIX;
        File imageFile = new File(getMapFolder(prNumber, mapName).getPath() + File.separator + imageName);

        if (!imageFile.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(FileUtils.readFileToByteArray(imageFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private MapImages createMapImages(final Dmm dmm) {
        CompletableFuture<BufferedImage> area = renderAsync(dmm, ByondTypes.TURF, ByondTypes.OBJ, ByondTypes.MOB);
        CompletableFuture<BufferedImage> full = renderAsync(dmm, ByondTypes.AREA, SPACE_TYPE);

        FutureUtil.completeFutures(area, full);

        return MapImages.builder()
                .areaLayer(FutureUtil.extractFuture(area)).fullLayer(FutureUtil.extractFuture(full))
                .build();
    }

    private CompletableFuture<BufferedImage> renderAsync(final Dmm dmm, final String... typesToIgnore) {
        return CompletableFuture.supplyAsync(() -> DmmRender.renderToImage(dmm, typesToIgnore));
    }

    private File getNewMapFolder(final int prNumber, final String mapName) {
        File mapFolder = new File(MAPS_FOLDER + File.separator + prNumber + File.separator + mapName);

        if (mapFolder.exists() && !FileSystemUtils.deleteRecursively(mapFolder)) {
            throw new RuntimeException("Error while deleting dir for pr " + prNumber + " and map " + mapName);
        }

        try {
            FileUtils.forceMkdir(mapFolder);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating dir for pr " + prNumber + " and map " + mapName);
        }

        return mapFolder;
    }

    private File getMapFolder(final int prNumber, final String mapName) {
        return new File(MAPS_FOLDER + File.separator + prNumber + File.separator + mapName);
    }

    private void saveMapImagesIfExist(
            @Nullable final MapImages mapImages, final File mapFolder, final String typePrefix) {
        if (mapImages != null) {
            FutureUtil.completeFutures(
                    CompletableFuture.runAsync(
                            () -> saveImage(mapFolder, mapImages.getAreaLayer(), typePrefix + AREA_SUFFIX)
                    ),
                    CompletableFuture.runAsync(
                            () -> saveImage(mapFolder, mapImages.getFullLayer(), typePrefix + FULL_SUFFIX)
                    )
            );
        }
    }

    private void saveImage(final File mapFolder, final BufferedImage image, final String imageName) {
        File imageFile = new File(mapFolder.getPath() + File.separator + imageName + PNG_SUFFIX);

        try {
            if (!imageFile.createNewFile()) {
                throw new IOException("Error while creating map image " + imageName);
            }
            ImageIO.write(image, "PNG", imageFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void compressImages(final File mapFolder) {
        FileUtils.listFiles(mapFolder, null, false).forEach(imageFile ->
                CompletableFuture.runAsync(() -> compressorService.compressImage(imageFile))
        );
        FutureUtil.awaitTermination(5, TimeUnit.MINUTES);
    }

    private void addImagesHash(final File mapFolder) {
        FileUtils.listFiles(mapFolder, null, false).forEach(imageFile -> {
            try {
                long hash = imageFile.lastModified();
                String name = imageFile.getName().substring(0, imageFile.getName().lastIndexOf('.'));
                String newName = name + '.' + hash + PNG_SUFFIX;
                FileUtils.moveFile(imageFile, new File(mapFolder.getPath() + File.separator + newName));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void saveImagesInfo(final File mapFolder) {
        try {
            File info = new File(mapFolder.getPath() + File.separator + INFO);

            MapImageSize mapImageSize = null;
            List<MapHash> mapHashes = new ArrayList<>();

            for (File imageFile : FileUtils.listFiles(mapFolder, null, false)) {
                if (mapImageSize == null) {
                    BufferedImage image = ImageIO.read(imageFile);
                    mapImageSize = new MapImageSize(image.getWidth(), image.getHeight());
                }

                String imageName = imageFile.getName();
                String name = imageName.substring(0, imageName.indexOf('.'));
                String hash = imageName.substring(imageName.indexOf('.') + 1, imageName.lastIndexOf('.'));
                mapHashes.add(new MapHash(name, Long.parseLong(hash)));
            }

            if (!info.createNewFile()) {
                throw new IOException("Error while creating info file in " + mapFolder);
            }

            objectMapper.writer().writeValue(info, new MapImagesInfo(mapImageSize, mapHashes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
