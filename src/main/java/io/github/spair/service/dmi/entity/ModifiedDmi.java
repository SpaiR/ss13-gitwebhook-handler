package io.github.spair.service.dmi.entity;

import io.github.spair.byond.dmi.Dmi;
import lombok.Data;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

import javax.annotation.Nullable;
import java.util.Optional;

@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor
public class ModifiedDmi {

    private String filename;
    @Nullable private Dmi oldDmi;
    @Nullable private Dmi newDmi;

    public Optional<Dmi> getOldDmi() {
        return Optional.ofNullable(oldDmi);
    }

    public Optional<Dmi> getNewDmi() {
        return Optional.ofNullable(newDmi);
    }
}
