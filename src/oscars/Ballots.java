package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

import org.jdom2.Element;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;

public final class Ballots {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter
            .ofPattern("M/d/yyyy H:mm:ss");

    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm:ss a");

    public final Collection<String[]> all;

    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length != 1)
            throw new IllegalArgumentException("Usage: Batch <URL>");
        URL url = new URL(inArgs[0]);
        url.openConnection().setDefaultUseCaches(false);
        int entryCount = -1;
        while (true) {
            try {
                Ballots ballots = new Ballots(url);
                if (ballots.all.size() > entryCount) {
                    entryCount = ballots.all.size();
                    Results.write(LocalDateTime.now(),
                            element -> element.addContent(entriesDOM(ballots.latest())));
                    System.err.println(LocalDateTime.now() + " - Wrote " + entryCount + " entries");
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading entries: " + e);
            }
            Thread.sleep(10000);
        }
    }

    public Ballots(URL inURL) throws IOException, CsvException {
        try (InputStreamReader inputReader = new InputStreamReader(inURL.openStream());
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(inputReader)) {
            all = csvReader.readAll();
        }
    }

    public Collection<String[]> latest() {
        return all.stream().collect(Collectors.toMap(row -> (row[1] + "|" + row[2]).toUpperCase(),
                row -> row,
                (row1, row2) -> timestamp(row1).compareTo(timestamp(row2)) > 0 ? row1 : row2))
                .values();
    }

    private static LocalDateTime timestamp(String[] inRow) {
        return LocalDateTime.parse(inRow[0], INPUT_FORMAT);
    }

    private static Element entriesDOM(Collection<String[]> inEntries) {
        return inEntries.stream().map(row -> new Element("entry")
                .addContent(new Element("timestamp").setAttribute("raw", timestamp(row).toString())
                        .addContent(timestamp(row).format(OUTPUT_FORMAT)))
                .addContent(new Element("firstName").addContent(row[1]))
                .addContent(new Element("lastName").addContent(row[2])))
                .reduce(new Element("entries"), Element::addContent);
    }
}