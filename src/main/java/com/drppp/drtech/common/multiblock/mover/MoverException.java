package com.drppp.drtech.common.multiblock.mover;

public final class MoverException extends Exception {
    private final String translationKey;

    public MoverException(String translationKey) {
        super(translationKey);
        this.translationKey = translationKey;
    }

    public MoverException(String translationKey, Throwable cause) {
        super(translationKey, cause);
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
