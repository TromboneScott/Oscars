package oscars;

public class Emails {
    public static void main(String[] inArgs) throws Exception {
        Ballot.stream().filter(ballot -> !ballot.getEmail().isEmpty()).forEach(
                ballot -> System.out.println(ballot.getName() + " = " + ballot.getEmail()));
    }
}