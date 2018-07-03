package io.github.spair.service.report;

import io.github.spair.service.image.ImageUploaderService;

public final class ReportHelper {

    private static final String IMG_DIFF_PATH = "http://tauceti.ru/img-diff/?";
    private static final String IMG_TEMPLATE = "<img src=\"%s\" title=\"%s\">";

    public static String createBeforeAfterDiffLink(
            final String linkBefore, final String linkAfter,
            final int resizeWidth, final int resizeHeight, final boolean forceResize) {
        boolean hasBeforeLink = !linkBefore.isEmpty();
        boolean hasAfterLink = !linkAfter.isEmpty();

        String wrappedLink = IMG_DIFF_PATH;

        if (hasBeforeLink) {
            wrappedLink += "before=" + wrapsLinkInResize(linkBefore, resizeWidth, resizeHeight, forceResize);
        }

        if (hasAfterLink) {
            if (hasBeforeLink) {
                wrappedLink += '&';
            }
            wrappedLink += "after=" + wrapsLinkInResize(linkAfter, resizeWidth, resizeHeight, forceResize);
        }

        return wrappedLink;
    }

    public static String createImgTag(final String imageLink, final String title) {
        if (!imageLink.isEmpty()) {
            return String.format(IMG_TEMPLATE, imageLink, title);
        } else {
            return "";
        }
    }

    private static String wrapsLinkInResize(
            final String link, final int resizeWidth, final int resizeHeight, final boolean forceResize) {
        if (link.isEmpty()) {
            return "";
        }
        String resizePrefix = "/" + resizeWidth + "x" + resizeHeight + "/";
        if (forceResize) {
            resizePrefix += "forceresize/";
        }
        String relativeLinkPath = link.substring(link.lastIndexOf('/') + 1, link.length());
        return ImageUploaderService.HOST_PATH + resizePrefix + relativeLinkPath;
    }

    private ReportHelper() {
    }
}
