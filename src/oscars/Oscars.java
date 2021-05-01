package oscars;

/** Oscars - Calculate the standings in the Oscars competition */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

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
        System.out.print("Writing category mappings... ");
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
                                .filter(guess -> nominee.equals(guess)).count()))))
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
        cleanUpCharts("category", categories.stream().map(category -> category.chartName(results)));
        cleanUpCharts("rank", players.stream().map(player -> "rank_" + player.getRank() + ".png"));
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
            for (long waitTimeMillis = 0; waitTimeMillis >= 0; waitTimeMillis = waitTimeMillis()) {
                Thread.sleep(waitTimeMillis);
                writeResults();
            }
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private long waitTimeMillis() {
        final long nextTime = elapsedTime < 0 ? 0
                : TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(elapsedTime) + 1);
        return runningTime < 0 ? Math.max(TimeUnit.SECONDS.toMillis(players.stream()
                .map(player -> player.getTime(runningTime))
                .filter(playerTime -> playerTime > elapsedTime && playerTime >= 0
                        && playerTime < nextTime)
                .mapToLong(Long::valueOf).min().orElse(nextTime)) - results.elapsedTimeMillis(), 0)
                : -1;
    }

    private void writeResults() throws IOException {
        runningTime = results.runningTime();
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(results.elapsedTimeMillis());
        players.parallelStream().forEach(player -> player.setScore(results));
        players.parallelStream()
                .forEach(player -> player.setRanks(results, players, runningTime, elapsedTime));
        writeDocument(resultsDOM(), Results.RESULTS_FILE, null);
    }

    private Element resultsDOM() {
        Element resultsDOM = new Element("results");
        resultsDOM.addContent(new Element("title").addContent(results.title()));
        resultsDOM.addContent(resultsCategoriesDOM());
        resultsDOM.addContent(resultsPlayersDOM());
        resultsDOM.addContent(resultsShowTimeDOM());
        resultsDOM.addContent(new Element("updated")
                .addContent(new SimpleDateFormat("MM/dd/yyyy h:mm:ss a - z").format(new Date())));
        return resultsDOM;
    }

    private Element resultsCategoriesDOM() {
        return categories.stream().map(category -> resultsCategoryDOM(category))
                .reduce(new Element("categories"), Element::addContent)
                .addContent(new Element("points").addContent(
                        categories.stream().filter(category -> !results.winners(category).isEmpty())
                                .map(category -> category.value)
                                .reduce(BigDecimal.ZERO, BigDecimal::add).toString()));
    }

    private Element resultsCategoryDOM(Category inCategory) {
        return results.winners(inCategory).stream()
                .map(winner -> new Element("winner").addContent(winner))
                .reduce(new Element("category").addContent(
                        new Element("name").addContent(inCategory.name)), Element::addContent)
                .addContent(new Element("chart").addContent(inCategory.chartName(results)))
                .addContent(new Element("correct").addContent(Optional
                        .of(results.winners(inCategory)).filter(winners -> !winners.isEmpty())
                        .map(winners -> String.valueOf(winners.stream()
                                .mapToLong(winner -> inCategory.guesses.get(winner)).sum()))
                        .orElse(null)));
    }

    private Element resultsPlayersDOM() {
        return IntStream.range(0, players.size()).boxed()
                .map(playerNum -> resultsPlayerDOM(playerNum))
                .reduce(new Element("players"), Element::addContent)
                .addContent(new Element("count").addContent(String.valueOf(players.size())));
    }

    private Element resultsPlayerDOM(int playerNum) {
        Player player = players.get(playerNum);
        Element playerDOM = player.toCoreDOM();
        playerDOM.addContent(new Element("rank").addContent(String.valueOf(player.getRank())));
        playerDOM.addContent(
                new Element("bpr").addContent(String.valueOf(player.getBestPossibleRank())));
        playerDOM.addContent(
                new Element("wpr").addContent(String.valueOf(player.getWorstPossibleRank())));
        playerDOM.addContent(
                new Element("score").addContent(String.format(scoreFormat, player.getScore())));
        playerDOM
                .addContent(
                        new Element("time")
                                .setAttribute("status",
                                        player.getTime(runningTime) < 0
                                                || player.getTime(runningTime) > elapsedTime
                                                        ? "unannounced"
                                                        : "correct")
                                .addContent(formatTime(player.time)));
        playerDOM.addContent(players.stream().map(
                opponent -> new Element("player").addContent(player.isWorseThan(opponent) ? "BETTER"
                        : player.isBetterThan(opponent) ? "WORSE" : "TBD"))
                .reduce(new Element("opponents"), Element::addContent));
        playerDOM.setAttribute("id", String.valueOf(playerNum + 1));
        return playerDOM;
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
        System.out.print("Writing rank images... ");
        mkdir("rank");
        for (int rank = 1; rank <= players.size(); rank++) {
            DefaultCategoryDataset data = new DefaultCategoryDataset();
            data.addValue(rank, "A", "");
            data.addValue(players.size(), "B", "");

            JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, data);
            chart.removeLegend();

            CategoryPlot plot = chart.getCategoryPlot();
            plot.getRangeAxis().setRange(1, players.size());
            plot.getRangeAxis().setInverted(true);
            plot.getRenderer().setSeriesPaint(0, Category.BAR_GRAY);
            plot.getRenderer().setSeriesPaint(1, Category.BAR_GREEN);

            ChartUtilities.saveChartAsPNG(new File("rank/rank_" + rank + ".png"), chart, 80, 180);
        }
        System.out.println("DONE");
    }

    private void writeCategoryPages() throws IOException {
        System.out.print("Writing category web pages... ");
        mkdir("category");
        writeDocument(
                categories.stream().map(category -> category.toCoreDOM())
                        .reduce(new Element("categories"), Element::addContent),
                "category/all.xml", "../xsl/categoryGraphs.xsl");
        for (Category category : categories) {
            category.writeChart(results);
            writeDocument(category.toDOM(players), "category/" + category.name + ".xml",
                    "../xsl/category.xsl");
        }
        System.out.println("DONE");
    }

    private void writePlayerPages() throws IOException {
        System.out.print("Writing player web pages... ");
        mkdir("player");
        for (Player player : players)
            writeDocument(player.toDOM(categories),
                    "player/" + player.firstName
                            + (player.firstName.isEmpty() || player.lastName.isEmpty() ? "" : " ")
                            + player.lastName + ".xml",
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

    private void cleanUpCharts(String inDirectory, Stream<String> inExpectedFiles)
            throws IOException {
        Set<String> expectedFiles = inExpectedFiles.collect(Collectors.toSet());
        Files.list(Paths.get(inDirectory))
                .filter(file -> file.toString().endsWith(".png")
                        && !expectedFiles.contains(file.getFileName().toString()))
                .map(Path::toFile).forEach(File::delete);
    }
}