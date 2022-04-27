package oscars;

/** Oscars - Calculate the standings in the Oscars competition */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Comment;
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

    private static final String CATEGORIES_FILE = "categories.csv";

    private static final String VALUE_DELIMITER = ",";

    private static final String COMMA_REPLACEMENT = "`";

    private final List<Player> players;

    private final List<Category> categories;

    private final Results results;

    private final String scoreFormat;

    private Standings standings;

    private long validTimes = 0;

    private LocalDateTime updated;

    /**
     * Prompt for Oscars results, store them and create output files
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] inArgs) throws Exception {
        new Oscars(inArgs).process();
    }

    public static void validateArgs(String[] inArgs) {
        if (inArgs.length != 1)
            throw new IllegalArgumentException("Missing paramter: URL");
    }

    private Oscars(String[] inArgs) throws Exception {
        Oscars.validateArgs(inArgs);
        System.out.print("Step 1 of 5: Loading data... ");
        Collection<Ballot> ballots = Ballot.stream(new URL(inArgs[0])).collect(Ballot.LATEST);
        List<String[]> categoryValues = readValues(CATEGORIES_FILE);
        System.out.println("DONE");

        List<? extends List<String>> categoryNominees = categoryNominees(categoryValues);
        Map<String, Map<String, String>> categoryMaps = categoryMaps(categoryValues, ballots,
                categoryNominees);
        Category[] categoryArray = buildCategories(categoryValues.get(0), categoryNominees,
                categoryMaps, ballots);
        players = Collections.unmodifiableList(
                buildPlayers(ballots, categoryArray, categoryValues.get(0), categoryMaps));
        categories = Collections.unmodifiableList(Arrays.stream(categoryArray)
                .filter(category -> !category.guesses.isEmpty()).collect(Collectors.toList()));
        results = new Results(categories);
        scoreFormat = "%." + categories.stream()
                .filter(category -> !category.tieBreakerValue.isEmpty()).count() + "f";
    }

    private static List<String[]> readValues(String inFileName) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(inFileName),
                StandardCharsets.ISO_8859_1)) {
            return stream.map(line -> Stream.of(line.split(VALUE_DELIMITER, -1))
                    .map(value -> value.replace(COMMA_REPLACEMENT, VALUE_DELIMITER))
                    .toArray(String[]::new)).collect(Collectors.toList());
        }
    }

    private Map<String, Map<String, String>> categoryMaps(List<String[]> inCategoryValues,
            Collection<Ballot> ballots, List<? extends List<String>> inCategoryNominees)
            throws IOException {
        Map<String, Map<String, String>> categoryMaps = readCategoryMaps();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        for (int categoryNum = 0; categoryNum < inCategoryValues.get(0).length; categoryNum++) {
            String categoryName = inCategoryValues.get(0)[categoryNum];
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(categoryName,
                    k -> new HashMap<>());
            List<String> nominees = inCategoryNominees.get(categoryNum);
            if (!nominees.isEmpty())
                for (Ballot ballot : ballots)
                    if (!categoryMap.containsKey(ballot.get(categoryNum))) {
                        List<String> mappings = nominees.stream()
                                .filter(ballot.get(categoryNum)::contains)
                                .collect(Collectors.toList());
                        if (mappings.size() == 1)
                            categoryMap.put(ballot.get(categoryNum), mappings.get(0));
                        else {
                            System.out.println("\nCATEGORY: " + categoryName);
                            IntStream.range(0, nominees.size()).forEach(nomineeNum -> System.out
                                    .println((nomineeNum + 1) + ": " + nominees.get(nomineeNum)));
                            System.out.print(ballot.get(categoryNum) + " = ");
                            String guessNum = stdin.readLine();
                            categoryMap.put(ballot.get(categoryNum),
                                    nominees.get(Integer.parseInt(guessNum) - 1));
                        }
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
        System.out.print("Step 2 of 5: Writing category mappings... ");
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
            Map<String, Map<String, String>> inCategoryMaps, Collection<Ballot> ballots)
            throws IOException {
        return IntStream.range(0, inCategoryNames.length).mapToObj(categoryNum -> new Category(
                inCategoryNames[categoryNum],
                inCategoryNominees.get(categoryNum).stream()
                        .collect(Collectors.toMap(nominee -> nominee, nominee -> ballots.stream()
                                .map(ballot -> inCategoryMaps.get(inCategoryNames[categoryNum])
                                        .get(ballot.get(categoryNum)))
                                .filter(nominee::equals).count())),
                inCategoryMaps.get(inCategoryNames[categoryNum]).entrySet().stream()
                        .collect(Collectors.toMap(Entry::getValue, Entry::getKey))))
                .toArray(Category[]::new);
    }

    private List<Player> buildPlayers(Collection<Ballot> ballots, Category[] inCategoryArray,
            String[] inCategoryNames, Map<String, Map<String, String>> inCategoryMaps) {
        return ballots.stream().map(ballot -> new Player(IntStream.range(0, inCategoryArray.length)
                .boxed()
                .collect(Collectors.toMap(categoryNum -> inCategoryArray[categoryNum],
                        categoryNum -> inCategoryMaps.get(inCategoryNames[categoryNum]).isEmpty()
                                ? ballot.get(categoryNum)
                                : inCategoryMaps.get(inCategoryNames[categoryNum])
                                        .get(ballot.get(categoryNum))))))
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
        cleanUpCharts(RankChart.DIRECTORY,
                standings.rankStream().mapToObj(RankChart::new).map(RankChart::chartName));
        System.out.println("DONE");
    }

    private boolean prompt() throws IOException, InterruptedException {
        updated = LocalDateTime.now();
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
            do {
                writeResults();
                Thread.sleep(waitTime(TimeUnit.SECONDS.toMillis(60)));
            } while (!standings.showEnded());
        } catch (InterruptedException e) {
            // Ignore
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private long waitTime(long inMaxWait) {
        long nextPlayerTime = TimeUnit.SECONDS
                .toMillis(players.stream().mapToLong(player -> player.time)
                        .filter(playerTime -> playerTime > standings.elapsedTime).min()
                        .orElseGet(() -> TimeUnit.MILLISECONDS.toSeconds(Long.MAX_VALUE)));
        long elapsedTimeMillis = results.elapsedTimeMillis();
        return Math.min(Math.max(nextPlayerTime - elapsedTimeMillis, 0),
                inMaxWait - elapsedTimeMillis % inMaxWait);
    }

    private void writeResults() throws IOException {
        standings = new Standings(players, results);
        long currentTimes = players.stream().filter(player -> player.time <= standings.elapsedTime)
                .count();
        if (validTimes != currentTimes)
            updated = LocalDateTime.now();
        validTimes = currentTimes;
        Results.write(updated, standings.resultsCategoryDOM(categories),
                standings.resultsPlayerDOM(players, scoreFormat), standings.resultsShowTimeDOM());
    }

    private void mkdir(String inDirectory) {
        File directory = new File(inDirectory);
        if (!directory.exists())
            directory.mkdir();
    }

    private void writeRankCharts() throws IOException {
        System.out.print("Step 3 of 5: Writing " + players.size() + " rank images... ");
        mkdir(RankChart.DIRECTORY);
        for (int rank = 1; rank <= players.size(); rank++) {
            System.out.print(rank + "-");
            new RankChart(rank).writeChart(players.size());
        }
        System.out.println("DONE");
    }

    private void writeCategoryPages() throws IOException {
        System.out.print("Step 4 of 5: Writing category web pages... ");
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
        System.out.print("Step 5 of 5: Writing player web pages... ");
        mkdir("player");
        for (Player player : players)
            writeDocument(player.toDOM(),
                    "player/" + Stream.of(player.firstName, player.lastName)
                            .filter(name -> !name.isEmpty()).collect(Collectors.joining(" "))
                            + ".xml",
                    "../xsl/player.xsl");
        System.out.println("DONE");
    }

    public static void writeDocument(Element inElement, String inXMLFile, String inXSLFile)
            throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(inXMLFile), "UTF-8"))) {
            new XMLOutputter(Format.getPrettyFormat()).output(xmlDocument(inXSLFile)
                    .addContent(new Comment("OSCARS website created by Scott McDonald"))
                    .addContent(inElement), writer);
        }
    }

    private static Document xmlDocument(String inXSLFile) {
        return inXSLFile == null ? new Document()
                : new Document().addContent(new ProcessingInstruction("xml-stylesheet", Stream
                        .of(new String[][] { { "type", "text/xsl" }, { "href", inXSLFile } })
                        .collect(Collectors.toMap(element -> element[0], element -> element[1]))));
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