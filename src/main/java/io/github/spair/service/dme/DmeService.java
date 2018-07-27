package io.github.spair.service.dme;

import io.github.spair.service.dme.entity.DmePair;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class DmeService {

    private final DmePairGenerator dmePairGenerator;

    @Autowired
    public DmeService(final DmePairGenerator dmePairGenerator) {
        this.dmePairGenerator = dmePairGenerator;
    }

    public Optional<DmePair> createDmePairForPullRequest(final PullRequest pullRequest,
                                                         @Nullable final Consumer<Integer> updateCallback,
                                                         @Nullable final Runnable endCallback) {
        return dmePairGenerator.generate(pullRequest, updateCallback, endCallback);
    }
}
