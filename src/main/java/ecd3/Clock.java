package ecd3;

import java.time.Instant;

public class Clock {

    private static Clock INSTANCE = null;

    Long epochSeconds;
    int nanoSeconds;

    private Clock() {
        this.epochSeconds = Instant.now().toEpochMilli();
        this.nanoSeconds = Instant.now().getNano();
    }

    public static Clock getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Clock();
        }
        return INSTANCE;
    }
}
