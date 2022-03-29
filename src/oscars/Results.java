package oscars;

/** Prompt and store Oscars results */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class Results {
    public static final String RESULTS_FILE = "results.xml";

    private static final String WINNER_DELIMITER = ",";

    private final Map<Category, Set<String>> winners = new HashMap<>();

    private final Map<ShowTimeType, LocalDateTime> showTimes = new HashMap<>();

    public Results(Collection<Category> inCategories) throws IOException {
        File resultsFile = new File(RESULTS_FILE);
        if (resultsFile.exists())
            try {
                Element resultsDOM = new SAXBuilder().build(resultsFile).getRootElement();
                Map<String, Category> categoryMap = inCategories.stream()
                        .collect(Collectors.toMap(category -> category.name, category -> category));
                winners.putAll(resultsDOM.getChild("categories").getChildren("category").stream()
                        .collect(Collectors.toMap(
                                categoryDOM -> categoryMap.get(categoryDOM.getChildText("name")),
                                categoryDOM -> Collections.unmodifiableSet(categoryDOM
                                        .getChild("nominees").getChildren("nominee").stream()
                                        .filter(nominee -> "correct"
                                                .equals(nominee.getAttributeValue("status")))
                                        .map(Element::getText).collect(Collectors.toSet())))));
                Stream.of(ShowTimeType.values())
                        .forEach(showTimeType -> Optional
                                .ofNullable(resultsDOM.getChild("showTime")
                                        .getChildText(showTimeType.name().toLowerCase()))
                                .filter(showTimeText -> !showTimeText.isEmpty())
                                .ifPresent(showTimeText -> showTimes.put(showTimeType,
                                        LocalDateTime.parse(showTimeText))));
            } catch (JDOMException e) {
                throw new IOException("ERROR: Unable to read results file: " + RESULTS_FILE, e);
            }
        else
            System.out.println("\nStarting new results file: " + RESULTS_FILE);

        while (!showTimes.containsKey(ShowTimeType.START))
            promptTime(ShowTimeType.START);
    }

    private boolean promptTime(ShowTimeType inShowTimeType) throws IOException {
        System.out.println("\n" + toString(inShowTimeType));
        System.out.println(
                "Enter * for system time, leave blank to remove, format: " + LocalDateTime.now());
        String enteredTime = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (enteredTime.isEmpty())
            if (inShowTimeType == ShowTimeType.START)
                System.out.println(ShowTimeType.START + " is required");
            else
                showTimes.remove(inShowTimeType);
        else if ("*".equals(enteredTime))
            showTimes.put(inShowTimeType, LocalDateTime.now());
        else
            try {
                showTimes.put(inShowTimeType, LocalDateTime.parse(enteredTime));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format");
            }
        return true;
    }

    private String toString(ShowTimeType inShowTimeType) {
        return inShowTimeType + " = " + getShowTime(inShowTimeType);
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
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Results");
        IntStream.range(0, inCategories.size()).forEach(resultNum -> System.out
                .println((resultNum + 1) + ": " + toString(inCategories.get(resultNum))));
        IntStream.range(0, ShowTimeType.values().length)
                .forEach(timeNum -> System.out.println((inCategories.size() + timeNum + 1) + ": "
                        + toString(ShowTimeType.values()[timeNum])));

        System.out.print("Enter results number to change (enter to quit): ");
        String selectedResult = stdin.readLine();
        if (selectedResult.isEmpty())
            return false;
        try {
            int resultNum = Integer.parseInt(selectedResult);
            if (resultNum > 0 && resultNum <= inCategories.size() + ShowTimeType.values().length)
                return resultNum > inCategories.size()
                        ? promptTime(ShowTimeType.values()[resultNum - inCategories.size() - 1])
                        : promptWinner(inCategories.get(resultNum - 1));
        } catch (NumberFormatException e) {
        }
        System.out.println("Invalid selection");
        return true;
    }

    private boolean promptWinner(Category inCategory) throws IOException {
        System.out.println("\n" + toString(inCategory));

        Set<String> pickNamesSet = new TreeSet<>(inCategory.guesses.keySet());
        String[] pickNames = pickNamesSet.toArray(new String[pickNamesSet.size()]);
        IntStream.range(0, pickNames.length)
                .forEach(x -> System.out.println((x + 1) + ": " + pickNames[x] + ": "
                        + Optional.ofNullable(inCategory.guessDescriptions.get(pickNames[x]))
                                .orElse("(no description - not guessed)")));

        System.out.print("Select winner number(s) (use " + WINNER_DELIMITER
                + " to separate ties or leave blank to remove winner): ");
        String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (input.isEmpty())
            winners.remove(inCategory);
        else {
            Set<String> winnerSet = new HashSet<>();
            for (String selectedWinner : input.split(WINNER_DELIMITER))
                try {
                    int selectedWinnerNum = Integer.parseInt(selectedWinner);
                    if (selectedWinnerNum > pickNames.length || selectedWinnerNum < 1)
                        throw new NumberFormatException();
                    winnerSet.add(pickNames[selectedWinnerNum - 1]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid selection");
                    return true;
                }
            winners.put(inCategory, Collections.unmodifiableSet(winnerSet));
        }
        inCategory.writeChart(this);
        return true;
    }

    private String toString(Category inCategory) {
        return inCategory + " = " + String.join(", ", winners(inCategory));
    }

    public String getShowTime(ShowTimeType inShowTimeType) {
        return Optional.ofNullable(showTimes.get(inShowTimeType)).map(LocalDateTime::toString)
                .orElse("");
    }

    /**
     * The elapsed time since the start of the broadcast in milliseconds
     *
     * @return The elapsed time, negative if the show hasn't started
     */
    public long elapsedTimeMillis() {
        return Math.max(0,
                Optional.ofNullable(showTimes.get(ShowTimeType.END)).map(this::toMillis).orElseGet(
                        System::currentTimeMillis) - toMillis(showTimes.get(ShowTimeType.START)));
    }

    private long toMillis(LocalDateTime inTime) {
        return inTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Get the winner(s) of the given category
     *
     * @param inCategory
     *            The category to get the winner(s) for
     * @return All the winners that have been entered for this category
     */
    public Set<String> winners(Category inCategory) {
        return Optional.ofNullable(winners.get(inCategory)).orElseGet(Collections::emptySet);
    }

    /**
     * The title text to use for the results
     *
     * @return The title text to use for the results
     */
    public String title() {
        return showTimes.get(ShowTimeType.START).getYear() + " OSCARS";
    }
}