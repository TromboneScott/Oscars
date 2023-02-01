package oscars;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        List<List<String>> categoryNominees = categoryNominees(categoryValues);
        categoryMaps = categoryMaps(ballots, categoryValues, categoryNominees);
        categoryArray = categoryArray(ballots, categoryValues, categoryNominees, categoryMaps);
        writeCategoryMaps(categoryMaps);
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

    private static List<List<String>> categoryNominees(List<String[]> inCategoryValues) {
        return IntStream.range(0, inCategoryValues.get(0).length)
                .mapToObj(categoryNum -> inCategoryValues.stream().skip(1)
                        .map(guesses -> guesses[categoryNum]).filter(guess -> !guess.isEmpty())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private static Map<String, Map<String, String>> categoryMaps(Collection<Ballot> inBallots,
            List<String[]> inCategoryValues, List<List<String>> inCategoryNominees)
            throws IOException {
        Map<String, Map<String, String>> categoryMaps = readCategoryMaps();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        for (int categoryNum = 0; categoryNum < inCategoryValues.get(0).length; categoryNum++) {
            String categoryName = inCategoryValues.get(0)[categoryNum];
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(categoryName,
                    k -> new HashMap<>());
            List<String> nominees = inCategoryNominees.get(categoryNum);
            if (!nominees.isEmpty())
                for (Ballot ballot : inBallots)
                    if (!categoryMap.containsKey(ballot.get(categoryNum))) {
                        List<String> mappings = nominees.stream()
                                .filter(ballot.get(categoryNum)::contains)
                                .collect(Collectors.toList());
                        if (mappings.size() == 1)
                            categoryMap.put(ballot.get(categoryNum), mappings.get(0));
                        else {
                            System.out.println("\nCATEGORY: " + categoryName);
                            for (int nomineeNum = 0; nomineeNum < nominees.size(); nomineeNum++)
                                System.out.println(
                                        (nomineeNum + 1) + ": " + nominees.get(nomineeNum));
                            System.out.print(ballot.get(categoryNum) + " = ");
                            String guessNum = stdin.readLine();
                            categoryMap.put(ballot.get(categoryNum),
                                    nominees.get(Integer.parseInt(guessNum) - 1));
                        }
                    }
        }
        return categoryMaps;
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

    private static Category[] categoryArray(Collection<Ballot> inBallots,
            List<String[]> inCategoryValues, List<List<String>> inCategoryNominees,
            Map<String, Map<String, String>> inCategoryMaps) {
        return IntStream.range(0, inCategoryValues.get(0).length)
                .mapToObj(categoryNum -> Category.of(inCategoryValues.get(0)[categoryNum],
                        inCategoryNominees.get(categoryNum).stream()
                                .collect(Collectors.toMap(nominee -> nominee,
                                        nominee -> inBallots.stream()
                                                .map(ballot -> inCategoryMaps
                                                        .get(inCategoryValues.get(0)[categoryNum])
                                                        .get(ballot.get(categoryNum)))
                                                .filter(nominee::equals).count())),
                        inCategoryMaps.get(inCategoryValues.get(0)[categoryNum]).entrySet().stream()
                                .collect(Collectors.toMap(Entry::getValue, Entry::getKey))))
                .toArray(Category[]::new);
    }

    private static void writeCategoryMaps(Map<String, Map<String, String>> inCategoryMaps)
            throws IOException {
        IOUtils.writeDocument(inCategoryMaps.keySet().stream()
                .map(category -> inCategoryMaps.get(category).entrySet().stream()
                        .map(map -> new Element("map")
                                .addContent(new Element("key").addContent(map.getKey()))
                                .addContent(new Element("value").addContent(map.getValue())))
                        .reduce(new Element("category").addContent(
                                new Element("name").addContent(category)), Element::addContent))
                .reduce(new Element("categories"), Element::addContent), CATEGORY_MAPS_FILE, null);
    }
}