package io.github.spair.controller;

import io.github.spair.service.idmap.IDMapService;
import io.github.spair.service.idmap.entity.MapImagesInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletRequest;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/map")
public class IDMapController {

    private static final int YEAR = 365;
    private static final String CACHE_HEADER = CacheControl.maxAge(YEAR, TimeUnit.DAYS).cachePublic().getHeaderValue();

    private static final String PR_PARAM = "pr";
    private static final String MAP_PARAM = "map";
    private static final String PR_IMAGES_INFO_VAR = "prImagesInfo";

    private final IDMapService idMapService;

    @Autowired
    public IDMapController(final IDMapService idMapService) {
        this.idMapService = idMapService;
    }

    @GetMapping
    public String map(final ServletRequest req, final Model model) {
        final String prNumber = req.getParameter(PR_PARAM);
        final String mapName = req.getParameter(MAP_PARAM);

        if (!StringUtils.isEmpty(mapName) && NumberUtils.isDigits(prNumber)) {
            MapImagesInfo info = idMapService.getMapImageInfo(Integer.parseInt(prNumber), mapName).orElse(null);
            model.addAttribute(PR_IMAGES_INFO_VAR, info);
        }

        return "idm";
    }

    @GetMapping("/image/{prNum}/{mapName}/{type}.{hash}.png")
    public ResponseEntity<byte[]> getImgByTypeAndHash(
            @PathVariable final int prNum,
            @PathVariable final String mapName,
            @PathVariable final String type,
            @PathVariable final long hash) {
        return idMapService.getMapImage(prNum, mapName, type, hash)
                .map(imageData -> new ResponseEntity<>(imageData, getHttpHeadher(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private HttpHeaders getHttpHeadher() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CACHE_HEADER);
        return headers;
    }
}
