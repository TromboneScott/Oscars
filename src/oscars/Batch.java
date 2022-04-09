package oscars;

import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.jdom2.Element;

import com.opencsv.CSVReaderHeaderAware;

public final class Batch {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter
            .ofPattern("M/d/yyyy H:mm:ss");

    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter
            .ofPattern("MM/dd/yyyy HH:mm:ss");

    private final String title;

    private final URL url;

    public static void main(String[] inArgs) throws Exception {
        new Batch(inArgs).run();
    }

    private Batch(String[] inArgs) throws Exception {
        if (inArgs.length != 2)
            throw new IllegalArgumentException("Usage: Batch <year> <URL>");
        title = inArgs[0] + " OSCARS";
        url = new URL(inArgs[1]);
        url.openConnection().setDefaultUseCaches(false);
    }

    private void run() throws Exception {
        int entryCount = 0;
        while (true) {
            List<String[]> entries = entries();
            if (entries.size() > entryCount)
                writeResults(entries);
            entryCount = entries.size();
            Thread.sleep(10000);
        }
    }

    private List<String[]> entries() {
        try (InputStreamReader inputReader = new InputStreamReader(url.openStream());
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(inputReader)) {
            return csvReader.readAll();
        } catch (Exception e) {
            System.err.println("Error downloading entries: " + e);
            return Collections.emptyList();
        }
    }

    private void writeResults(List<String[]> inEntries) throws Exception {
        String updated = LocalDateTime.now().format(OUTPUT_FORMAT);
        Oscars.writeDocument(
                new Element("results").addContent(new Element("title").addContent(title))
                        .addContent(entriesDOM(inEntries))
                        .addContent(new Element("updated").addContent(updated)),
                Results.RESULTS_FILE, null);
        System.err.println(updated + " - Wrote " + inEntries.size() + " entries");
    }

    private Element entriesDOM(List<String[]> inEntries) throws Exception {
        return inEntries.stream()
                .map(row -> new Element("entry")
                        .addContent(new Element("time").addContent(
                                LocalDateTime.parse(row[0], INPUT_FORMAT).format(OUTPUT_FORMAT)))
                        .addContent(new Element("firstName").addContent(row[1]))
                        .addContent(new Element("lastName").addContent(row[2])))
                .reduce(new Element("entries"), Element::addContent);
    }
}