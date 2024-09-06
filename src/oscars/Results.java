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
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/** The current results of the Oscars contest */
public class Results {
    /** Get user input from the system's standard input */
    public static final Scanner STDIN = new Scanner(System.in);

    private static final String RESULTS_FILE = "results.xml";

    private static final String WINNER_DELIMITER = ",";

    private static enum ShowTimeType {
        START,
        END;
    }

    private final ImmutableMap<Column, ImmutableMap<String, String>> nomineeDescriptions;

    private final Map<Column, ImmutableSet<String>> winners;

    private final Map<ShowTimeType, ZonedDateTime> showTimes;

    /** Read existing Results or create new Results including the given nominee descriptions */
    public Results(Function<Column, ImmutableMap<String, String>> inNominees) throws IOException {
        nomineeDescriptions = Column.CATEGORIES.stream()
                .collect(ImmutableMap.toImmutableMap(category -> category, inNominees::apply));
        try {
            Element awardsDOM = Directory.DATA.getRootElement(RESULTS_FILE)
                    .map(element -> element.getChild("awards"))
                    .orElseGet(() -> new Element("EMPTY"));
            winners = awardsDOM.getChildren("category").stream()
                    .collect(Collectors.toMap(
                            categoryDOM -> Column.of(categoryDOM.getAttributeValue("name")),
                            categoryDOM -> categoryDOM.getChildren("nominee").stream()
                                    .map(nomineeDOM -> nomineeDOM.getAttributeValue("name"))
                                    .collect(ImmutableSet.toImmutableSet())));
            showTimes = Stream.of(ShowTimeType.values())
                    .filter(type -> awardsDOM.getAttribute(type.name()) != null)
                    .collect(Collectors.toMap(type -> type,
                            type -> ZonedDateTime.parse(awardsDOM.getAttributeValue(type.name()))));
        } catch (Exception e) {
            throw new IOException("Error reading results file: " + RESULTS_FILE, e);
        }
    }

    /**
     * Prompt for results
     * 
     * @param inPlayers
     *            The Players with their picks in each category
     * @return Whether or not the user wants to continue entering results
     */
    public boolean prompt(Collection<Player> inPlayers) throws IOException {
        System.out.println("Results");
        for (int line = 0; line < Column.CATEGORIES.size() + ShowTimeType.values().length; line++)
            System.out.println((line + 1) + ": "
                    + (line < Column.CATEGORIES.size() ? toString(Column.CATEGORIES.get(line))
                            : toString(ShowTimeType.values()[line - Column.CATEGORIES.size()])));

        System.out.print("Enter number (0 to exit): ");
        String input = STDIN.nextLine();
        if ("0".equals(input))
            return false;
        try {
            int entry = Integer.parseInt(input) - 1;
            if (entry < Column.CATEGORIES.size())
                promptWinner(Column.CATEGORIES.get(entry), inPlayers);
            else
                promptTime(ShowTimeType.values()[entry - Column.CATEGORIES.size()]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("\nInvalid selection: " + input);
        }
        return true;
    }

    private String toString(Column inCategory) {
        return inCategory + " = " + String.join(", ", winners(inCategory));
    }

    private String toString(ShowTimeType inShowTimeType) {
        return "Show Time " + inShowTimeType + " = "
                + ObjectUtils.toString(showTimes.get(inShowTimeType), () -> "");
    }

    private void promptWinner(Column inCategory, Collection<Player> inPlayers) throws IOException {
        System.out.println("\n" + toString(inCategory));

        IntStream.range(0, inCategory.nominees().size()).forEach(x -> System.out.println((x + 1)
                + ": " + nomineeDescriptions.get(inCategory).get(inCategory.nominees().get(x))));
        System.out.print("Select number(s) (use " + WINNER_DELIMITER
                + " to separate ties, leave blank to remove): ");
        String input = STDIN.nextLine();
        try {
            winners.put(inCategory, Stream.of((input + WINNER_DELIMITER).split(WINNER_DELIMITER))
                    .mapToInt(entry -> Integer.parseInt(entry) - 1).sorted()
                    .mapToObj(inCategory.nominees()::get).collect(ImmutableSet.toImmutableSet()));
            inCategory.writeChart(inPlayers, this);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("\nInvalid selection: " + input);
        }
    }

    private void promptTime(ShowTimeType inShowTimeType) {
        System.out.println("\n" + toString(inShowTimeType));
        System.out.println("Format: " + LocalDateTime.now()
                + "\nEnter time (use * for system time or leave blank to remove):");
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
        return Math.max(0, timeInMillis(ShowTimeType.END) - timeInMillis(ShowTimeType.START));
    }

    private Long timeInMillis(ShowTimeType inShowTimeType) {
        return Optional.ofNullable(showTimes.get(inShowTimeType)).map(ZonedDateTime::toInstant)
                .orElseGet(Instant::now).toEpochMilli();
    }

    /** Get the winner(s) of the given category in display order, may be empty but won't be null */
    public ImmutableSet<String> winners(Column inCategory) {
        return winners.computeIfAbsent(inCategory, k -> ImmutableSet.of());
    }

    /** Write the given content to the results XML file */
    public static void write(ZonedDateTime inUpdated, Content... inContent) throws IOException {
        String updated = inUpdated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a - z"));
        Directory.DATA.write(new Element("results").setAttribute("updated", updated)
                .addContent(Arrays.asList(inContent)), RESULTS_FILE, null);
    }

    /** Write these Results and the given Standings to the results XML file */
    public void write(ZonedDateTime inUpdated, Standings inStandings) throws IOException {
        Element awardsDOM = Column.CATEGORIES.stream()
                .map(category -> winners(category).stream()
                        .map(winner -> new Element("nominee").setAttribute("name", winner))
                        .reduce(new Element("category").setAttribute("name", category.name()),
                                Element::addContent))
                .reduce(new Element("awards"), Element::addContent)
                .setAttributes(Stream.of(ShowTimeType.values()).filter(showTimes::containsKey)
                        .map(type -> new Attribute(type.name(), showTimes.get(type).toString()))
                        .collect(ImmutableList.toImmutableList()));
        write(inUpdated, awardsDOM, inStandings.toDOM());
    }
}