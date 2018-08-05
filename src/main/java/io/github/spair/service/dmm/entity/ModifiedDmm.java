package io.github.spair.service.dmm.entity;

import io.github.spair.byond.dmm.parser.Dmm;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Optional;

@Data
@Setter(AccessLevel.NONE)
public class ModifiedDmm {

    private String sanitizedName;
    private String filename;
    @Nullable private Dmm oldDmm;
    @Nullable private Dmm newDmm;

    public ModifiedDmm(final String filename, @Nullable final Dmm oldDmm, @Nullable final Dmm newDmm) {
        this.sanitizedName = filename.substring(filename.lastIndexOf('/') + 1, filename.lastIndexOf('.'));
        this.filename = filename;
        this.oldDmm = oldDmm;
        this.newDmm = newDmm;
    }

    public Optional<Dmm> getOldDmm() {
        return Optional.ofNullable(oldDmm);
    }

    public Optional<Dmm> getNewDmm() {
        return Optional.ofNullable(newDmm);
    }
}
