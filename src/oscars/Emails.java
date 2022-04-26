package oscars;

import java.net.URL;

public class Emails {
    public static void main(String[] inArgs) throws Exception {
        Oscars.validateArgs(inArgs);
        new Ballots(new URL(inArgs[0])).all.stream().filter(row -> !row[row.length - 1].isEmpty())
                .forEach(row -> System.out
                        .println(String.format("%s %s = %s", row[1], row[2], row[row.length - 1])));
    }
}