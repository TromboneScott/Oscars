package oscars;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
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
import com.google.common.io.Files;

import oscars.column.Category;
import oscars.file.Directory;
import oscars.file.XMLFile;

/** The current results of the Oscars contest */
public class Results {
    /** Get user input from the system's standard input */
    public static final Scanner STDIN = new Scanner(System.in);

    public static final long UPDATE_TIME = TimeUnit.SECONDS.toMillis(10);

    private static final Instant CURTAIN = OffsetDateTime
            .parse(XMLFile.readDefinitionsFile().getAttributeValue("curtain")).toInstant();

    private static final XMLFile RESULTS_FILE = new XMLFile(Directory.DATA, "results.xml");

    private static final String WINNER_DELIMITER = ",";

    private static enum ShowTimeType {
        START,
        END;
    }

    private final ImmutableMap<Category, ImmutableMap<String, String>> nomineeMap;

    private final Map<Category, ImmutableSet<String>> winners;

    private final Map<ShowTimeType, ZonedDateTime> showTimes;

    private final Thread elapsedUpdater = new Thread(() -> {
        try {
            while (true) {
                writeElapsed();
                Thread.sleep(UPDATE_TIME);
            }
        } catch (InterruptedException e) {
            // Ignore - This means we're exiting gracefully
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

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
        elapsedUpdater.start();
    }

    /** Prompt for results and return whether or not the user wants to continue entering results */
    public boolean prompt() throws IOException {
        System.out.println(Font.title("RESULTS"));
        for (int line = 0; line < Category.ALL.size() + ShowTimeType.values().length; line++)
            System.out.println(Font.menuNumber(line)
                    + (line < Category.ALL.size() ? toHeader(Category.ALL.get(line), false)
                            : toHeader(ShowTimeType.values()[line - Category.ALL.size()], false)));

        System.out.print(Font.BROWN.apply("Enter number (0 to exit): "));
        String input = STDIN.nextLine();
        if ("0".equals(input)) {
            elapsedUpdater.interrupt();
            return false;
        }
        try {
            int entry = Integer.parseInt(input) - 1;
            if (entry < Category.ALL.size())
                promptWinner(Category.ALL.get(entry));
            else
                promptTime(ShowTimeType.values()[entry - Category.ALL.size()]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            outputInvalidInput(input);
        }
        return true;
    }

    public static void outputInvalidInput(String inInput) {
        System.out.println(Font.YELLOW.apply("\nInvalid response: ") + Font.INVERSE.apply(inInput));
    }

    private String toHeader(Category inCategory, boolean inTitle) {
        return toHeader(inCategory.name(), inTitle,
                String.join(Font.WHITE.apply(" AND "), winners(inCategory)));
    }

    private String toHeader(ShowTimeType inShowTimeType, boolean inTitle) {
        return toHeader("Show Time " + inShowTimeType, inTitle,
                ObjectUtils.toString(showTimes.get(inShowTimeType), () -> ""));
    }

    private static String toHeader(String inHeader, boolean inTitle, String inAnswer) {
        return (inTitle ? Font.title(inHeader) : inHeader)
                + (inAnswer.isEmpty() ? "" : " = " + Font.GREEN.apply(inAnswer));
    }

    private void promptWinner(Category inCategory) throws IOException {
        System.out.println("\n" + toHeader(inCategory, true));

        IntStream.range(0, inCategory.nominees().size()).forEach(x -> System.out.println(
                Font.menuNumber(x) + nomineeMap.get(inCategory).get(inCategory.nominees().get(x))));
        System.out.println(Font.CYAN.apply("Put a comma between multiple winners"));
        System.out.print(Font.BROWN.apply("Enter number(s) (leave blank to remove): "));
        String input = STDIN.nextLine();
        try {
            winners.put(inCategory, Stream.of((input + WINNER_DELIMITER).split(WINNER_DELIMITER))
                    .mapToInt(entry -> Integer.parseInt(entry) - 1).sorted()
                    .mapToObj(inCategory.nominees()::get).collect(ImmutableSet.toImmutableSet()));
            inCategory.writeChart();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            outputInvalidInput(input);
        }
    }

    private void promptTime(ShowTimeType inShowTimeType) throws IOException {
        System.out.println("\n" + toHeader(inShowTimeType, true));
        System.out.println("Format: " + Font.CYAN.apply(LocalDateTime.now() + "\n")
                + Font.BROWN.apply("Enter time (use * for system time or leave blank to remove):"));
        String input = STDIN.nextLine();
        if (input.isEmpty())
            showTimes.remove(inShowTimeType);
        else
            try {
                showTimes.put(inShowTimeType, "*".equals(input) ? ZonedDateTime.now()
                        : LocalDateTime.parse(input).atZone(ZoneId.systemDefault()));
            } catch (DateTimeParseException e) {
                outputInvalidInput(input);
            }
        writeElapsed();
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
    public static void write(Content inContent) throws IOException {
        RESULTS_FILE.write(new Element("results").addContent(inContent));
    }

    /** Write the elapsed time of the broadcast */
    private void writeElapsed() throws IOException {
        if (showTimes.containsKey(ShowTimeType.START))
            writeElapsed(Duration.between(showTimes.get(ShowTimeType.START),
                    showTimes.getOrDefault(ShowTimeType.END, ZonedDateTime.now())));
        else
            writeCountdown();
    }

    /** Write the countdown until the broadcast */
    public static void writeCountdown() throws IOException {
        writeElapsed(Duration.between(CURTAIN, Instant.now()));
    }

    /** Write the time until (negative) or since (positive) the start of the broadcast */
    private static void writeElapsed(Duration inElapsed) throws IOException {
        Files.write(String.valueOf(inElapsed.toMillis()).getBytes(),
                new File(Directory.DATA, "elapsed.txt"));
    }
}