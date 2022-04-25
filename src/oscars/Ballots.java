package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.jdom2.Element;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;

public final class Ballots {
    public final Collection<String[]> all;

    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length != 1)
            throw new IllegalArgumentException("Usage: Batch <URL>");
        for (int entryCount = -1;; Thread.sleep(10000))
            try {
                URL url = new URL(inArgs[0]);
                url.openConnection().setDefaultUseCaches(false);
                Ballots ballots = new Ballots(url);
                if (ballots.all.size() > entryCount) {
                    entryCount = ballots.all.size();
                    Results.write(LocalDateTime.now(), toDOM(ballots.latest()));
                    System.err.println(LocalDateTime.now() + " - Wrote " + entryCount + " entries");
                }
            } catch (IOException e) {
                System.err.println(LocalDateTime.now() + " - Error downloading entries: " + e);
            }
    }

    public Ballots(URL inURL) throws IOException, CsvException {
        try (InputStreamReader inputReader = new InputStreamReader(inURL.openStream());
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(inputReader)) {
            all = csvReader.readAll();
        }
    }

    public Collection<String[]> latest() {
        return all
                .stream().collect(Collectors.toMap(row -> (row[1] + "|" + row[2]).toUpperCase(),
                        row -> row, BinaryOperator.maxBy(Comparator.comparing(Ballots::timestamp))))
                .values();
    }

    private static LocalDateTime timestamp(String[] inRow) {
        return LocalDateTime.parse(inRow[0], DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"));
    }

    private static Element toDOM(Collection<String[]> inRows) {
        return inRows.stream()
                .map(row -> new Element("entry").addContent(toDOM(timestamp(row)))
                        .addContent(new Element("firstName").addContent(row[1]))
                        .addContent(new Element("lastName").addContent(row[2])))
                .reduce(new Element("entries"), Element::addContent);
    }

    private static Element toDOM(LocalDateTime inTimestamp) {
        return new Element("timestamp").setAttribute("raw", inTimestamp.toString()).addContent(
                inTimestamp.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a")));
    }
}