const URL_PARAMS = getUrlParams();
const OLD_MAP = 'Old Map';
const NEW_MAP = 'New Map';
const DIFF_LAYER = 'Diff Layer';
const AREA_LAYER_OLD = 'Area Layer Old';
const AREA_LAYER_NEW = 'Area Layer New';

let prImagesInfo = window.prImagesInfo;
let imagesPaths = {};
let prNum = URL_PARAMS['pr'];
let mapName = URL_PARAMS['map'];

if (prNum && mapName && prImagesInfo) {
    window.onload = () => {
        resolveImagesPaths();
        initMap();
        hideSpinner();
        preloadImages();
    };
} else {
    showInvalidUrlMsg()
}

function initMap() {
    let map = L.map('map', {zoomAnimation: true, minZoom: -2, maxZoom: 2, crs: L.CRS.Simple}),
        bounds = [[0, 0], [prImagesInfo.size.width, prImagesInfo.size.height]];

    map.fitBounds(bounds);
    map.setMaxBounds(bounds);

    let baseMaps = getBaseMaps(bounds),
        overlayMaps = getOverlayMaps(bounds);

    map.addControl(L.control.layers(baseMaps, overlayMaps));
    map.addLayer(baseMaps[OLD_MAP]);

    addSpaceDisableBtn();
}

function preloadImages() {
    for (let imagePath in imagesPaths) {
        if (imagePath !== OLD_MAP) {
            new Image().src = imagesPaths[imagePath];
        }
    }
}

function getUrlParams() {
    let vars = {};
    window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, (m, key, value) => {
        vars[key] = value;
    });
    return vars;
}

function resolveImagesPaths() {
    const typesMap = {
        'old-full': OLD_MAP,
        'old-area': AREA_LAYER_OLD,
        'new-full': NEW_MAP,
        'new-area': AREA_LAYER_NEW,
        'diff': DIFF_LAYER
    };

    prImagesInfo.hashes.forEach(mapHash => {
        let type = mapHash.type, hash = mapHash.hash;
        imagesPaths[typesMap[type]] = `map/image/${prNum}/${mapName}/${type}.${hash}.png`
    });
}

function getBaseMaps(bounds) {
    const ATTR_TEXT = '&copy; Created with <a href="https://github.com/SpaiR/byond-dmm-util">BYOND Dmm Util</a>';
    let baseMaps = {};
    baseMaps[OLD_MAP] = L.imageOverlay(imagesPaths[OLD_MAP], bounds, {attribution: ATTR_TEXT});
    baseMaps[NEW_MAP] = L.imageOverlay(imagesPaths[NEW_MAP], bounds, {attribution: ATTR_TEXT});
    return baseMaps;
}

function getOverlayMaps(bounds) {
    let overlayMaps = {};
    overlayMaps[AREA_LAYER_OLD] = L.imageOverlay(imagesPaths[AREA_LAYER_OLD], bounds);
    overlayMaps[AREA_LAYER_NEW] = L.imageOverlay(imagesPaths[AREA_LAYER_NEW], bounds);
    overlayMaps[DIFF_LAYER] = L.imageOverlay(imagesPaths[DIFF_LAYER], bounds, {opacity: 0.75});
    return overlayMaps;
}

function showInvalidUrlMsg() {
    hideSpinner();
    document.getElementById('map').style.display = 'none';
    document.getElementById('invalid-url-msg').style.display = 'block';
}

function hideSpinner() {
    document.getElementById('spinner').style.display = 'none';
}

function addSpaceDisableBtn() {
    let overlaysPanel = document.querySelector('.leaflet-control-layers-overlays');
    let labelClone = overlaysPanel.firstElementChild.cloneNode(true);

    let mapEl = document.getElementById('map');

    let checkbox = labelClone.querySelector('.leaflet-control-layers-selector');
    checkbox.checked = true;
    checkbox.onchange = () => {
        if (checkbox.checked) {
            mapEl.classList.add('space-back');
        } else {
            mapEl.classList.remove('space-back');
        }
    };

    labelClone.querySelector('span').innerHTML = ' Toggle Space';

    overlaysPanel.appendChild(labelClone);
}