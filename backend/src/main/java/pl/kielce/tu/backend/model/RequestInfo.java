package pl.kielce.tu.backend.model;

public record RequestInfo(String method, String endpoint, String statusInfo, long duration) {
}
