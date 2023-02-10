package oscars;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/** Map the Category data - Immutable */
public final class CategoryMapper {
    private static final String CATEGORIES_FILE = "categories.csv";

    private static final String CATEGORY_MAPS_FILE = "categoryMaps.xml";

    private static final String VALUE_DELIMITER = ",";

    private static final String COMMA_REPLACEMENT = "`";

    private final Collection<Ballot> ballots;

    private final List<String[]> categoryValues;

    private final Map<String, Map<String, String>> categoryMaps;

    private final Category[] categoryArray;

    public CategoryMapper(Stream<Ballot> inBallots) throws IOException {
        ballots = inBallots.collect(Ballot.LATEST);
        categoryValues = readCategoryValues();
        List<List<String>> categoryNominees = categoryNominees();
        categoryMaps = categoryMaps(categoryNominees);
        categoryArray = categoryArray(categoryNominees);
        writeCategoryMaps();
    }

    public List<Player> getPlayers() {
        return ballots.stream().map(ballot -> new Player(IntStream.range(0, categoryArray.length)
                .boxed()
                .collect(Collectors.toMap(categoryNum -> categoryArray[categoryNum],
                        categoryNum -> categoryMaps.get(categoryValues.get(0)[categoryNum])
                                .isEmpty()
                                        ? ballot.get(categoryNum)
                                        : categoryMaps.get(categoryValues.get(0)[categoryNum])
                                                .get(ballot.get(categoryNum))))))
                .collect(Collectors.toList());
    }

    public List<Category> getCategories() {
        return Arrays.stream(categoryArray).skip(1).filter(category -> category.guesses != null)
                .collect(Collectors.toList());
    }

    private static List<String[]> readCategoryValues() throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(CATEGORIES_FILE),
                StandardCharsets.ISO_8859_1)) {
            return stream.map(line -> Stream.of(line.split(VALUE_DELIMITER, -1))
                    .map(value -> value.replace(COMMA_REPLACEMENT, VALUE_DELIMITER))
                    .toArray(String[]::new)).collect(Collectors.toList());
        }
    }

    private List<List<String>> categoryNominees() {
        return IntStream.range(0, categoryValues.get(0).length)
                .mapToObj(categoryNum -> categoryValues.stream().skip(1)
                        .map(guesses -> guesses[categoryNum]).filter(guess -> !guess.isEmpty())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, Map<String, String>> categoryMaps(List<List<String>> inCategoryNominees)
            throws IOException {
        Map<String, Map<String, String>> categoryMaps = readCategoryMaps();
        for (int categoryNum = 0; categoryNum < categoryValues.get(0).length; categoryNum++) {
            String categoryName = categoryValues.get(0)[categoryNum];
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(categoryName,
                    k -> new HashMap<>());
            List<String> nominees = inCategoryNominees.get(categoryNum);
            if (!nominees.isEmpty())
                for (Ballot ballot : ballots) {
                    String guess = ballot.get(categoryNum);
                    if (!categoryMap.containsKey(guess)) {
                        List<String> mappings = nominees.stream().filter(
                                nominee -> guess.toUpperCase().contains(nominee.toUpperCase()))
                                .collect(Collectors.toList());
                        categoryMap.put(guess, mappings.size() == 1 ? mappings.get(0)
                                : prompt(categoryName, guess, nominees));
                    }
                }
        }
        return categoryMaps;
    }

    private static String prompt(String inCategoryName, String inGuess, List<String> inNominees)
            throws IOException {
        System.out.println("\nCATEGORY: " + inCategoryName);
        for (int nomineeNum = 0; nomineeNum < inNominees.size(); nomineeNum++)
            System.out.println((nomineeNum + 1) + ": " + inNominees.get(nomineeNum));
        System.out.print(inGuess + " = ");
        return inNominees.get(Integer.parseInt(IOUtils.STDIN.readLine()) - 1);
    }

    private static Map<String, Map<String, String>> readCategoryMaps() throws IOException {
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

    private Category[] categoryArray(List<List<String>> inCategoryNominees) {
        return IntStream.range(0, categoryValues.get(0).length).mapToObj(categoryNum -> Category.of(
                categoryValues.get(0)[categoryNum],
                inCategoryNominees.get(categoryNum).stream()
                        .map(nominee -> new Guess(nominee, ballots.stream()
                                .map(ballot -> categoryMaps.get(categoryValues.get(0)[categoryNum])
                                        .get(ballot.get(categoryNum)))
                                .filter(nominee::equals).count(),
                                categoryMaps.get(categoryValues.get(0)[categoryNum])
                                        .get(nominee)))))
                .toArray(Category[]::new);
    }

    private void writeCategoryMaps() throws IOException {
        IOUtils.writeDocument(categoryMaps.keySet().stream()
                .map(category -> categoryMaps.get(category).entrySet().stream()
                        .map(map -> new Element("map")
                                .addContent(new Element("key").addContent(map.getKey()))
                                .addContent(new Element("value").addContent(map.getValue())))
                        .reduce(new Element("category").addContent(
                                new Element("name").addContent(category)), Element::addContent))
                .reduce(new Element("categories"), Element::addContent), CATEGORY_MAPS_FILE, null);
    }
}