package oscars;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/** Map the Category data - Immutable */
public final class CategoryMapper {
    private static final String CATEGORY_MAPS_FILE = "categoryMaps.xml";

    private final BallotReader ballotReader;

    private final Collection<Ballot> ballots;

    private final Map<String, LinkedHashMap<String, String>> categoryMaps;

    public CategoryMapper() throws IOException {
        ballotReader = new BallotReader();
        ballots = ballotReader.readBallots().collect(Ballot.LATEST);
        categoryMaps = categoryMaps();
        defineCategories();
        writeCategoryMaps();
    }

    public List<Player> getPlayers() {
        return ballots.stream()
                .map(ballot -> new Player(categoryMaps.entrySet().stream()
                        .collect(Collectors.toMap(entry -> Category.of(entry.getKey()),
                                entry -> entry.getValue().isEmpty() ? ballot.get(entry.getKey())
                                        : entry.getValue().get(ballot.get(entry.getKey()))))))
                .collect(Collectors.toList());
    }

    public List<Category> getCategories() {
        return ballotReader.categoryDefinitions.values().stream()
                .filter(category -> !category.nominees.isEmpty())
                .map(category -> Category.of(category.name)).collect(Collectors.toList());
    }

    private Map<String, LinkedHashMap<String, String>> categoryMaps() throws IOException {
        Map<String, LinkedHashMap<String, String>> categoryMaps = readCategoryMaps();
        for (Category category : ballotReader.categoryDefinitions.values()) {
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(category.name,
                    k -> new LinkedHashMap<>());
            List<String> nominees = category.nominees.stream().map(nominee -> nominee.name)
                    .collect(Collectors.toList());
            if (!nominees.isEmpty())
                ballots.stream().sorted(Comparator.comparing(Ballot::getTimestamp))
                        .map(ballot -> ballot.get(category.name))
                        .filter(guess -> !categoryMap.containsKey(guess))
                        .forEach(guess -> categoryMap.put(guess,
                                mapping(category, guess, nominees)));
            for (String nominee : nominees)
                if (!categoryMap.containsValue(nominee)) {
                    System.out.println("\n--Nominee not chosen on any Ballots--");
                    System.out.println("CATEGORY: " + category.name);
                    System.out.println("NOMINEE: " + nominee);
                    System.out.print("Enter Ballot Description: ");
                    categoryMap.put(Results.STDIN.nextLine(), nominee);
                }
        }
        return categoryMaps;
    }

    private static String mapping(Category inCategory, String inGuess, List<String> inNominees) {
        List<String> mappings = inNominees.stream()
                .filter(nominee -> inGuess.toUpperCase().contains(nominee.toUpperCase()))
                .collect(Collectors.toList());
        return mappings.size() == 1 ? mappings.get(0)
                : prompt(inCategory, inGuess, mappings.isEmpty() ? inNominees : mappings);
    }

    private static String prompt(Category inCategory, String inGuess, List<String> inNominees) {
        System.out.println("\nCATEGORY: " + inCategory.name);
        for (int nomineeNum = 0; nomineeNum < inNominees.size(); nomineeNum++)
            System.out.println((nomineeNum + 1) + ": " + inNominees.get(nomineeNum));
        System.out.print(inGuess + " = ");
        String input = Results.STDIN.nextLine();
        try {
            return inNominees.get(Integer.parseInt(input) - 1);
        } catch (Exception e) {
            System.out.println("\nInvalid Input: " + input);
            return prompt(inCategory, inGuess, inNominees);
        }
    }

    private static Map<String, LinkedHashMap<String, String>> readCategoryMaps()
            throws IOException {
        File categoryMapsFile = new File(CATEGORY_MAPS_FILE);
        if (categoryMapsFile.exists())
            try {
                return new SAXBuilder().build(categoryMapsFile).getRootElement()
                        .getChildren("category").stream()
                        .collect(Collectors.toMap(
                                categoryDOM -> categoryDOM.getAttributeValue("name"),
                                categoryDOM -> categoryDOM.getChildren("map").stream()
                                        .collect(Collectors.toMap(
                                                mapDOM -> mapDOM.getAttributeValue("ballot"),
                                                mapDOM -> mapDOM.getAttributeValue("website"),
                                                (list1, list2) -> list1, LinkedHashMap::new))));
            } catch (JDOMException e) {
                throw new IOException(
                        "ERROR: Unable to read category maps file: " + CATEGORY_MAPS_FILE, e);
            }
        System.out.println("\nStarting new category maps file: " + CATEGORY_MAPS_FILE);
        return new HashMap<>();
    }

    private void defineCategories() {
        ballotReader.categoryDefinitions.values().stream()
                .filter(category -> !category.nominees.isEmpty())
                .forEach(category -> new Category(category.name, category.tieBreaker,
                        category.nominees.stream().map(nominee -> new Nominee(nominee.name,
                                categoryMaps.get(category.name).entrySet().stream()
                                        .filter(entry -> entry.getValue().equals(nominee.name))
                                        .map(Entry::getKey).findAny().orElse(null),
                                ballots.stream()
                                        .map(ballot -> categoryMaps.get(category.name)
                                                .get(ballot.get(category.name)))
                                        .filter(nominee.name::equals).count()))));
    }

    private void writeCategoryMaps() throws IOException {
        Directory.CURRENT.write(categoryMaps.entrySet().stream()
                .map(entry -> entry.getValue().entrySet().stream()
                        .map(map -> new Element("map").setAttribute("website", map.getValue())
                                .setAttribute("ballot", map.getKey()))
                        .reduce(new Element("category").setAttribute("name", entry.getKey()),
                                Element::addContent))
                .reduce(new Element("categories"), Element::addContent), CATEGORY_MAPS_FILE, null);
    }
}