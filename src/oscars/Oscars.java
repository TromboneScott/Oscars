package oscars;

/** Oscars - Calculate the standings in the Oscars competition */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * This program will allow Oscars winners to be selected and a new results file will be generated.
 * The Oscars picks for each player are in a comma delimited file which won't change during the
 * contest. A separate file indicates the column names of that file and all nominees for each
 * category the way they should be displayed on the web site. The column name can include a tie
 * breaker number inside parentheses like this: Director(1) to indicate that Director is the first
 * tie breaker. The contestants' names must be in the columns named "First" and "Last" and their
 * time estimate must be in the column named "Time" in the format "H:MM" or "H:MM:SS.D".
 *
 * @author Scott McDonald
 * @version 4.5
 */
public class Oscars implements Runnable {
    private static final String CATEGORY_MAPS_FILE = "categoryMaps.xml";

    private static final String PLAYERS_FILE = "players.csv";

    private static final String CATEGORIES_FILE = "categories.csv";

    private static final String VALUE_DELIMITER = ",";

    private static final String COMMA_REPLACEMENT = "`";

    private final List<Player> players;

    private final List<Category> categories;

    private final Results results;

    private final String scoreFormat;

    private long runningTime;

    private long elapsedTime;

    /**
     * Prompt for Oscars results, store them and create output files
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] inArgs) throws IOException, InterruptedException {
        new Oscars().process();
    }

    private Oscars() throws IOException {
        System.out.print("Loading data... ");
        List<String[]> playerValues = readValues(PLAYERS_FILE);
        List<String[]> categoryValues = readValues(CATEGORIES_FILE);
        System.out.println("DONE");

        List<? extends List<String>> categoryNominees = categoryNominees(categoryValues);
        Map<String, Map<String, String>> categoryMaps = categoryMaps(categoryValues, playerValues,
                categoryNominees);
        Category[] categoryArray = buildCategories(categoryValues.get(0), categoryNominees,
                categoryMaps, playerValues);
        players = Collections.unmodifiableList(
                buildPlayers(playerValues, categoryArray, categoryValues.get(0), categoryMaps));
        categories = Collections.unmodifiableList(Arrays.stream(categoryArray)
                .filter(category -> !category.guesses.isEmpty()).collect(Collectors.toList()));
        results = new Results(categories);
        scoreFormat = "%." + categories.stream()
                .filter(category -> !category.tieBreakerValue.isEmpty()).count() + "f";
    }

    private static List<String[]> readValues(String inFileName) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(inFileName))) {
            return stream.map(line -> Stream.of(line.split(VALUE_DELIMITER, -1))
                    .map(value -> value.replace(COMMA_REPLACEMENT, VALUE_DELIMITER))
                    .toArray(String[]::new)).collect(Collectors.toList());
        }
    }

    private Map<String, Map<String, String>> categoryMaps(List<String[]> inCategoryValues,
            List<String[]> inPlayerValues, List<? extends List<String>> inCategoryNominees)
            throws IOException {
        Map<String, Map<String, String>> categoryMaps = readCategoryMaps();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        for (int categoryNum = 0; categoryNum < inCategoryValues.get(0).length; categoryNum++) {
            String categoryName = inCategoryValues.get(0)[categoryNum];
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(categoryName,
                    k -> new HashMap<>());
            List<String> nominees = inCategoryNominees.get(categoryNum);
            if (!nominees.isEmpty())
                for (String[] guesses : inPlayerValues)
                    if (!categoryMap.containsKey(guesses[categoryNum])) {
                        System.out.println("\nCATEGORY: " + categoryName);
                        IntStream.range(0, nominees.size()).forEach(nomineeNum -> System.out
                                .println((nomineeNum + 1) + ": " + nominees.get(nomineeNum)));
                        System.out.print(guesses[categoryNum] + " = ");
                        String guessNum = stdin.readLine();
                        categoryMap.put(guesses[categoryNum],
                                nominees.get(Integer.parseInt(guessNum) - 1));
                    }
        }
        writeCategoryMaps(categoryMaps);
        return categoryMaps;
    }

    private List<? extends List<String>> categoryNominees(List<String[]> inCategoryValues) {
        return IntStream.range(0, inCategoryValues.get(0).length)
                .mapToObj(categoryNum -> inCategoryValues.stream().skip(1)
                        .map(guesses -> guesses[categoryNum]).filter(guess -> !guess.isEmpty())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, Map<String, String>> readCategoryMaps() throws IOException {
        File categoryMapsFile = new File(CATEGORY_MAPS_FILE);
        if (categoryMapsFile.exists())
            try {
                return new SAXBuilder().build(categoryMapsFile).getRootElement()
                        .getChildren("category").stream()
                        .collect(Collectors.toMap(categoryDOM -> categoryDOM.getChildText("name"),
                                categoryDOM -> categoryDOM.getChildren("map").stream()
                                        .collect(Collectors.toMap(
                                                mapDOM -> mapDOM.getChildText("key"),
                                                mapDOM -> mapDOM.getChildText("value")))));
            } catch (JDOMException e) {
                throw new IOException(
                        "ERROR: Unable to read category maps file: " + CATEGORY_MAPS_FILE, e);
            }
        System.out.println("\nStarting new category maps file: " + CATEGORY_MAPS_FILE);
        return new HashMap<>();
    }

    private void writeCategoryMaps(Map<String, Map<String, String>> inCategoryMaps)
            throws IOException {
        System.out.print("Step 1 of 4: Writing category mappings... ");
        writeDocument(inCategoryMaps.keySet().stream()
                .map(category -> inCategoryMaps.get(category).entrySet().stream()
                        .map(map -> new Element("map")
                                .addContent(new Element("key").addContent(map.getKey()))
                                .addContent(new Element("value").addContent(map.getValue())))
                        .reduce(new Element("category").addContent(
                                new Element("name").addContent(category)), Element::addContent))
                .reduce(new Element("categories"), Element::addContent), CATEGORY_MAPS_FILE, null);
        System.out.println("DONE");
    }

    private Category[] buildCategories(String[] inCategoryNames,
            List<? extends List<String>> inCategoryNominees,
            Map<String, Map<String, String>> inCategoryMaps, List<String[]> inPlayerValues)
            throws IOException {
        return IntStream.range(0, inCategoryNames.length).mapToObj(categoryNum -> new Category(
                inCategoryNames[categoryNum],
                inCategoryNominees.get(categoryNum).stream().collect(Collectors.toMap(
                        Function.identity(),
                        nominee -> inPlayerValues.stream()
                                .map(guesses -> inCategoryMaps.get(inCategoryNames[categoryNum])
                                        .get(guesses[categoryNum]))
                                .filter(nominee::equals).count()))))
                .toArray(Category[]::new);
    }

    private List<Player> buildPlayers(List<String[]> inPlayerValues, Category[] inCategoryArray,
            String[] inCategoryNames, Map<String, Map<String, String>> inCategoryMaps) {
        return inPlayerValues.stream().map(playerValues -> new Player(IntStream
                .range(0, inCategoryArray.length).boxed()
                .collect(Collectors.toMap(categoryNum -> inCategoryArray[categoryNum],
                        categoryNum -> inCategoryMaps.get(inCategoryNames[categoryNum]).isEmpty()
                                ? playerValues[categoryNum]
                                : inCategoryMaps.get(inCategoryNames[categoryNum])
                                        .get(playerValues[categoryNum])))))
                .collect(Collectors.toList());
    }

    private void process() throws IOException, InterruptedException {
        writeRankCharts();
        writeCategoryPages();
        writePlayerPages();

        System.out.println();
        while (prompt())
            System.out.println();

        // In case it was interrupted
        System.out.print("\nWriting final results... ");
        writeResults();
        cleanUpCharts(Category.DIRECTORY,
                categories.stream().map(category -> category.chartName(results)));
        cleanUpCharts(RankChart.DIRECTORY, players.stream().mapToLong(Player::getRank)
                .mapToObj(RankChart::new).map(RankChart::chartName));
        System.out.println("DONE");
    }

    private boolean prompt() throws IOException, InterruptedException {
        Thread thread = new Thread(this);
        try {
            thread.start();
            return results.prompt(categories);
        } finally {
            // Stop file I/O thread and wait for it to finish
            thread.interrupt();
            thread.join();
        }
    }

    /**
     * Run in a separate thread: Process the results and wait until we need to update the times
     * again. Continue until the main thread kills this thread or the show ends.
     */
    @Override
    public void run() {
        try {
            for (long waitTime = 0; waitTime >= 0; waitTime = nextPlayerTime(10)) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(waitTime));
                writeResults();
            }
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void writeResults() throws IOException {
        runningTime = results.runningTime();
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(results.elapsedTimeMillis());
        players.parallelStream().forEach(player -> player.setScore(results));
        players.parallelStream()
                .forEach(player -> player.setRanks(results, players, runningTime, elapsedTime));
        writeDocument(resultsDOM(), Results.RESULTS_FILE, null);
    }

    private long nextPlayerTime(long inMaxTime) {
        return runningTime >= 0 ? -1
                : players.stream().mapToLong(player -> player.getTime(runningTime) - elapsedTime)
                        .filter(playerTime -> playerTime > 0 && playerTime < inMaxTime).min()
                        .orElse(inMaxTime);
    }

    private Element resultsDOM() {
        return new Element("results").addContent(new Element("title").addContent(results.title()))
                .addContent(categories.stream().map(this::resultsCategoryDOM)
                        .reduce(new Element("categories"), Element::addContent))
                .addContent(IntStream.range(0, players.size()).mapToObj(this::resultsPlayerDOM)
                        .reduce(new Element("players"), Element::addContent))
                .addContent(resultsShowTimeDOM())
                .addContent(new Element("refresh").addContent(String.valueOf(elapsedTime < 0 ? -1
                        : (nextPlayerTime(TimeUnit.MINUTES.toSeconds(5) - 5) + 5))))
                .addContent(new Element("updated").addContent(
                        new SimpleDateFormat("MM/dd/yyyy h:mm:ss a - z").format(new Date())));
    }

    private Element resultsCategoryDOM(Category inCategory) {
        return new Element("category").addContent(new Element("name").addContent(inCategory.name))
                .addContent(inCategory.guesses.keySet().stream().sorted()
                        .map(guess -> new Element("nominee").addContent(guess).setAttribute(
                                "status",
                                results.winners(inCategory).isEmpty() ? "unannounced"
                                        : results.winners(inCategory).contains(guess) ? "correct"
                                                : "incorrect"))
                        .reduce(new Element("nominees"), Element::addContent));
    }

    private Element resultsPlayerDOM(int playerNum) {
        Player player = players.get(playerNum);
        return player.toDOM()
                .addContent(new Element("rank").addContent(String.valueOf(player.getRank())))
                .addContent(
                        new Element("bpr").addContent(String.valueOf(player.getBestPossibleRank())))
                .addContent(new Element("wpr")
                        .addContent(String.valueOf(player.getWorstPossibleRank())))
                .addContent(
                        new Element("score").addContent(
                                String.format(scoreFormat, player.getScore())))
                .addContent(
                        new Element("time")
                                .setAttribute("status",
                                        player.getTime(runningTime) < 0
                                                || player.getTime(runningTime) > elapsedTime
                                                        ? "unannounced"
                                                        : "correct")
                                .addContent(formatTime(player.time)))
                .addContent(players.stream()
                        .map(opponent -> new Element("player").addContent(player.lostTo(opponent)
                                ? "BETTER"
                                : opponent.lostTo(player)
                                        || player.tiedWith(opponent, results, runningTime) ? "WORSE"
                                                : "TBD"))
                        .reduce(new Element("opponents"), Element::addContent))
                .setAttribute("id", String.valueOf(playerNum + 1));
    }

    private Element resultsShowTimeDOM() {
        String timeString = formatTime(
                runningTime >= 0 ? runningTime : elapsedTime >= 0 ? elapsedTime : 0);
        return Arrays.stream(ShowTimeType.values())
                .map(showTimeType -> new Element(showTimeType.name().toLowerCase())
                        .addContent(results.getShowTime(showTimeType)))
                .reduce(new Element("showTime")
                        .addContent(new Element("length").addContent(timeString))
                        .addContent(new Element("header")
                                .addContent("Time" + (runningTime >= 0 ? "=" : ">") + timeString)),
                        Element::addContent);
    }

    private String formatTime(long inTime) {
        return inTime < 0 ? ""
                : String.format("%d:%02d:%02d", TimeUnit.SECONDS.toHours(inTime),
                        TimeUnit.SECONDS.toMinutes(inTime) % 60, inTime % 60);
    }

    private void mkdir(String inDirectory) {
        File directory = new File(inDirectory);
        if (!directory.exists())
            directory.mkdir();
    }

    private void writeRankCharts() throws IOException {
        System.out.print("Step 2 of 4: Writing rank images... ");
        mkdir(RankChart.DIRECTORY);
        for (int rank = 1; rank <= players.size(); rank++)
            new RankChart(rank).writeChart(players.size());
        System.out.println("DONE");
    }

    private void writeCategoryPages() throws IOException {
        System.out.print("Step 3 of 4: Writing category web pages... ");
        mkdir(Category.DIRECTORY);
        writeDocument(
                categories.stream().map(category -> category.toDOM(players))
                        .reduce(new Element("categories"), Element::addContent),
                Category.DIRECTORY + "all.xml", "../xsl/categoryGraphs.xsl");
        for (Category category : categories) {
            category.writeChart(results);
            writeDocument(new Element("category").addContent(category.name),
                    Category.DIRECTORY + category.name + ".xml", "../xsl/category.xsl");
        }
        System.out.println("DONE");
    }

    private void writePlayerPages() throws IOException {
        System.out.print("Step 4 of 4: Writing player web pages... ");
        mkdir("player");
        for (Player player : players)
            writeDocument(player.toDOM(),
                    "player/" + Stream.of(player.firstName, player.lastName)
                            .filter(name -> !name.isEmpty()).collect(Collectors.joining(" "))
                            + ".xml",
                    "../xsl/player.xsl");
        System.out.println("DONE");
    }

    private void writeDocument(Element inElement, String inXMLFile, String inXSLFile)
            throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(inXMLFile), "UTF-8"))) {
            new XMLOutputter(Format.getPrettyFormat()).output(buildDocument(inElement, inXSLFile),
                    writer);
        }
    }

    private Document buildDocument(Element inElement, String inXSLFile) {
        return Optional.ofNullable(inXSLFile)
                .map(xslFile -> new Document().addContent(new ProcessingInstruction(
                        "xml-stylesheet",
                        Stream.of(new String[][] { { "type", "text/xsl" }, { "href", xslFile } })
                                .collect(Collectors.toMap(element -> element[0],
                                        element -> element[1])))))
                .orElseGet(Document::new).addContent(inElement);
    }

    private void cleanUpCharts(String inDirectory, Stream<String> inChartsToKeep)
            throws IOException {
        Set<String> chartsToKeep = inChartsToKeep.collect(Collectors.toSet());
        Files.list(Paths.get(inDirectory))
                .filter(file -> file.toString().endsWith(".png")
                        && !chartsToKeep.contains(file.getFileName().toString()))
                .map(Path::toFile).forEach(File::delete);
    }
}