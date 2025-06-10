package oscars;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/** Write the results in a background thread so we can prompt for winners - Immutable */
final class ResultsUpdater extends Thread {
    private static final long UPDATE_TIME = TimeUnit.SECONDS.toMillis(10);

    /**
     * Repeatedly write the results with the current elapsed time until until the main thread kills
     * this thread or the show ends.
     */
    @Override
    public void run() {
        ZonedDateTime updated = ZonedDateTime.now();
        try {
            writeResults(updated);
            for (Results.writeUpdated(); Oscars.RESULTS.millisSinceStart() > 0
                    && !Oscars.RESULTS.showEnded(); writeResults(updated))
                Thread.sleep(UPDATE_TIME);
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Write the current results */
    public static void writeResults(ZonedDateTime inUpdated) throws IOException {
        Results.write(inUpdated, Oscars.RESULTS.toDOM(), new Standings().toDOM());
    }
}