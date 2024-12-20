package com.sivalabs.ft.features.domain;

import java.time.Instant;

public record UpdateReleaseCommand(
        String code, String description, ReleaseStatus status, Instant releasedAt, String updatedBy) {}
