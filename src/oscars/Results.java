package oscars;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

/** The current results of the Oscars contest */
public class Results {
    /** Get user input from the system's standard input */
    public static final Scanner STDIN = new Scanner(System.in);

    private static final String RESULTS_FILE = "results.xml";

    private static final String WINNER_DELIMITER = ",";

    private static final DateTimeFormatter UPDATED_PATTERN = DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm:ss a - z");

    private static enum ShowTimeType {
        START,
        END;
    }

    private final Map<String, Map<String, String>> nomineeDescriptions;

    private final Map<String, Collection<String>> winners;

    private final Map<ShowTimeType, ZonedDateTime> showTimes;

    /** Read existing Results or create new Results including the given nominee descriptions */
    public Results(Map<String, Map<String, String>> inNomineeDescriptions) throws IOException {
        nomineeDescriptions = inNomineeDescriptions;
        Element awardsDOM = Directory.DATA.getRootElement(RESULTS_FILE)
                .map(element -> element.getChild("awards")).orElseGet(() -> new Element("EMPTY"));
        winners = winners(awardsDOM);
        showTimes = showTimes(awardsDOM);
    }

    /**
     * Prompt for results
     * 
     * @param inPlayers
     *            Players whose picks we can count for the category chart
     * @return Whether or not the user wants to continue entering results
     */
    public boolean prompt(List<Player> inPlayers) throws IOException {
        System.out.println("Results");
        for (int resultNum = 0; resultNum < Category.ALL.size()
                + ShowTimeType.values().length; resultNum++)
            System.out.println((resultNum + 1) + ": "
                    + (resultNum < Category.ALL.size() ? toString(Category.ALL.get(resultNum).name)
                            : toString(ShowTimeType.values()[resultNum - Category.ALL.size()])));

        System.out.print("Enter number to change (\"exit\" to quit): ");
        String input = STDIN.nextLine();
        if ("exit".equalsIgnoreCase(input))
            return false;
        try {
            int resultNum = Integer.parseInt(input) - 1;
            if (resultNum < 0 || resultNum >= Category.ALL.size() + ShowTimeType.values().length)
                throw new NumberFormatException();
            if (resultNum < Category.ALL.size())
                promptWinner(Category.ALL.get(resultNum), inPlayers);
            else
                promptTime(ShowTimeType.values()[resultNum - Category.ALL.size()]);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid selection: " + input);
        }
        return true;
    }

    private String toString(String inCategory) {
        return inCategory + " = " + String.join(", ", winners(inCategory));
    }

    private String toString(ShowTimeType inShowTimeType) {
        return "Show Time " + inShowTimeType + " = " + Optional
                .ofNullable(showTimes.get(inShowTimeType)).map(Object::toString).orElse("");
    }

    private void promptWinner(Category inCategory, List<Player> inPlayers) throws IOException {
        System.out.println("\n" + toString(inCategory.name));

        IntStream.range(0, inCategory.nominees.size()).forEach(x -> System.out.println((x + 1)
                + ": " + nomineeDescriptions.get(inCategory.name).get(inCategory.nominees.get(x))));
        System.out.print("Select number(s) (use " + WINNER_DELIMITER
                + " to separate ties, leave blank to remove): ");
        String input = STDIN.nextLine();
        try {
            winners.put(inCategory.name,
                    Collections.unmodifiableCollection(
                            Stream.of((input + WINNER_DELIMITER).split(WINNER_DELIMITER))
                                    .mapToInt(Integer::parseInt).peek(number -> {
                                        if (number > inCategory.nominees.size() || number < 1)
                                            throw new NumberFormatException();
                                    }).sorted()
                                    .mapToObj(number -> inCategory.nominees.get(number - 1))
                                    .collect(Collectors.toCollection(LinkedHashSet::new))));
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

    /** Get whether or not the Oscars broadcast has ended */
    public boolean showEnded() {
        return showTimes.containsKey(ShowTimeType.END);
    }

    /** Get the elapsed time in milliseconds since the start of the broadcast */
    public long elapsedTimeMillis() {
        return Math.max(0, getMillis(ShowTimeType.END) - getMillis(ShowTimeType.START));
    }

    private Long getMillis(ShowTimeType inShowTimeType) {
        return Optional.ofNullable(showTimes.get(inShowTimeType)).map(ZonedDateTime::toInstant)
                .orElseGet(Instant::now).toEpochMilli();
    }

    /** Get the winner(s) of the given category in display order */
    public Collection<String> winners(String inCategory) {
        return winners.computeIfAbsent(inCategory, k -> Collections.emptySet());
    }

    /** Write the given content to the results XML file */
    public static void write(ZonedDateTime inUpdated, Content... inContent) throws IOException {
        Directory.DATA.write(
                new Element("results").setAttribute("updated", inUpdated.format(UPDATED_PATTERN))
                        .addContent(Arrays.asList(inContent)),
                RESULTS_FILE, null);
    }

    /** Get the awards DOM Element for the current Results */
    public Element awardsDOM() {
        return Category.ALL.stream()
                .map(category -> winners(category.name).stream()
                        .map(winner -> new Element("nominee").setAttribute("name", winner))
                        .reduce(new Element("category").setAttribute("name", category.name),
                                Element::addContent))
                .reduce(new Element("awards"), Element::addContent)
                .setAttributes(Stream.of(ShowTimeType.values()).filter(showTimes::containsKey)
                        .map(type -> new Attribute(type.name().toLowerCase(),
                                showTimes.get(type).toString()))
                        .collect(Collectors.toList()))
                .setAttribute("duration",
                        String.valueOf(TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis())));
    }

    private static Map<String, Collection<String>> winners(Element inAwardsDOM) {
        return inAwardsDOM.getChildren("category").stream().collect(Collectors.toMap(
                categoryDOM -> categoryDOM.getAttributeValue("name"),
                categoryDOM -> Collections.unmodifiableCollection(categoryDOM.getChildren("nominee")
                        .stream().map(element -> element.getAttributeValue("name"))
                        .collect(Collectors.toCollection(LinkedHashSet::new)))));
    }

    private static Map<ShowTimeType, ZonedDateTime> showTimes(Element inAwardsDOM) {
        return Stream.of(ShowTimeType.values())
                .filter(type -> inAwardsDOM.getAttributeValue(type.name().toLowerCase()) != null)
                .collect(Collectors.toMap(type -> type, type -> ZonedDateTime
                        .parse(inAwardsDOM.getAttributeValue(type.name().toLowerCase()))));
    }
}