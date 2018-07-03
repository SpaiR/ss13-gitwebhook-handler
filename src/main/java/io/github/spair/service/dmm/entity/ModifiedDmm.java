package io.github.spair.service.dmm.entity;

import io.github.spair.byond.dmm.parser.Dmm;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Optional;

@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor
public class ModifiedDmm {

    private String filename;
    @Nullable private Dmm oldDmm;
    @Nullable private Dmm newDmm;

    public Optional<Dmm> getOldDmm() {
        return Optional.ofNullable(oldDmm);
    }

    public Optional<Dmm> getNewDmm() {
        return Optional.ofNullable(newDmm);
    }
}
