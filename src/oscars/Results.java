package oscars;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
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

import oscars.column.Category;
import oscars.column.CategoryChart;
import oscars.file.Directory;
import oscars.file.XMLFile;

/** The current results of the Oscars contest */
public class Results {
    /** Get user input from the system's standard input */
    public static final Scanner STDIN = new Scanner(System.in);

    private static final XMLFile RESULTS_FILE = new XMLFile(Directory.DATA, "results.xml");

    private static final String WINNER_DELIMITER = ",";

    private static enum ShowTimeType {
        START,
        END;
    }

    private final ImmutableMap<Category, ImmutableMap<String, String>> nomineeMap;

    private final Map<Category, ImmutableSet<String>> winners;

    private final Map<ShowTimeType, ZonedDateTime> showTimes;

    /** Read existing Results or create new Results including the given nominee descriptions */
    public Results(ImmutableMap<Category, ImmutableMap<String, String>> inNomineeMap)
            throws IOException {
        nomineeMap = inNomineeMap;
        try {
            Element awardsDOM = RESULTS_FILE.read().map(element -> element.getChild("awards"))
                    .orElseGet(() -> new Element("EMPTY"));
            winners = awardsDOM.getChildren("category").stream()
                    .collect(Collectors.toMap(
                            categoryDOM -> Category.of(categoryDOM.getAttributeValue("name")),
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

    /** Prompt for results and return whether or not the user wants to continue entering results */
    public boolean prompt() throws IOException {
        System.out.println("\033[1;4mRESULTS\033[m");
        for (int line = 0; line < Category.ALL.size() + ShowTimeType.values().length; line++)
            System.out.println(formatNumber(line)
                    + (line < Category.ALL.size() ? toString(Category.ALL.get(line))
                            : toString(ShowTimeType.values()[line - Category.ALL.size()])));

        System.out.print("\033[33mEnter number (0 to exit):\033[m ");
        String input = STDIN.nextLine();
        if ("0".equals(input))
            return false;
        try {
            int entry = Integer.parseInt(input) - 1;
            if (entry < Category.ALL.size())
                promptWinner(Category.ALL.get(entry));
            else
                promptTime(ShowTimeType.values()[entry - Category.ALL.size()]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("\nInvalid selection: " + input);
        }
        return true;
    }

    private String toString(Category inCategory) {
        return inCategory.name() + formatAnswer(String.join(", ", winners(inCategory)));
    }

    private String toString(ShowTimeType inShowTimeType) {
        return "Show Time " + inShowTimeType
                + formatAnswer(ObjectUtils.toString(showTimes.get(inShowTimeType), () -> ""));
    }

    private String formatAnswer(String inAnswer) {
        return "\033[m" + (inAnswer.isEmpty() ? "" : " = \033[32m" + inAnswer + "\033[m");
    }

    private String formatNumber(int inNumber) {
        return String.format("\033[33m%2d\033[m: ", inNumber + 1);
    }

    private void promptWinner(Category inCategory) throws IOException {
        System.out.println("\n\033[1;4m" + toString(inCategory));

        IntStream.range(0, inCategory.nominees().size()).forEach(x -> System.out.println(
                formatNumber(x) + nomineeMap.get(inCategory).get(inCategory.nominees().get(x))));
        System.out.print("\033[33mSelect number(s) (use " + WINNER_DELIMITER
                + " to separate ties, leave blank to remove):\033[m ");
        String input = STDIN.nextLine();
        try {
            winners.put(inCategory, Stream.of((input + WINNER_DELIMITER).split(WINNER_DELIMITER))
                    .mapToInt(entry -> Integer.parseInt(entry) - 1).sorted()
                    .mapToObj(inCategory.nominees()::get).collect(ImmutableSet.toImmutableSet()));
            new CategoryChart(inCategory).write();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("\nInvalid selection: " + input);
        }
    }

    private void promptTime(ShowTimeType inShowTimeType) {
        System.out.println("\n\033[1;4m" + toString(inShowTimeType));
        System.out.println("Format: \033[36m" + LocalDateTime.now()
                + "\n\033[33mEnter time (use * for system time or leave blank to remove):\033[m");
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

    /** Get the elapsed time in seconds since the start of the broadcast */
    public long elapsedTimeSeconds() {
        return Math.max(0, TimeUnit.MILLISECONDS.toSeconds(millisSinceStart()));
    }

    /** Get the number of milliseconds since the start of the broadcast (can be negative) */
    public long millisSinceStart() {
        return timeInMillis(ShowTimeType.END) - timeInMillis(ShowTimeType.START);
    }

    private Long timeInMillis(ShowTimeType inShowTimeType) {
        return Optional.ofNullable(showTimes.get(inShowTimeType)).map(ZonedDateTime::toInstant)
                .orElseGet(Instant::now).toEpochMilli();
    }

    /** Get the winner(s) of the given category in display order, may be empty but won't be null */
    public ImmutableSet<String> winners(Category inCategory) {
        return winners.computeIfAbsent(inCategory, k -> ImmutableSet.of());
    }

    /** Get the DOM Element for these Results */
    public Element toDOM() {
        return Category.ALL.stream()
                .map(category -> winners(category).stream()
                        .map(winner -> new Element("nominee").setAttribute("name", winner))
                        .reduce(category.toDOM(), Element::addContent))
                .reduce(new Element("awards"), Element::addContent)
                .setAttributes(Stream.of(ShowTimeType.values()).filter(showTimes::containsKey)
                        .map(type -> new Attribute(type.name(), showTimes.get(type).toString()))
                        .collect(ImmutableList.toImmutableList()));
    }

    /** Write the given content to the results XML file */
    public static void write(ZonedDateTime inUpdated, Content... inContent) throws IOException {
        String updated = inUpdated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a - z"));
        RESULTS_FILE.write(new Element("results").setAttribute("updated", updated)
                .addContent(ImmutableList.copyOf(inContent)));
    }
}