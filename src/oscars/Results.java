package oscars;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private static final String RESULTS_FILE = "results.xml";

    private static final String WINNER_DELIMITER = ",";

    private static final String YEAR = Paths.get(".").toAbsolutePath().normalize().getFileName()
            .toString();

    private final Map<Category, Set<String>> winners;

    private final Map<ShowTimeType, LocalDateTime> showTimes;

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
        String selectedResult = IOUtils.STDIN.readLine();
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
        return inCategory + " = " + String.join(", ", winners(inCategory));
    }

    private String toString(ShowTimeType inShowTimeType) {
        return inShowTimeType + " = " + get(inShowTimeType);
    }

    private void promptWinner(Category inCategory) throws IOException {
        System.out.println("\n" + toString(inCategory));

        String[] pickNames = inCategory.guesses.keySet().stream().toArray(String[]::new);
        for (int x = 0; x < pickNames.length; x++)
            System.out.println((x + 1) + ": " + pickNames[x] + " -> "
                    + Optional.ofNullable(inCategory.guessDescriptions.get(pickNames[x]))
                            .orElse("(no guesses so not downloaded)"));

        System.out.print("Select number(s) (use " + WINNER_DELIMITER
                + " to separate ties, leave blank to remove): ");
        String input = IOUtils.STDIN.readLine();
        try {
            winners.put(inCategory,
                    Collections.unmodifiableSet(
                            Stream.of((input + WINNER_DELIMITER).split(WINNER_DELIMITER))
                                    .mapToInt(Integer::parseInt).peek(number -> {
                                        if (number > pickNames.length || number < 1)
                                            throw new NumberFormatException();
                                    }).mapToObj(number -> pickNames[number - 1])
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
        String enteredTime = IOUtils.STDIN.readLine();
        if (enteredTime.isEmpty())
            showTimes.remove(inShowTimeType);
        else if ("*".equals(enteredTime))
            showTimes.put(inShowTimeType, LocalDateTime.now());
        else
            try {
                showTimes.put(inShowTimeType, LocalDateTime.parse(enteredTime));
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
        return Optional.ofNullable(showTimes.get(inShowTimeType))
                .map(showTime -> showTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .orElseGet(System::currentTimeMillis);
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

    public static void write(LocalDateTime inUpdated, Content... inContent) throws IOException {
        IOUtils.writeDocument(new Element("results")
                .addContent(new Element("year").addContent(YEAR))
                .addContent(
                        new Element("updated").addContent(inUpdated.atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a - z"))))
                .addContent(Arrays.asList(inContent)), RESULTS_FILE, null);
    }
}