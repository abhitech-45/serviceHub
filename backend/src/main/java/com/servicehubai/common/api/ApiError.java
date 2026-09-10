package com.servicehubai.common.api;

import java.util.List;

public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String correlationId,
        List<FieldError> fieldErrors) {

    public record FieldError(String field, String message) {
    }
}
