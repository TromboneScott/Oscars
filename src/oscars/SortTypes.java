package oscars;

import java.io.IOException;

import org.jdom2.Element;

public final class SortTypes {
    private static final String[] COLUMNS = { "name", "rank", "bpr", "wpr", "score", "time" };

    public static void writePages() throws IOException {
        for (String column : COLUMNS) {
            writePage(column);
            writePage(column + "Reverse");
        }
        Directory.SORT.cleanUp();
    }

    private static void writePage(String inSortType) throws IOException {
        Directory.SORT.write(new Element("sort").addContent(inSortType), inSortType + ".xml",
                "sort.xsl");
    }
}