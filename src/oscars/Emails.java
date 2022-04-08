package oscars;

import java.io.InputStreamReader;
import java.net.URL;

import com.opencsv.CSVReaderHeaderAware;

public class Emails {
    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length != 1)
            throw new IllegalArgumentException("Usage: Emails <URL>");
        try (InputStreamReader inputReader = new InputStreamReader(new URL(inArgs[0]).openStream());
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(inputReader)) {
            csvReader.readAll().stream().filter(row -> !row[row.length - 1].isEmpty()).forEach(
                    row -> System.out.println(row[1] + " " + row[2] + " = " + row[row.length - 1]));
        }
    }
}