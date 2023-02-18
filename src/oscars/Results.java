package oscars;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class Results {
    public static final BufferedReader STDIN = new BufferedReader(new InputStreamReader(System.in));

    private static final String RESULTS_FILE = "results.xml";

    private static final String WINNER_DELIMITER = ",";

    private static final String YEAR = Paths.get(".").toAbsolutePath().normalize().getFileName()
            .toString();

    private final Map<Category, Set<String>> winners;

    private final Map<ShowTimeType, ZonedDateTime> showTimes;

    public Results(Collection<Category> inCategories) throws IOException {
        File resultsFile = new File(RESULTS_FILE);
        if (resultsFile.exists())
            try {
                Element resultsDOM = new SAXBuilder().build(resultsFile).getRootElement();
                winners = Standings.winners(resultsDOM, inCategories.stream().collect(
                        Collectors.toMap(category -> category.name, category -> category)));
                showTimes = Standings.showTimes(resultsDOM);
            } catch (JDOMException e) {
                throw new IOException("ERROR: Unable to read results file: " + RESULTS_FILE, e);
            }
        else {
            System.out.println("\nStarting new results file: " + RESULTS_FILE);
            winners = new HashMap<>();
            showTimes = new HashMap<>();
        }
    }

    /**
     * Prompt for results
     *
     * @param inCategories
     *            Categories to prompt for
     * @param inPlayers
     *            Players whose picks we can choose from
     * @return true unless user wants to exit the program
     */
    public boolean prompt(List<Category> inCategories) throws IOException {
        System.out.println("Results");
        for (int resultNum = 0; resultNum < inCategories.size()
                + ShowTimeType.values().length; resultNum++)
            System.out.println((resultNum + 1) + ": "
                    + (resultNum < inCategories.size() ? toString(inCategories.get(resultNum))
                            : toString(ShowTimeType.values()[resultNum - inCategories.size()])));

        System.out.print("Enter number to change (\"exit\" to quit): ");
        String selectedResult = STDIN.readLine();
        if ("exit".equalsIgnoreCase(selectedResult))
            return false;
        try {
            int resultNum = Integer.parseInt(selectedResult) - 1;
            if (resultNum < 0 || resultNum >= inCategories.size() + ShowTimeType.values().length)
                throw new NumberFormatException();
            if (resultNum < inCategories.size())
                promptWinner(inCategories.get(resultNum));
            else
                promptTime(ShowTimeType.values()[resultNum - inCategories.size()]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection: " + selectedResult);
        }
        return true;
    }

    private String toString(Category inCategory) {
        return inCategory.name + " = " + String.join(", ", winners(inCategory));
    }

    private String toString(ShowTimeType inShowTimeType) {
        return inShowTimeType + " = " + get(inShowTimeType);
    }

    private void promptWinner(Category inCategory) throws IOException {
        System.out.println("\n" + toString(inCategory));

        for (int x = 0; x < inCategory.nominees.size(); x++)
            System.out.println((x + 1) + ": " + inCategory.nominees.get(x).name + " -> "
                    + inCategory.nominees.get(x).description);

        System.out.print("Select number(s) (use " + WINNER_DELIMITER
                + " to separate ties, leave blank to remove): ");
        String input = STDIN.readLine();
        try {
            winners.put(inCategory,
                    Collections.unmodifiableSet(
                            Stream.of((input + WINNER_DELIMITER).split(WINNER_DELIMITER))
                                    .mapToInt(Integer::parseInt).peek(number -> {
                                        if (number > inCategory.nominees.size() || number < 1)
                                            throw new NumberFormatException();
                                    }).mapToObj(number -> inCategory.nominees.get(number - 1).name)
                                    .collect(Collectors.toSet())));
            inCategory.writeChart(this);
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection: " + input);
        }
    }

    private void promptTime(ShowTimeType inShowTimeType) throws IOException {
        System.out.println("\n" + toString(inShowTimeType));
        System.out.println(
                "Enter * for system time, leave blank to remove, format: " + LocalDateTime.now());
        String enteredTime = STDIN.readLine();
        if (enteredTime.isEmpty())
            showTimes.remove(inShowTimeType);
        else if ("*".equals(enteredTime))
            showTimes.put(inShowTimeType, ZonedDateTime.now());
        else
            try {
                showTimes.put(inShowTimeType,
                        LocalDateTime.parse(enteredTime).atZone(ZoneId.systemDefault()));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time: " + enteredTime);
            }
    }

    public String get(ShowTimeType inShowTimeType) {
        return Optional.ofNullable(showTimes.get(inShowTimeType)).map(Object::toString).orElse("");
    }

    /**
     * The elapsed time since the start of the broadcast in milliseconds
     *
     * @return The elapsed time, zero if the show hasn't started
     */
    public long elapsedTimeMillis() {
        return Math.max(0, getMillis(ShowTimeType.END) - getMillis(ShowTimeType.START));
    }

    private Long getMillis(ShowTimeType inShowTimeType) {
        return Optional.ofNullable(showTimes.get(inShowTimeType)).map(ZonedDateTime::toInstant)
                .orElseGet(Instant::now).toEpochMilli();
    }

    /**
     * Get the winner(s) of the given category
     *
     * @param inCategory
     *            The category to get the winner(s) for
     * @return All the winners that have been entered for this category
     */
    public Set<String> winners(Category inCategory) {
        return winners.computeIfAbsent(inCategory, k -> Collections.emptySet());
    }

    public static void write(ZonedDateTime inUpdated, Content... inContent) throws IOException {
        Directory.CURRENT.write(new Element("results")
                .addContent(new Element("year").addContent(YEAR))
                .addContent(new Element("updated").addContent(
                        inUpdated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a - z"))))
                .addContent(Arrays.asList(inContent)), RESULTS_FILE, null);
    }
}