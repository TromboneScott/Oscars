package oscars;

import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;

import com.opencsv.CSVReaderHeaderAware;

public final class Batch {
    private static final long FREQUENCY = TimeUnit.SECONDS.toMillis(10);

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter
            .ofPattern("M/d/yyyy H:mm:ss");

    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter
            .ofPattern("MM/dd/yyyy HH:mm:ss");

    private final String title;

    private final URL url;

    public static void main(String[] inArgs) throws Exception {
        Batch batch = new Batch(inArgs);
        while (true) {
            batch.writeResults();
            Thread.sleep(FREQUENCY - System.currentTimeMillis() % FREQUENCY);
        }
    }

    private Batch(String[] inArgs) throws Exception {
        if (inArgs.length != 2)
            throw new IllegalArgumentException("Usage: Batch <year> <URL>");
        title = inArgs[0] + " OSCARS";
        url = new URL(inArgs[1]);
    }

    private void writeResults() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Oscars.writeDocument(
                new Element("results").addContent(new Element("title").addContent(title))
                        .addContent(entriesDOM())
                        .addContent(new Element("updated").addContent(now.format(OUTPUT_FORMAT))),
                Results.RESULTS_FILE, null);
    }

    private Element entriesDOM() throws Exception {
        try (InputStreamReader inputReader = new InputStreamReader(url.openStream());
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(inputReader)) {
            return csvReader.readAll().stream().map(row -> new Element("entry")
                    .addContent(new Element("time").addContent(
                            LocalDateTime.parse(row[0], INPUT_FORMAT).format(OUTPUT_FORMAT)))
                    .addContent(new Element("firstName").addContent(row[1]))
                    .addContent(new Element("lastName").addContent(row[2])))
                    .reduce(new Element("entries"), Element::addContent);
        }
    }
}