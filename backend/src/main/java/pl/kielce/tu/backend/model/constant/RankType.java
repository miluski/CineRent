package pl.kielce.tu.backend.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum RankType {
    USER,
    ADMIN;
}
