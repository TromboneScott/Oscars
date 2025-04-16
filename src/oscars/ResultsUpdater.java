package oscars;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/** Write the results in a background thread so we can prompt for winners - Singleton */
enum ResultsUpdater implements Runnable {
    INSTANCE;

    private static final long MAX_WAIT = TimeUnit.MINUTES.toMillis(1);

    private long elapsedTime = 0; // Time since the start of the show

    private long validTimes = 0; // The number of players whose time guess has been reached

    private ZonedDateTime updated = ZonedDateTime.now(); // The last time the results changed

    /**
     * Write the results and wait until we need to update the times again. Continue until the main
     * thread kills this thread or the show ends.
     */
    @Override
    public void run() {
        updated = ZonedDateTime.now();
        try {
            do {
                writeResults();
                Thread.sleep(waitTime());
            } while (Oscars.RESULTS.millisSinceStart() > 0 && !Oscars.RESULTS.showEnded());
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long waitTime() {
        long nextPlayerTime = Oscars.PLAYERS.stream().mapToLong(player -> player.time())
                .filter(playerTime -> playerTime > elapsedTime).map(TimeUnit.SECONDS::toMillis)
                .min().orElse(Long.MAX_VALUE);
        long millisSinceStart = Oscars.RESULTS.millisSinceStart();
        return Math.min(Math.max(nextPlayerTime - millisSinceStart, 0),
                Math.max(MAX_WAIT - millisSinceStart, MAX_WAIT - millisSinceStart % MAX_WAIT));
    }

    /** Write the current results */
    public void writeResults() throws IOException {
        elapsedTime = Oscars.RESULTS.elapsedTimeSeconds();
        long currentTimes = Oscars.PLAYERS.stream().filter(player -> player.time() <= elapsedTime)
                .count();
        if (validTimes != currentTimes)
            updated = ZonedDateTime.now();
        validTimes = currentTimes;
        Results.write(updated, Oscars.RESULTS.toDOM(), new Standings().toDOM());
    }
}