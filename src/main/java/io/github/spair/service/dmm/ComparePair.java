package io.github.spair.service.dmm;

import io.github.spair.byond.dmm.Dmm;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
final class ComparePair {
    private Dmm toCompare;
    private Dmm compareWith;
}
