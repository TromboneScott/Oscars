/** Prompt and store Oscars results */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class Results {
    public static final String RESULTS_FILE = "results.xml";

    private static final String WINNER_DELIMITER = ",";

    private static final String TIME_FORMAT = "MM/dd/yyyy HH:mm:ss";

    private static final Set<String> EMPTY_STRING_SET = Collections.emptySet();

    private final Map<Category, Set<String>> winners = new HashMap<Category, Set<String>>();

    private final Map<ShowTimeType, Date> showTimes = new HashMap<ShowTimeType, Date>();

    public Results(Collection<Category> inCategories) throws IOException {
        File resultsFile = new File(RESULTS_FILE);
        if (resultsFile.exists())
            try {
                Element resultsDOM = new SAXBuilder().build(resultsFile).getRootElement();
                winners.putAll(winners(resultsDOM.getChild("categories"), toMap(inCategories)));
                showTimes.putAll(showTimes(resultsDOM.getChild("showTime")));
            } catch (JDOMException e) {
                throw new IOException("ERROR: Unable to read results file: " + RESULTS_FILE, e);
            }
        else
            System.out.println("\nStarting new results file: " + RESULTS_FILE);

        while (!showTimes.containsKey(ShowTimeType.START))
            promptTime(ShowTimeType.START);
    }

    private Map<String, Category> toMap(Collection<Category> inCategories) {
        Map<String, Category> categories = new HashMap<String, Category>(inCategories.size());
        for (Category category : inCategories)
            categories.put(category.name, category);
        return categories;
    }

    private Map<Category, Set<String>> winners(Element inCategoriesDOM,
            Map<String, Category> inCategories) {
        Map<Category, Set<String>> winners = new HashMap<Category, Set<String>>();
        for (Element categoryDOM : inCategoriesDOM.getChildren("category")) {
            Category category = inCategories.get(categoryDOM.getChildText("name"));
            Set<String> winnerSet = new HashSet<String>();
            for (Element winnerDOM : categoryDOM.getChildren("winner"))
                winnerSet.add(winnerDOM.getText());
            if (!winnerSet.isEmpty() && category != null)
                winners.put(category, Collections.unmodifiableSet(winnerSet));
        }
        return winners;
    }

    private Map<ShowTimeType, Date> showTimes(Element inShowTimeDOM) {
        Map<ShowTimeType, Date> showTimes = new HashMap<ShowTimeType, Date>();
        for (ShowTimeType showTimeType : ShowTimeType.values()) {
            String showTimeText = inShowTimeDOM.getChildText(showTimeType.name().toLowerCase());
            if (showTimeText != null && !showTimeText.isEmpty())
                try {
                    showTimes.put(showTimeType,
                            new SimpleDateFormat(TIME_FORMAT).parse(showTimeText));
                } catch (ParseException e) {
                    System.out.println("WARNING: Invalid " + showTimeType + " value");
                }
        }
        return showTimes;
    }

    private boolean promptTime(ShowTimeType inShowTimeType) throws IOException {
        System.out.println("\n" + showTimeString(inShowTimeType));
        System.out.println("Enter in this format (leave blank to remove): " + format(new Date()));
        String enteredTime = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (enteredTime.isEmpty())
            if (inShowTimeType == ShowTimeType.START)
                System.out.println(ShowTimeType.START + " is required");
            else
                showTimes.remove(inShowTimeType);
        else
            try {
                showTimes.put(inShowTimeType, new SimpleDateFormat(TIME_FORMAT).parse(enteredTime));
            } catch (ParseException e) {
                System.out.println("Invalid time format");
            }
        return true;
    }

    private String showTimeString(ShowTimeType inShowTimeType) {
        return inShowTimeType
                + (showTimes.containsKey(inShowTimeType) ? " = "
                        + format(showTimes.get(inShowTimeType)) : "");
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
        for (int resultNum = 0; resultNum < inCategories.size(); resultNum++)
            System.out
                    .println((resultNum + 1) + ": " + categoryString(inCategories.get(resultNum)));
        for (int timeNum = 0; timeNum < ShowTimeType.values().length; timeNum++)
            System.out.println((inCategories.size() + timeNum + 1) + ": "
                    + showTimeString(ShowTimeType.values()[timeNum]));

        System.out.print("Enter results number to change (enter to quit): ");
        String selectedResult = stdin.readLine();
        if (selectedResult.isEmpty())
            return false;
        try {
            int resultNum = Integer.parseInt(selectedResult);
            if (resultNum < 1 || resultNum > inCategories.size() + ShowTimeType.values().length)
                throw new NumberFormatException();
            return resultNum > inCategories.size() ? promptTime(ShowTimeType.values()[resultNum
                    - inCategories.size() - 1]) : promptWinner(inCategories.get(resultNum - 1));
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection");
            return true;
        }
    }

    private boolean promptWinner(Category inCategory) throws IOException {
        System.out.println("\n" + categoryString(inCategory));

        Set<String> pickNamesSet = new TreeSet<String>(inCategory.guesses.keySet());
        String[] pickNames = pickNamesSet.toArray(new String[pickNamesSet.size()]);
        for (int x = 0; x < pickNames.length; x++)
            System.out.println(x + 1 + ": " + pickNames[x]);

        System.out.print("Select winner number(s) (use " + WINNER_DELIMITER
                + " to separate ties or leave blank to remove winner): ");
        String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (input.isEmpty())
            winners.remove(inCategory);
        else {
            Set<String> winnerSet = new HashSet<String>();
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
        inCategory.writeChart(winners(inCategory));
        return true;
    }

    private String categoryString(Category inCategory) {
        return inCategory
                + (winners.containsKey(inCategory) ? " = "
                        + join(winners.get(inCategory), WINNER_DELIMITER) : "");
    }

    /** Join the Iterable elements into a String with the given delimiter (reverse of String.split) */
    private String join(Iterable<? extends Object> inIterable, String inDelimiter) {
        StringBuffer result = new StringBuffer();
        Iterator<? extends Object> iter = inIterable.iterator();
        if (iter.hasNext())
            result.append(iter.next());
        while (iter.hasNext())
            result.append(inDelimiter).append(iter.next());
        return result.toString();
    }

    public String getShowTime(ShowTimeType inShowTimeType) {
        return showTimes.containsKey(inShowTimeType) ? format(showTimes.get(inShowTimeType)) : "";
    }

    /**
     * The actual running time of the broadcast in seconds if it's ended
     * 
     * @return The running time in seconds or -1 if it's not ended yet
     */
    public long runningTime() {
        return showTimes.containsKey(ShowTimeType.END) ? TimeUnit.MILLISECONDS.toSeconds(showTimes
                .get(ShowTimeType.END).getTime() - showTimes.get(ShowTimeType.START).getTime())
                : -1;
    }

    /**
     * The elapsed time in milliseconds since the start of the broadcast in seconds
     * 
     * @return The elapsed time, negative if the show hasn't started
     */
    public long elapsedTimeMillis() {
        return System.currentTimeMillis() - showTimes.get(ShowTimeType.START).getTime();
    }

    private String format(Date inTime) {
        return new SimpleDateFormat(TIME_FORMAT).format(inTime);
    }

    /**
     * Get the winner(s) of the given category
     * 
     * @param inCategory
     *            The category to get the winner(s) for
     * @return All the winners that have been entered for this category
     */
    public Set<String> winners(Category inCategory) {
        return winners.containsKey(inCategory) ? winners.get(inCategory) : EMPTY_STRING_SET;
    }

    /**
     * The title text to use for the results
     * 
     * @return The title text to use for the results
     */
    public String title() {
        Calendar calStartTime = Calendar.getInstance();
        calStartTime.setTime(showTimes.get(ShowTimeType.START));
        return calStartTime.get(Calendar.YEAR) + " OSCARS";
    }
}