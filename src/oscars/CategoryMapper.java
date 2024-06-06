package oscars;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/** Map the Category data - Immutable */
public final class CategoryMapper {
    private static final String MAPPINGS_FILE = "mappings.xml";

    private final Collection<Ballot> ballots;

    private final Map<String, LinkedHashMap<String, String>> categoryMaps;

    public CategoryMapper() throws IOException {
        ballots = Ballot.readBallots().collect(Ballot.LATEST);
        categoryMaps = categoryMaps();
        writeCategoryMaps(readFile(element -> element.getAttributeValue("ballot")), categoryMaps);
    }

    public static void setHeaders(String[] inHeaders) throws IOException {
        if (inHeaders.length != Category.ALL.size())
            throw new IOException("Ballot headers: " + inHeaders.length
                    + " does not match defined categories: " + Category.ALL.size());
        writeCategoryMaps(
                IntStream.range(0, inHeaders.length).boxed().collect(Collectors.toMap(
                        column -> Category.ALL.get(column).name, column -> inHeaders[column])),
                readCategoryMaps());
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(ballots.stream()
                .map(ballot -> new Player(ballot.values.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey,
                                entry -> Optional.ofNullable(categoryMaps.get(entry.getKey()))
                                        .map(map -> map.get(entry.getValue()))
                                        .orElseGet(() -> entry.getValue())))))
                .collect(Collectors.toList()));
    }

    public Map<String, Map<String, String>> getNomineeDescriptions() {
        return Collections.unmodifiableMap(categoryMaps.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey,
                        entry -> Collections.unmodifiableMap(entry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(Entry::getValue, Entry::getKey))))));
    }

    private Map<String, LinkedHashMap<String, String>> categoryMaps() throws IOException {
        Map<String, LinkedHashMap<String, String>> categoryMaps = readCategoryMaps();
        Category.stream().forEach(category -> {
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(category.name,
                    k -> new LinkedHashMap<>());
            ballots.stream().sorted(Comparator.comparing(Ballot::getTimestamp))
                    .map(ballot -> ballot.values.get(category.name))
                    .filter(guess -> !categoryMap.containsKey(guess))
                    .forEach(guess -> categoryMap.put(guess, mapping(category, guess)));
            for (String nominee : category.nominees)
                if (!categoryMap.containsValue(nominee)) {
                    System.out.println("\n--Nominee not chosen on any Ballots--");
                    System.out.println("CATEGORY: " + category.name);
                    System.out.println("NOMINEE: " + nominee);
                    System.out.print("Enter Ballot Description: ");
                    categoryMap.put(Results.STDIN.nextLine(), nominee);
                }
        });
        return categoryMaps;
    }

    private static String mapping(Category inCategory, String inGuess) {
        List<String> mappings = inCategory.nominees.stream()
                .filter(nominee -> inGuess.toUpperCase().contains(nominee.toUpperCase()))
                .collect(Collectors.toList());
        return mappings.size() == 1 ? mappings.get(0)
                : prompt(inCategory, inGuess, mappings.isEmpty() ? inCategory.nominees : mappings);
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

    private static <T> Map<String, T> readFile(Function<Element, T> inFunction) throws IOException {
        File mappingsFile = new File(MAPPINGS_FILE);
        if (mappingsFile.exists())
            try {
                return new SAXBuilder().build(mappingsFile).getRootElement().getChildren("category")
                        .stream()
                        .collect(Collectors.toMap(
                                categoryDOM -> categoryDOM.getAttributeValue("name"),
                                inFunction::apply));
            } catch (JDOMException e) {
                throw new IOException("ERROR: Unable to read mappings file: " + MAPPINGS_FILE, e);
            }
        System.out.println("\nStarting new mappings file: " + MAPPINGS_FILE);
        return new HashMap<>();
    }

    private static Map<String, LinkedHashMap<String, String>> readCategoryMaps()
            throws IOException {
        return readFile(element -> element.getChildren("map").stream()
                .collect(Collectors.toMap(mapDOM -> mapDOM.getAttributeValue("ballot"),
                        mapDOM -> mapDOM.getAttributeValue("website"), (list1, list2) -> list1,
                        LinkedHashMap::new)));
    }

    private static void writeCategoryMaps(Map<String, String> inHeaderMap,
            Map<String, LinkedHashMap<String, String>> inCategoryMaps) throws IOException {
        Directory.CURRENT.write(Category.ALL.stream().map(category -> category.name)
                .map(category -> Optional.ofNullable(inCategoryMaps.get(category))
                        .orElseGet(() -> new LinkedHashMap<>()).entrySet().stream()
                        .map(map -> new Element("map").setAttribute("website", map.getValue())
                                .setAttribute("ballot", map.getKey()))
                        .reduce(new Element("category").setAttribute("name", category).setAttribute(
                                "ballot", inHeaderMap.get(category)), Element::addContent))
                .reduce(new Element("mappings"), Element::addContent), MAPPINGS_FILE, null);
    }
}