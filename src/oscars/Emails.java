package oscars;

import java.net.URL;

public class Emails {
    public static void main(String[] inArgs) throws Exception {
        Oscars.validateArgs(inArgs);
        new Ballots(new URL(inArgs[0])).all.stream().filter(row -> !email(row).isEmpty())
                .forEach(row -> System.out.println(Ballots.name(row) + " = " + email(row)));
    }

    private static String email(String[] inRow) {
        return inRow[inRow.length - 1];
    }
}