package oscars;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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

    private final Map<String, Map<String, String>> categoryMaps;

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

    private Map<String, Map<String, String>> categoryMaps() throws IOException {
        Map<String, Map<String, String>> categoryMaps = readCategoryMaps();
        for (Entry<String, Category> categoryEntry : ballotReader.categoryDefinitions.entrySet()) {
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(categoryEntry.getKey(),
                    k -> new HashMap<>());
            List<String> nominees = categoryEntry.getValue().nominees.stream()
                    .map(nominee -> nominee.name).collect(Collectors.toList());
            if (!nominees.isEmpty())
                for (Ballot ballot : ballots) {
                    String guess = ballot.get(categoryEntry.getKey());
                    if (!categoryMap.containsKey(guess)) {
                        List<String> mappings = nominees.stream().filter(
                                nominee -> guess.toUpperCase().contains(nominee.toUpperCase()))
                                .collect(Collectors.toList());
                        categoryMap.put(guess, mappings.size() == 1 ? mappings.get(0)
                                : prompt(categoryEntry.getKey(), guess, nominees));
                    }
                }
        }
        return categoryMaps;
    }

    private static String prompt(String inCategoryName, String inGuess, List<String> inNominees) {
        System.out.println("\nCATEGORY: " + inCategoryName);
        for (int nomineeNum = 0; nomineeNum < inNominees.size(); nomineeNum++)
            System.out.println((nomineeNum + 1) + ": " + inNominees.get(nomineeNum));
        System.out.print(inGuess + " = ");
        String input = Results.STDIN.nextLine();
        try {
            return inNominees.get(Integer.parseInt(input) - 1);
        } catch (Exception e) {
            System.out.println("\nInvalid Input: " + input);
            return prompt(inCategoryName, inGuess, inNominees);
        }
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
                                                mapDOM -> mapDOM.getChildText("ballot"),
                                                mapDOM -> mapDOM.getChildText("website")))));
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
        Directory.CURRENT.write(ballotReader.categoryDefinitions.keySet().stream()
                .map(category -> categoryMaps.get(category).entrySet().stream()
                        .sorted(Comparator.comparing(Entry::getKey, String::compareToIgnoreCase))
                        .map(map -> new Element("map")
                                .addContent(new Element("ballot").addContent(map.getKey()))
                                .addContent(new Element("website").addContent(map.getValue())))
                        .reduce(new Element("category").addContent(
                                new Element("name").addContent(category)), Element::addContent))
                .reduce(new Element("categories"), Element::addContent), CATEGORY_MAPS_FILE, null);
    }
}