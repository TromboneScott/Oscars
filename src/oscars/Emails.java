package oscars;

import java.net.URL;

public class Emails {
    public static void main(String[] inArgs) throws Exception {
        Oscars.validateArgs(inArgs);
        Ballot.stream(new URL(inArgs[0])).filter(ballot -> !ballot.getEmail().isEmpty()).forEach(
                ballot -> System.out.println(ballot.getName() + " = " + ballot.getEmail()));
    }
}