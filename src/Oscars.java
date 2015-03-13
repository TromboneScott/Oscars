/** Oscars - Calculate the standings in the Oscars competition */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.output.XMLOutputter;

/**
 * This program will allow Oscars winners to be selected and a new result files will be generated.
 * The Oscars picks for each player are in a separate comma delimited file which won't change during
 * the contest. A separate file indicates the column names of that file and positionally represents
 * the nominees for each category. The column name can include a tie breaker number inside
 * parentheses like this: Director(1) to indicate that Director is the first tie breaker. The
 * contestants' names must be in the columns named "First" and "Last" and their time estimate must
 * be in the column named "Time" in the format "H:MM" or "H:MM:SS.D".
 * 
 * @author Scott McDonald
 * @version 4.0
 */
public class Oscars implements Runnable {
    private static final String PLAYERS_FILE = "players.csv";

    private static final String CATEGORIES_FILE = "categories.csv";

    private static final String VALUE_DELIMITER = ",";

    private final Collection<Player> players;

    private final List<Category> categories;

    private final Results results;

    private final String scoreFormat;

    private final List<Collection<Callable<Object>>> updatersList;

    private long runningTime;

    private long elapsedTime;

    /**
     * Prompt for Oscars results, store them and create output files
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] inArgs) throws IOException, InterruptedException {
        Oscars oscars = new Oscars();

        System.out.print("Writing web pages... ");
        oscars.writeCategoryPages();
        oscars.writePlayerPages();
        System.out.println("DONE\n");

        while (oscars.process())
            System.out.println();
        System.out.print("Writing final results... ");
        oscars.update();
        System.out.println("DONE");
    }

    private Oscars() throws IOException {
        System.out.print("Loading data... ");

        List<String[]> playerValues = readValues(PLAYERS_FILE);
        List<String[]> categoryValues = readValues(CATEGORIES_FILE);
        Category[] categoryArray = buildCategories(playerValues, categoryValues);
        players = Collections.unmodifiableCollection(buildPlayers(playerValues, categoryValues,
                categoryArray));
        categories = Collections.unmodifiableList(filterCategories(categoryArray));
        results = new Results(categories);
        scoreFormat = "%." + tieBreakerCount(categories) + "f";
        updatersList = Collections.unmodifiableList(updatersList());
        System.out.println("DONE\n");
    }

    private static List<String[]> readValues(String inFileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inFileName));
        List<String[]> lines = new ArrayList<String[]>();
        for (String line = reader.readLine(); line != null; line = reader.readLine())
            lines.add(line.split(VALUE_DELIMITER, -1));
        return lines;
    }

    private Category[] buildCategories(List<String[]> inPlayerValues,
            List<String[]> inCategoryValues) throws IOException {
        Category[] categoryArray = new Category[inCategoryValues.get(0).length];
        for (int categoryNum = 0; categoryNum < categoryArray.length; categoryNum++) {
            Map<String, Integer> guesses = new HashMap<String, Integer>(inCategoryValues.size());
            if (!inCategoryValues.get(1)[categoryNum].isEmpty()) {
                for (int guessNum = 1; guessNum < inCategoryValues.size()
                        && !inCategoryValues.get(guessNum)[categoryNum].isEmpty(); guessNum++)
                    guesses.put(inCategoryValues.get(guessNum)[categoryNum], 0);
                for (String[] aPlayerValues : inPlayerValues) {
                    String guess = inCategoryValues.get(Integer
                            .parseInt(aPlayerValues[categoryNum]))[categoryNum];
                    guesses.put(guess, guesses.get(guess) + 1);
                }
            }
            categoryArray[categoryNum] = new Category(inCategoryValues.get(0)[categoryNum], guesses);
        }
        return categoryArray;
    }

    private Collection<Player> buildPlayers(List<String[]> inPlayerValues,
            List<String[]> inCategoryValues, Category[] inCategoryArray) {
        Collection<Player> result = new ArrayList<Player>(inPlayerValues.size());
        for (String[] aPlayerValues : inPlayerValues) {
            HashMap<Category, String> pickMap = new HashMap<Category, String>(
                    inCategoryArray.length);
            for (int categoryNum = 0; categoryNum < inCategoryArray.length; categoryNum++)
                pickMap.put(
                        inCategoryArray[categoryNum],
                        inCategoryValues.get(1)[categoryNum].isEmpty() ? aPlayerValues[categoryNum]
                                : inCategoryValues.get(Integer.parseInt(aPlayerValues[categoryNum]))[categoryNum]);
            result.add(new Player(pickMap));
        }
        return result;
    }

    private ArrayList<Category> filterCategories(Category[] inCategoryArray) {
        ArrayList<Category> result = new ArrayList<Category>(inCategoryArray.length);
        for (Category category : inCategoryArray)
            if (!category.guesses.isEmpty())
                result.add(category);
        return result;
    }

    private int tieBreakerCount(Collection<Category> inCategories) {
        int tieBreakerCount = 0;
        for (Category category : inCategories)
            if (!category.tieBreakerValue.isEmpty())
                tieBreakerCount++;
        return tieBreakerCount;
    }

    private boolean process() throws IOException, InterruptedException {
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
        for (long waitTimeMillis = 0; waitTimeMillis >= 0; waitTimeMillis = waitTimeMillis())
            try {
                Thread.sleep(waitTimeMillis);
                update();
            } catch (InterruptedException e) {
                return;
            } catch (IOException e) {
                return;
            }
    }

    private long waitTimeMillis() {
        double nextTime = elapsedTime < 0 ? 0 : TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS
                .toMinutes(elapsedTime) + 1);
        for (Player player : players) {
            double playerTime = player.getTime(runningTime);
            if (playerTime > elapsedTime && playerTime >= 0 && playerTime < nextTime)
                nextTime = playerTime;
        }
        return runningTime < 0 ? Math.max(TimeUnit.SECONDS.toMillis((long) Math.ceil(nextTime))
                - results.elapsedTimeMillis(), 0) : -1;
    }

    private void update() throws InterruptedException, IOException {
        runningTime = results.runningTime();
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(results.elapsedTimeMillis());
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            for (Collection<Callable<Object>> updaters : updatersList)
                executor.invokeAll(updaters);
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
        writeResults();
    }

    private List<Collection<Callable<Object>>> updatersList() {
        List<Collection<Callable<Object>>> updaters = new ArrayList<Collection<Callable<Object>>>(2);
        updaters.add(playerUpdaters(true));
        updaters.add(playerUpdaters(false));
        return updaters;
    }

    private Collection<Callable<Object>> playerUpdaters(boolean inInitialize) {
        Collection<Callable<Object>> tasks = new ArrayList<Callable<Object>>(players.size()
                * (inInitialize ? 1 : 3));
        for (Player player : players)
            if (inInitialize)
                tasks.add(playerUpdater(player, 0));
            else
                for (int operationNum = 1; operationNum < 4; operationNum++)
                    tasks.add(playerUpdater(player, operationNum));
        return Collections.unmodifiableCollection(tasks);
    }

    private Callable<Object> playerUpdater(final Player inPlayer, final int inOperationNum) {
        return new Callable<Object>() {
            @Override
            public Object call() {
                if (inOperationNum == 0)
                    inPlayer.setScore(results);
                else if (inOperationNum == 1)
                    inPlayer.setRank(players, runningTime, elapsedTime);
                else if (inOperationNum == 2)
                    inPlayer.setBestPossibleRank(results, players, runningTime, elapsedTime);
                else if (inOperationNum == 3)
                    inPlayer.setWorstPossibleRank(results, players, runningTime, elapsedTime);
                else
                    throw new UnsupportedOperationException();
                return null;
            }
        };
    }

    private void writeResults() throws IOException {
        Element resultsDOM = new Element("results");
        resultsDOM.addContent(new Element("title").addContent(results.title()));

        Element categoriesDOM = new Element("categories");
        int announcedCount = 0;
        for (Category category : categories)
            if (!results.winners(category).isEmpty()) {
                announcedCount++;
                Element categoryDOM = new Element("category");
                categoryDOM.addContent(new Element("name").addContent(category.name));
                for (String winner : results.winners(category))
                    categoryDOM.addContent(new Element("winner").addContent(winner));
                categoriesDOM.addContent(categoryDOM);
            }
        categoriesDOM.addContent(new Element("count").addContent(String.valueOf(announcedCount)));
        resultsDOM.addContent(categoriesDOM);

        int realPlayerCount = 0;
        Element playersDOM = new Element("players");
        for (Player player : players) {
            Element playerDOM = new Element("player");
            if (player.isPseudo)
                playerDOM.setAttribute("type", "pseudo");
            else
                realPlayerCount++;
            playerDOM.addContent(new Element("firstName").addContent(player.firstName));
            playerDOM.addContent(new Element("lastName").addContent(player.lastName));
            playerDOM.addContent(new Element("rank").addContent(String.valueOf(player.getRank())));
            playerDOM.addContent(new Element("bpr").addContent(String.valueOf(player
                    .getBestPossibleRank())));
            playerDOM.addContent(new Element("wpr").addContent(String.valueOf(player
                    .getWorstPossibleRank())));
            playerDOM.addContent(new Element("score").addContent(String.format(scoreFormat,
                    player.getScore())));
            playerDOM.addContent(new Element("time")
                    .setAttribute(
                            "status",
                            player.getTime(runningTime) < 0
                                    || player.getTime(runningTime) > elapsedTime ? "unannounced"
                                    : "correct").addContent(player.timeString));
            playersDOM.addContent(playerDOM);
        }
        playersDOM.addContent(new Element("count").addContent(String.valueOf(realPlayerCount)));
        resultsDOM.addContent(playersDOM);

        Element showTimeDOM = new Element("showTime");
        for (ShowTimeType showTimeType : ShowTimeType.values())
            showTimeDOM.addContent(new Element(showTimeType.name().toLowerCase())
                    .addContent(results.getShowTime(showTimeType)));
        showTimeDOM.addContent(new Element("length")
                .addContent(runningTime >= 0 ? toTime(runningTime)
                        : elapsedTime >= 0 ? toTime(elapsedTime) : "0:00"));
        showTimeDOM.addContent(new Element("header").addContent(runningTime >= 0 ? "T="
                + toTime(runningTime) : elapsedTime >= 0 ? "T>" + toTime(elapsedTime) : "Time"));
        resultsDOM.addContent(showTimeDOM);

        resultsDOM.addContent(new Element("updated").addContent(new SimpleDateFormat(
                "MM/dd/yyyy h:mm:ss a - z").format(new Date())));

        writeDocument(resultsDOM, Results.RESULTS_FILE, null);
    }

    private String toTime(long inTime) {
        return String.format("%d:%02d", TimeUnit.SECONDS.toHours(inTime),
                TimeUnit.SECONDS.toMinutes(inTime) % 60);
    }

    private void writeCategoryPages() throws IOException {
        for (Category category : categories) {
            category.writeChart(results.winners(category));

            Element categoryDOM = new Element("category");
            categoryDOM.addContent(new Element("name").addContent(category.name));
            categoryDOM.addContent(new Element("tieBreaker").addContent(category.tieBreakerValue));
            categoryDOM.addContent(new Element("value").addContent(category.value.toString()));

            Element guessesDOM = new Element("guesses");
            List<String> guesses = new ArrayList<String>(category.guesses.keySet());
            Collections.sort(guesses);
            for (String guess : guesses) {
                Element guessDOM = new Element("guess");
                guessDOM.addContent(new Element("name").addContent(guess));
                guessDOM.addContent(new Element("count").addContent(String.valueOf(category.guesses
                        .get(guess))));
                guessesDOM.addContent(guessDOM);
            }
            categoryDOM.addContent(guessesDOM);

            Element playersDOM = new Element("players");
            for (Player player : players) {
                Element playerDOM = new Element("player");
                playerDOM.addContent(new Element("firstName").addContent(player.firstName));
                playerDOM.addContent(new Element("lastName").addContent(player.lastName));
                playerDOM.addContent(new Element("guess").addContent(player.picks.get(category)));
                playersDOM.addContent(playerDOM);
            }
            categoryDOM.addContent(playersDOM);

            writeDocument(categoryDOM, "category/" + category.name + ".xml", "../xsl/category.xsl");
        }
    }

    private void writePlayerPages() throws IOException {
        for (Player player : players) {
            Element playerDOM = new Element("player");
            playerDOM.addContent(new Element("firstName").addContent(player.firstName));
            playerDOM.addContent(new Element("lastName").addContent(player.lastName));

            Element categoriesDOM = new Element("categories");
            for (Category category : categories) {
                Element categoryDOM = new Element("category");
                categoryDOM.addContent(new Element("name").addContent(category.name));
                categoryDOM.addContent(new Element("guess").addContent(player.picks.get(category)));
                categoryDOM.addContent(new Element("tieBreaker")
                        .addContent(category.tieBreakerValue));
                categoryDOM.addContent(new Element("value").addContent(category.value.toString()));
                categoriesDOM.addContent(categoryDOM);
            }

            playerDOM.addContent(categoriesDOM);

            writeDocument(playerDOM, "player/" + player.firstName
                    + (player.firstName.isEmpty() || player.lastName.isEmpty() ? "" : " ")
                    + player.lastName + ".xml", "../xsl/player.xsl");
        }
    }

    private void writeDocument(Element inElement, String inXMLFile, String inXSLFile)
            throws IOException {
        Document document = new Document();
        if (inXSLFile != null) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("type", "text/xsl");
            data.put("href", inXSLFile);
            document.addContent(new ProcessingInstruction("xml-stylesheet", data));
        }
        document.addContent(inElement);

        PrintWriter writer = new PrintWriter(inXMLFile);
        new XMLOutputter().output(document, writer);
        writer.close();
    }
}