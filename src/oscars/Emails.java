package oscars;

public class Emails {
    public static void main(String[] inArgs) throws Exception {
        Oscars.validateArgs(inArgs);
        Ballot.stream(inArgs).filter(ballot -> !ballot.getEmail().isEmpty()).forEach(
                ballot -> System.out.println(ballot.getName() + " = " + ballot.getEmail()));
    }
}