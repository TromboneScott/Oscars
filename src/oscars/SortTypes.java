package oscars;

import java.io.IOException;

import org.jdom2.Element;

public final class SortTypes {
    private static final String[] COLUMNS = { "name", "rank", "bpr", "wpr", "score", "time" };

    public static void writePages() throws IOException {
        for (String column : COLUMNS)
            for (String type : new String[] { column, column + "Reverse" })
                Directory.SORT.write(new Element("sort").addContent(type), type + ".xml",
                        "sort.xsl");
        Directory.SORT.cleanUp();
    }
}