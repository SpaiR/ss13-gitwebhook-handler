package io.github.spair.handler.command.diff;

import io.github.spair.byond.dme.Dme;
import io.github.spair.byond.dmm.MapRegion;
import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.handler.command.PullRequestHelper;
import io.github.spair.service.ByondFiles;
import io.github.spair.service.HandlerUrlService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.dme.DmeService;
import io.github.spair.service.dme.entity.DmePair;
import io.github.spair.service.dmm.DmmService;
import io.github.spair.service.github.GitHubCommentService;
import io.github.spair.service.idmap.IDMapService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BuildIDMapCommand implements HandlerCommand<PullRequest> {

    static final String TITLE = "## Interactive Diff Map";
    private static final String ARROW = "&#x2BC8;"; // ⯈

    private final GitHubCommentService gitHubCommentService;
    private final ConfigService configService;
    private final GitHubService gitHubService;
    private final DmeService dmeService;
    private final DmmService dmmService;
    private final IDMapService interactiveDiffMapService;
    private final HandlerUrlService handlerUrlService;

    @Autowired
    public BuildIDMapCommand(
            final GitHubCommentService gitHubCommentService,
            final ConfigService configService,
            final GitHubService gitHubService,
            final DmeService dmeService,
            final DmmService dmmService,
            final IDMapService interactiveDiffMapService,
            final HandlerUrlService handlerUrlService) {
        this.configService = configService;
        this.gitHubService = gitHubService;
        this.dmeService = dmeService;
        this.dmmService = dmmService;
        this.interactiveDiffMapService = interactiveDiffMapService;
        this.handlerUrlService = handlerUrlService;
        this.gitHubCommentService = gitHubCommentService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        if (!PullRequestHelper.checkForIDMap(pullRequest, configService.getConfig())) {
            return;
        }

        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> allPullRequestFiles = gitHubService.listPullRequestFiles(prNumber);
        final List<PullRequestFile> dmmPrFiles = PullRequestHelper.filterDmmFiles(allPullRequestFiles);

        if (dmmPrFiles.isEmpty()) {
            return;
        }

        Optional<DmePair> dmePair = dmeService.createDmePairForPullRequest(pullRequest, null, null);

        if (!dmePair.isPresent()) {
            return;
        }

        Dme oldDme = dmePair.get().getOldDme();
        Dme newDme = dmePair.get().getNewDme();

        List<String> mapLinks = new ArrayList<>();

        dmmService.listModifiedDmms(dmmPrFiles, oldDme, newDme).forEach(modifiedDmm -> {
            MapRegion mapRegion = dmmService.createMapRegion(modifiedDmm);
            interactiveDiffMapService.buildMap(modifiedDmm, mapRegion, prNumber);
            mapLinks.add(createMapLink(prNumber, modifiedDmm.getSanitizedName()));
        });

        if (!mapLinks.isEmpty()) {
            gitHubCommentService.sendCommentOrUpdate(prNumber, buildComment(mapLinks), TITLE);
        }
    }

    private String createMapLink(final int prNumber, final String mapName) {
        String mapNameWithSuffix = mapName + ByondFiles.DMM_SUFFIX;
        String handlerUrl = handlerUrlService.getServerUrl();
        return String.format(ARROW + " [%s](%s/map?pr=%d&map=%s)", mapNameWithSuffix, handlerUrl, prNumber, mapName);
    }

    private String buildComment(final List<String> mapLinks) {
        StringBuilder comment = new StringBuilder(TITLE).append(System.lineSeparator()).append(System.lineSeparator());
        mapLinks.forEach(link -> comment.append(link).append(System.lineSeparator()));
        return comment.toString();
    }
}
