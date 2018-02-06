package oscars;

/** Oscars - Calculate the standings in the Oscars competition */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * This program will allow Oscars winners to be selected and a new results file
 * will be generated. The Oscars picks for each player are in a comma delimited
 * file which won't change during the contest. A separate file indicates the
 * column names of that file and all nominees for each category the way they
 * should be displayed on the web site. The column name can include a tie
 * breaker number inside parentheses like this: Director(1) to indicate that
 * Director is the first tie breaker. The contestants' names must be in the
 * columns named "First" and "Last" and their time estimate must be in the
 * column named "Time" in the format "H:MM" or "H:MM:SS.D". A pseudo player can
 * be entered by putting PSEUDO- in front of their first name.
 * 
 * @author Scott McDonald
 * @version 4.1
 */
public class Oscars implements Runnable {
	private static final String CATEGORY_MAPS_FILE = "categoryMaps.xml";

	private static final String PLAYERS_FILE = "players.csv";

	private static final String CATEGORIES_FILE = "categories.csv";

	private static final String VALUE_DELIMITER = ",";

	private final Collection<Player> players;

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
		Oscars oscars = new Oscars();
		oscars.writeCategoryPages();
		oscars.writePlayerPages();

		System.out.println();
		while (oscars.process())
			System.out.println();

		// In case it was interrupted
		System.out.print("\nWriting final results... ");
		oscars.writeCorrectChart();
		oscars.writeResults();
		System.out.println("DONE");
	}

	private Oscars() throws IOException {
		System.out.print("Loading data... ");
		List<String[]> playerValues = readValues(PLAYERS_FILE);
		List<String[]> categoryValues = readValues(CATEGORIES_FILE);
		System.out.println("DONE");

		ArrayList<ArrayList<String>> categoryNominees = categoryNominees(categoryValues);
		Map<String, Map<String, String>> categoryMaps = categoryMaps(categoryValues, playerValues, categoryNominees);
		Category[] categoryArray = buildCategories(categoryValues.get(0), categoryNominees, categoryMaps, playerValues);
		players = Collections
				.unmodifiableCollection(buildPlayers(playerValues, categoryArray, categoryValues.get(0), categoryMaps));
		categories = Collections.unmodifiableList(filterCategories(categoryArray));
		results = new Results(categories);
		scoreFormat = "%." + tieBreakerCount(categories) + "f";
	}

	private static List<String[]> readValues(String inFileName) throws IOException {
		try (Stream<String> stream = Files.lines(Paths.get(inFileName))) {
			return stream.map(line -> line.split(VALUE_DELIMITER, -1)).collect(Collectors.toList());
		}
	}

	private Map<String, Map<String, String>> categoryMaps(List<String[]> inCategoryValues,
			List<String[]> inPlayerValues, ArrayList<ArrayList<String>> inCategoryNominees) throws IOException {
		Map<String, Map<String, String>> categoryMaps = readCategoryMaps();
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		for (int categoryNum = 0; categoryNum < inCategoryValues.get(0).length; categoryNum++) {
			String categoryName = inCategoryValues.get(0)[categoryNum];
			Map<String, String> categoryMap = categoryMaps.get(categoryName);
			if (categoryMap == null) {
				categoryMap = new HashMap<String, String>();
				categoryMaps.put(categoryName, categoryMap);
			}
			if (!inCategoryNominees.get(categoryNum).isEmpty()) {
				for (String[] guesses : inPlayerValues)
					if (!categoryMap.containsKey(guesses[categoryNum])) {
						System.out.println("\nCATEGORY: " + categoryName);
						for (int nomineeNum = 0; nomineeNum < inCategoryNominees.get(categoryNum).size(); nomineeNum++)
							System.out.println(
									(nomineeNum + 1) + ": " + inCategoryNominees.get(categoryNum).get(nomineeNum));
						System.out.print(guesses[categoryNum] + " = ");
						String guessNum = stdin.readLine();
						categoryMap.put(guesses[categoryNum],
								inCategoryNominees.get(categoryNum).get(Integer.parseInt(guessNum) - 1));
					}
			}
		}
		writeCategoryMaps(categoryMaps);
		return categoryMaps;
	}

	private ArrayList<ArrayList<String>> categoryNominees(List<String[]> inCategoryValues) {
		int categoryCount = inCategoryValues.get(0).length;
		ArrayList<ArrayList<String>> categoryNominees = new ArrayList<ArrayList<String>>(categoryCount);
		for (int categoryNum = 0; categoryNum < categoryCount; categoryNum++) {
			ArrayList<String> guesses = new ArrayList<String>(inCategoryValues.size());
			for (int guessNum = 1; guessNum < inCategoryValues.size(); guessNum++)
				if (!inCategoryValues.get(guessNum)[categoryNum].isEmpty())
					guesses.add(inCategoryValues.get(guessNum)[categoryNum]);
			categoryNominees.add(guesses);
		}
		return categoryNominees;
	}

	private Map<String, Map<String, String>> readCategoryMaps() throws IOException {
		Map<String, Map<String, String>> categoryMaps = new HashMap<String, Map<String, String>>();
		File categoryMapsFile = new File(CATEGORY_MAPS_FILE);
		if (categoryMapsFile.exists())
			try {
				Element categoriesDOM = new SAXBuilder().build(categoryMapsFile).getRootElement();
				for (Element categoryDOM : categoriesDOM.getChildren("category")) {
					Map<String, String> map = new HashMap<String, String>();
					for (Element mapDOM : categoryDOM.getChildren("map"))
						map.put(mapDOM.getChildText("key"), mapDOM.getChildText("value"));
					categoryMaps.put(categoryDOM.getChildText("name"), map);
				}
			} catch (JDOMException e) {
				throw new IOException("ERROR: Unable to read category maps file: " + CATEGORY_MAPS_FILE, e);
			}
		else
			System.out.println("\nStarting new category maps file: " + CATEGORY_MAPS_FILE);
		return categoryMaps;
	}

	private void writeCategoryMaps(Map<String, Map<String, String>> inCategoryMaps) throws IOException {
		System.out.print("Writing category mappings... ");
		Element categoriesDOM = new Element("categories");
		for (String category : inCategoryMaps.keySet()) {
			Element categoryDOM = new Element("category");
			categoryDOM.addContent(new Element("name").addContent(category));
			for (Entry<String, String> map : inCategoryMaps.get(category).entrySet()) {
				Element mapDOM = new Element("map");
				mapDOM.addContent(new Element("key").addContent(map.getKey()));
				mapDOM.addContent(new Element("value").addContent(map.getValue()));
				categoryDOM.addContent(mapDOM);
			}
			categoriesDOM.addContent(categoryDOM);
		}
		writeDocument(categoriesDOM, CATEGORY_MAPS_FILE, null);
		System.out.println("DONE");
	}

	private Category[] buildCategories(String[] inCategoryNames, ArrayList<ArrayList<String>> inCategoryNominees,
			Map<String, Map<String, String>> inCategoryMaps, List<String[]> inPlayerValues) throws IOException {
		Category[] categoryArray = new Category[inCategoryNames.length];
		for (int categoryNum = 0; categoryNum < categoryArray.length; categoryNum++) {
			Map<String, Integer> guesses = new HashMap<String, Integer>();
			ArrayList<String> allNominees = inCategoryNominees.get(categoryNum);
			if (!allNominees.isEmpty()) {
				for (String nominee : allNominees)
					guesses.put(nominee, 0);
				for (String[] aPlayerValues : inPlayerValues) {
					String guess = inCategoryMaps.get(inCategoryNames[categoryNum]).get(aPlayerValues[categoryNum]);
					guesses.put(guess, guesses.get(guess) + 1);
				}
			}
			categoryArray[categoryNum] = new Category(inCategoryNames[categoryNum], guesses);
		}
		return categoryArray;
	}

	private Collection<Player> buildPlayers(List<String[]> inPlayerValues, Category[] inCategoryArray,
			String[] inCategoryNames, Map<String, Map<String, String>> inCategoryMaps) {
		Collection<Player> result = new ArrayList<Player>(inPlayerValues.size());
		for (String[] aPlayerValues : inPlayerValues) {
			HashMap<Category, String> pickMap = new HashMap<Category, String>(inCategoryArray.length);
			for (int categoryNum = 0; categoryNum < inCategoryArray.length; categoryNum++)
				pickMap.put(inCategoryArray[categoryNum],
						inCategoryMaps.get(inCategoryNames[categoryNum]).isEmpty() ? aPlayerValues[categoryNum]
								: inCategoryMaps.get(inCategoryNames[categoryNum]).get(aPlayerValues[categoryNum]));
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
	 * Run in a separate thread: Process the results and wait until we need to
	 * update the times again. Continue until the main thread kills this thread or
	 * the show ends.
	 */
	@Override
	public void run() {
		try {
			writeCorrectChart();
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
		double nextTime = elapsedTime < 0 ? 0 : TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(elapsedTime) + 1);
		for (Player player : players) {
			double playerTime = player.getTime(runningTime);
			if (playerTime > elapsedTime && playerTime >= 0 && playerTime < nextTime)
				nextTime = playerTime;
		}
		return runningTime < 0
				? Math.max(TimeUnit.SECONDS.toMillis((long) Math.ceil(nextTime)) - results.elapsedTimeMillis(), 0)
				: -1;
	}

	private void writeResults() throws IOException {
		runningTime = results.runningTime();
		elapsedTime = TimeUnit.MILLISECONDS.toSeconds(results.elapsedTimeMillis());
		players.parallelStream().forEach(player -> player.setScore(results));
		players.parallelStream().forEach(player -> player.setRanks(results, players, runningTime, elapsedTime));
		writeDocument(resultsDOM(), Results.RESULTS_FILE, null);
	}

	private Element resultsDOM() {
		Element resultsDOM = new Element("results");
		resultsDOM.addContent(new Element("title").addContent(results.title()));
		resultsDOM.addContent(resultsCategoriesDOM());
		resultsDOM.addContent(resultsPlayersDOM());
		resultsDOM.addContent(resultsShowTimeDOM());
		resultsDOM.addContent(
				new Element("updated").addContent(new SimpleDateFormat("MM/dd/yyyy h:mm:ss a - z").format(new Date())));
		return resultsDOM;
	}

	private Element resultsCategoriesDOM() {
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
		return categoriesDOM;
	}

	private Element resultsPlayersDOM() {
		int realPlayerCount = 0;
		Element playersDOM = new Element("players");
		for (Player player : players) {
			Element playerDOM = player.toCoreDOM();
			playerDOM.addContent(new Element("rank").addContent(String.valueOf(player.getRank())));
			playerDOM.addContent(new Element("bpr").addContent(String.valueOf(player.getBestPossibleRank())));
			playerDOM.addContent(new Element("wpr").addContent(String.valueOf(player.getWorstPossibleRank())));
			playerDOM.addContent(new Element("score").addContent(String.format(scoreFormat, player.getScore())));
			playerDOM.addContent(new Element("time").setAttribute("status",
					player.getTime(runningTime) < 0 || player.getTime(runningTime) > elapsedTime ? "unannounced"
							: "correct")
					.addContent(formatTime(player.time)));
			if (player.isPseudo)
				playerDOM.setAttribute("type", "pseudo");
			else
				realPlayerCount++;
			playersDOM.addContent(playerDOM);
		}
		playersDOM.addContent(new Element("count").addContent(String.valueOf(realPlayerCount)));
		return playersDOM;
	}

	private Element resultsShowTimeDOM() {
		Element showTimeDOM = new Element("showTime");
		for (ShowTimeType showTimeType : ShowTimeType.values())
			showTimeDOM.addContent(
					new Element(showTimeType.name().toLowerCase()).addContent(results.getShowTime(showTimeType)));
		String timeString = formatTime(runningTime >= 0 ? runningTime : elapsedTime >= 0 ? elapsedTime : 0);
		showTimeDOM.addContent(new Element("length").addContent(timeString));
		showTimeDOM.addContent(new Element("header").addContent("Time" + (runningTime >= 0 ? "=" : ">") + timeString));
		return showTimeDOM;
	}

	private String formatTime(long inTime) {
		return inTime < 0 ? ""
				: String.format("%d:%02d:%02d", TimeUnit.SECONDS.toHours(inTime),
						TimeUnit.SECONDS.toMinutes(inTime) % 60, inTime % 60);
	}

	private void writeCategoryPages() throws IOException {
		System.out.print("Writing category web pages... ");
		writeAllCategoryPage();
		for (Category category : categories) {
			category.writeChart(results.winners(category));
			writeDocument(category.toDOM(players), "category/" + category.name + ".xml", "../xsl/category.xsl");
		}
		System.out.println("DONE");
	}

	private void writeAllCategoryPage() throws IOException {
		Element categoriesDOM = new Element("categories");
		for (Category category : categories)
			categoriesDOM.addContent(category.toDOM(players));
		writeDocument(categoriesDOM, "category/all.xml", "../xsl/categoryGraphs.xsl");
	}

	private void writePlayerPages() throws IOException {
		System.out.print("Writing player web pages... ");
		for (Player player : players)
			writeDocument(player.toDOM(categories), "player/" + player.firstName
					+ (player.firstName.isEmpty() || player.lastName.isEmpty() ? "" : " ") + player.lastName + ".xml",
					"../xsl/player.xsl");
		System.out.println("DONE");
	}

	private void writeDocument(Element inElement, String inXMLFile, String inXSLFile) throws IOException {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(inXMLFile), "UTF-8"));
		new XMLOutputter(Format.getPrettyFormat()).output(buildDocument(inElement, inXSLFile), writer);
		writer.close();
	}

	private Document buildDocument(Element inElement, String inXSLFile) {
		Document document = new Document();
		if (inXSLFile != null) {
			Map<String, String> data = new HashMap<String, String>();
			data.put("type", "text/xsl");
			data.put("href", inXSLFile);
			document.addContent(new ProcessingInstruction("xml-stylesheet", data));
		}
		document.addContent(inElement);
		return document;
	}

	private void writeCorrectChart() throws IOException {
		ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
		ChartUtilities.saveChartAsPNG(new File("category/correct.png"), correctChart(), 500, 650, info);
		writeCorrectImageMap(addURLs(info));
	}

	private JFreeChart correctChart() {
		JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, correctChartDataset());

		CategoryPlot plot = chart.getCategoryPlot();
		if (!players.isEmpty())
			plot.getRangeAxis().setRange(0, players.size());
		plot.setBackgroundPaint(Category.BACKGROUND_COLOR);
		plot.setOrientation(PlotOrientation.HORIZONTAL);

		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		renderer.setSeriesPaint(0, Category.BAR_GREEN);
		renderer.setSeriesPaint(1, Category.BAR_RED);
		return chart;
	}

	private DefaultCategoryDataset correctChartDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Category category : categories) {
			int correctCount = 0;
			int incorrectCount = 0;
			Set<String> winners = results.winners(category);
			if (!winners.isEmpty())
				for (String nominee : category.guesses.keySet())
					if (winners.contains(nominee))
						correctCount += category.guesses.get(nominee);
					else
						incorrectCount += category.guesses.get(nominee);
			dataset.addValue(correctCount, "Correct", category.name);
			dataset.addValue(incorrectCount, "Incorrect", category.name);
		}
		return dataset;
	}

	private ChartRenderingInfo addURLs(ChartRenderingInfo inInfo) {
		for (Object entity : inInfo.getEntityCollection().getEntities())
			if (entity.getClass().equals(CategoryLabelEntity.class))
				((CategoryLabelEntity) entity).setURLText("#" + ((CategoryLabelEntity) entity).getKey());
			else if (entity.getClass().equals(CategoryItemEntity.class))
				((CategoryItemEntity) entity).setURLText("#" + ((CategoryItemEntity) entity).getColumnKey());
		return inInfo;
	}

	private void writeCorrectImageMap(ChartRenderingInfo inInfo) throws FileNotFoundException, IOException {
		PrintWriter writer = new PrintWriter("category/correct.xsl");
		writer.println("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">");
		writer.println("<xsl:template match=\"/categories\">");
		ChartUtilities.writeImageMap(writer, "correct", inInfo, false);
		writer.println("</xsl:template>");
		writer.println("</xsl:stylesheet>");
		writer.close();
	}
}