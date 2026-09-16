package com.servicehubai.chat.application;

public class AiProviderException extends RuntimeException {
    private final String reason;

    public AiProviderException(String reason, Throwable cause) {
        super(reason, cause);
        this.reason = reason;
    }

    public AiProviderException(String reason) {
        super(reason);
        this.reason = reason;
    }

    public String reason() {
        return reason;
    }
}
