package com.azuredoom.levelingcore;

public class LevelingCoreException extends RuntimeException {

    public LevelingCoreException(String failedToCloseH2Connection, Exception e) {
        super(failedToCloseH2Connection, e);
    }

    public LevelingCoreException(String failedToCloseH2Connection) {
        super(failedToCloseH2Connection);
    }
}
