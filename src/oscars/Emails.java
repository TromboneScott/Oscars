package oscars;

import java.net.URL;

public class Emails {
    public static void main(String[] inArgs) throws Exception {
        if (inArgs.length != 1)
            throw new IllegalArgumentException("Usage: Emails <URL>");
        Ballots.ballots(new URL(inArgs[0])).stream().filter(row -> !row[row.length - 1].isEmpty())
                .forEach(row -> System.out
                        .println(String.format("%s %s = %s", row[1], row[2], row[row.length - 1])));
    }
}