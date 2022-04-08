package oscars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.jdom2.Element;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;

public class Batch {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter
            .ofPattern("M/d/yyyy H:mm:ss");

    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter
            .ofPattern("MM/dd/yyyy HH:mm:ss");

    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length != 2)
            throw new IllegalArgumentException("Usage: Batch <year> <URL>");
        Oscars.writeDocument(
                new Element("results")
                        .addContent(new Element("title").addContent(inArgs[0] + " OSCARS"))
                        .addContent(entryDOM(inArgs[1]))
                        .addContent(new Element("updated").addContent(
                                LocalDateTime.now().atZone(ZoneId.systemDefault()).format(
                                        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a - z")))),
                Results.RESULTS_FILE, null);
    }

    private static Element entryDOM(String inURL) throws IOException, CsvException {
        try (InputStreamReader inputReader = new InputStreamReader(new URL(inURL).openStream());
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