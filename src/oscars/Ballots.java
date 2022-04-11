package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Element;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;

public final class Ballots {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter
            .ofPattern("M/d/yyyy H:mm:ss");

    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter
            .ofPattern("MM/dd/yyyy HH:mm:ss");

    private final String title;

    private final URL url;

    public static void main(String[] inArgs) throws Exception {
        new Ballots(inArgs).run();
    }

    private Ballots(String[] inArgs) throws Exception {
        if (inArgs.length != 2)
            throw new IllegalArgumentException("Usage: Batch <year> <URL>");
        title = inArgs[0] + " OSCARS";
        url = new URL(inArgs[1]);
        url.openConnection().setDefaultUseCaches(false);
    }

    private void run() throws Exception {
        int entryCount = -1;
        while (true) {
            try {
                Collection<String[]> entries = uniqueBallots(url);
                if (entries.size() > entryCount) {
                    writeResults(entries);
                    entryCount = entries.size();
                }
            } catch (IOException e) {
                System.err.println("Error downloading entries: " + e);
            }
            Thread.sleep(10000);
        }
    }

    public static List<String[]> ballots(URL inURL) throws IOException, CsvException {
        try (InputStreamReader inputReader = new InputStreamReader(inURL.openStream());
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(inputReader)) {
            return csvReader.readAll();
        }
    }

    public static Collection<String[]> uniqueBallots(URL inURL) throws Exception {
        return ballots(inURL).stream()
                .collect(Collectors.toMap(row -> (row[1] + "|" + row[2]).toUpperCase(), row -> row,
                        (row1, row2) -> row2))
                .values();
    }

    private void writeResults(Collection<String[]> inEntries) throws Exception {
        String updated = LocalDateTime.now().format(OUTPUT_FORMAT);
        Oscars.writeDocument(
                new Element("results").addContent(new Element("title").addContent(title))
                        .addContent(entriesDOM(inEntries))
                        .addContent(new Element("updated").addContent(updated)),
                Results.RESULTS_FILE, null);
        System.err.println(updated + " - Wrote " + inEntries.size() + " entries");
    }

    private Element entriesDOM(Collection<String[]> inEntries) throws Exception {
        return inEntries.stream()
                .map(row -> new Element("entry")
                        .addContent(new Element("time").addContent(
                                LocalDateTime.parse(row[0], INPUT_FORMAT).format(OUTPUT_FORMAT)))
                        .addContent(new Element("firstName").addContent(row[1]))
                        .addContent(new Element("lastName").addContent(row[2])))
                .reduce(new Element("entries"), Element::addContent);
    }
}