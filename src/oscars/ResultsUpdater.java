package oscars;

import java.io.IOException;

/** Write the results in a background thread so we can prompt for winners - Immutable */
final class ResultsUpdater extends Thread {
    /**
     * Repeatedly write the results with the current elapsed time until until the main thread kills
     * this thread or the show ends.
     */
    @Override
    public void run() {
        try {
            writeResults();
            for (Results.writeUpdated(); !Oscars.RESULTS.showEnded(); writeResults())
                Thread.sleep(Results.UPDATE_TIME);
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Write the current results */
    public static void writeResults() throws IOException {
        Results.write(Oscars.RESULTS.toDOM(), new Standings().toDOM());
    }
}