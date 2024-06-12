package oscars;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

public class Results {
    public static final Scanner STDIN = new Scanner(System.in);

    private static final String RESULTS_FILE = "results.xml";

    private static final String WINNER_DELIMITER = ",";

    private static final String YEAR = Paths.get("").toAbsolutePath().normalize().getFileName()
            .toString();

    private final Map<String, Map<String, String>> nomineeDescriptions;

    private final Map<String, Set<String>> winners;

    private final Map<ShowTimeType, ZonedDateTime> showTimes;

    public Results(Map<String, Map<String, String>> inNomineeDescriptions) throws IOException {
        nomineeDescriptions = inNomineeDescriptions;
        Element resultsDOM = Directory.DATA.getRootElement(RESULTS_FILE);
        if (resultsDOM == null)
            System.out.println("\nStarting new results file: " + RESULTS_FILE);
        winners = winners(resultsDOM);
        showTimes = showTimes(resultsDOM);
    }

    /**
     * Prompt for results
     * 
     * @param inPlayers
     *            Players whose picks we can count
     * @return true unless user wants to exit the program
     */
    public boolean prompt(List<Player> inPlayers) throws IOException {
        System.out.println("Results");
        Category[] categories = Category.stream().toArray(Category[]::new);
        for (int resultNum = 0; resultNum < categories.length
                + ShowTimeType.values().length; resultNum++)
            System.out.println((resultNum + 1) + ": "
                    + (resultNum < categories.length ? toString(categories[resultNum].name)
                            : toString(ShowTimeType.values()[resultNum - categories.length])));

        System.out.print("Enter number to change (\"exit\" to quit): ");
        String input = STDIN.nextLine();
        if ("exit".equalsIgnoreCase(input))
            return false;
        try {
            int resultNum = Integer.parseInt(input) - 1;
            if (resultNum < 0 || resultNum >= categories.length + ShowTimeType.values().length)
                throw new NumberFormatException();
            if (resultNum < categories.length)
                promptWinner(categories[resultNum], inPlayers);
            else
                promptTime(ShowTimeType.values()[resultNum - categories.length]);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid selection: " + input);
        }
        return true;
    }

    private String toString(String inCategory) {
        return inCategory + " = " + String.join(", ", winners(inCategory));
    }

    private String toString(ShowTimeType inShowTimeType) {
        return inShowTimeType + " = " + get(inShowTimeType);
    }

    private void promptWinner(Category inCategory, List<Player> inPlayers) throws IOException {
        System.out.println("\n" + toString(inCategory.name));

        for (int x = 0; x < inCategory.nominees.size(); x++)
            System.out.println((x + 1) + ": "
                    + nomineeDescriptions.get(inCategory.name).get(inCategory.nominees.get(x)));

        System.out.print("Select number(s) (use " + WINNER_DELIMITER
                + " to separate ties, leave blank to remove): ");
        String input = STDIN.nextLine();
        try {
            winners.put(inCategory.name,
                    Collections.unmodifiableSet(new LinkedHashSet<>(
                            Stream.of((input + WINNER_DELIMITER).split(WINNER_DELIMITER))
                                    .mapToInt(Integer::parseInt).peek(number -> {
                                        if (number > inCategory.nominees.size() || number < 1)
                                            throw new NumberFormatException();
                                    }).sorted()
                                    .mapToObj(number -> inCategory.nominees.get(number - 1))
                                    .collect(Collectors.toList()))));
            inCategory.writeChart(this, inPlayers);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid selection: " + input);
        }
    }

    private void promptTime(ShowTimeType inShowTimeType) {
        System.out.println("\n" + toString(inShowTimeType));
        System.out.println(
                "Enter * for system time, leave blank to remove, format: " + LocalDateTime.now());
        String input = STDIN.nextLine();
        if (input.isEmpty())
            showTimes.remove(inShowTimeType);
        else if ("*".equals(input))
            showTimes.put(inShowTimeType, ZonedDateTime.now());
        else
            try {
                showTimes.put(inShowTimeType,
                        LocalDateTime.parse(input).atZone(ZoneId.systemDefault()));
            } catch (DateTimeParseException e) {
                System.out.println("\nInvalid time: " + input);
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
    public Set<String> winners(String inCategory) {
        return winners.computeIfAbsent(inCategory, k -> Collections.emptySet());
    }

    public static void write(ZonedDateTime inUpdated, Content... inContent) throws IOException {
        Directory.DATA.write(new Element("results").setAttribute("year", YEAR)
                .setAttribute("updated",
                        inUpdated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a - z")))
                .addContent(Arrays.asList(inContent)), RESULTS_FILE, null);
    }

    public Element awardsDOM() {
        return Category.stream()
                .map(category -> winners.get(category.name).stream()
                        .map(winner -> new Element("nominee").setAttribute("name", winner))
                        .reduce(new Element("category").setAttribute("name", category.name),
                                Element::addContent))
                .reduce(new Element("awards"), Element::addContent)
                .setAttributes(showTimes.entrySet().stream()
                        .map(entry -> new Attribute(entry.getKey().name().toLowerCase(),
                                entry.getValue().toString()))
                        .collect(Collectors.toList()))
                .setAttribute("length",
                        Standings.formatTime(TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis())));
    }

    private static Map<String, Set<String>> winners(Element inResultsDOM) {
        return Optional.ofNullable(inResultsDOM).map(element -> element.getChild("awards"))
                .map(element -> element.getChildren("category").stream()).orElseGet(Stream::empty)
                .collect(Collectors.toMap(categoryDOM -> categoryDOM.getAttributeValue("name"),
                        categoryDOM -> Collections.unmodifiableSet(
                                new LinkedHashSet<>(categoryDOM.getChildren("nominee").stream()
                                        .map(element -> element.getAttributeValue("name"))
                                        .collect(Collectors.toList())))));
    }

    private static Map<ShowTimeType, ZonedDateTime> showTimes(Element inResultsDOM) {
        return Optional.ofNullable(inResultsDOM).map(element -> element.getChild("awards"))
                .map(element -> Stream.of(ShowTimeType.values())
                        .map(type -> new SimpleEntry<>(type,
                                element.getAttributeValue(type.name().toLowerCase())))
                        .filter(entry -> entry.getValue() != null))
                .orElseGet(Stream::empty).collect(Collectors.toMap(Entry::getKey,
                        entry -> ZonedDateTime.parse(entry.getValue())));
    }
}