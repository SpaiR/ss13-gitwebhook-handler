package io.github.spair.service.image;

public final class ImageHelper {

    private static final String IMG_DIFF_PATH = "http://tauceti.ru/img-diff/?";
    private static final String IMG_TEMPLATE = "<img src=\"%s\" title=\"%s\" />";

    public static String wrapDiffLinksWithResize(
            final String linkBefore, final String linkAfter, final int resizeWidth, final int resizeHeight) {
        boolean hasBeforeLink = !linkBefore.isEmpty();
        boolean hasAfterLink = !linkAfter.isEmpty();

        String wrappedLink = IMG_DIFF_PATH;

        if (hasBeforeLink) {
            wrappedLink += "before=" + resizeLink(linkBefore, resizeWidth, resizeHeight);
        }

        if (hasAfterLink) {
            if (hasBeforeLink) {
                wrappedLink += '&';
            }
            wrappedLink += "after=" + resizeLink(linkAfter, resizeWidth, resizeHeight);
        }

        return wrappedLink;
    }

    public static String wrapInImgTag(final String imageLink, final String title) {
        if (!imageLink.isEmpty()) {
            return String.format(IMG_TEMPLATE, imageLink, title);
        } else {
            return "";
        }
    }

    private static String resizeLink(final String link, final int resizeWidth, final int resizeHeight) {
        final String resizePrefix = "/" + resizeWidth + "x" + resizeHeight + "/forceresize/";
        final String relativeLinkPath = link.substring(link.lastIndexOf('/') + 1, link.length());
        return ImageUploaderService.HOST_PATH + resizePrefix + relativeLinkPath;
    }

    private ImageHelper() {
    }
}
