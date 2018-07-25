package io.github.spair.service.dme.entity;

import io.github.spair.byond.dme.Dme;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DmePair {
    private Dme oldDme;
    private Dme newDme;
}
