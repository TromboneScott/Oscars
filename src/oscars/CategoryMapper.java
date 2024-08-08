package oscars;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

/** Map the Category data from the survey to the website - Immutable */
public final class CategoryMapper {
    private static final String MAPPINGS_FILE = "mappings.xml";

    private final Ballots ballots = new Ballots();

    private final Map<Column, LinkedHashMap<String, String>> categoryMaps;

    private final HashMap<String, String> matches = new HashMap<>();

    /** Read the ballots and write the website mappings */
    public CategoryMapper() throws IOException {
        categoryMaps = readCategoryMaps();
        for (Column category : Column.CATEGORIES) {
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(category,
                    k -> new LinkedHashMap<>());
            ballots.players().stream().sorted(Comparator.comparing(Player::getTimestamp))
                    .map(player -> player.answer(category))
                    .filter(guess -> !categoryMap.containsKey(guess))
                    .forEach(guess -> categoryMap.put(guess, mapping(category,
                            StringUtils.substringBeforeLast(guess, " - ").toUpperCase())));
            for (String nominee : category.nominees())
                if (!categoryMap.containsValue(nominee)) {
                    System.out.println("\n--Nominee not chosen on any Ballots--");
                    System.out.println("CATEGORY: " + category);
                    System.out.println("NOMINEE: " + nominee);
                    System.out.print("Enter Ballot Description: ");
                    categoryMap.put(Results.STDIN.nextLine(), nominee);
                }
        }
        writeCategoryMaps();
    }

    /** The players with their entries */
    public List<Player> players() {
        return Collections.unmodifiableList(ballots.players().stream()
                .map(player -> new Player(Column.ALL.stream()
                        .collect(Collectors.toMap(column -> column,
                                column -> Optional.ofNullable(categoryMaps.get(column))
                                        .map(map -> map.get(player.answer(column)))
                                        .orElseGet(() -> player.answer(column))))))
                .collect(Collectors.toList()));
    }

    /** The survey descriptions of the nominees in each Category */
    public Map<Column, Map<String, String>> nomineeDescriptions() {
        return Collections.unmodifiableMap(categoryMaps.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey,
                        entry -> Collections.unmodifiableMap(entry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(Entry::getValue, Entry::getKey))))));
    }

    private String mapping(Column inCategory, String inGuess) {
        String match = matches.get(inGuess);
        if (inCategory.nominees().contains(match))
            return match;
        if (match != null)
            System.out.println(
                    "\n\n*** WARNING - Existing mapping found ***\n" + inGuess + "\n" + match);

        List<String> mappings = inCategory.nominees().stream()
                .filter(nominee -> inGuess.contains(nominee.toUpperCase()))
                .collect(Collectors.toList());
        String mapping = mappings.size() == 1 ? mappings.get(0)
                : prompt(inCategory, inGuess,
                        mappings.isEmpty() ? inCategory.nominees() : mappings);
        matches.put(inGuess, mapping);
        return mapping;
    }

    private static String prompt(Column inCategory, String inGuess, List<String> inNominees) {
        System.out.println("\nCATEGORY: " + inCategory);
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

    private static Map<Column, LinkedHashMap<String, String>> readCategoryMaps()
            throws IOException {
        try {
            return Directory.DATA.getRootElement(MAPPINGS_FILE)
                    .map(mappingsDOM -> mappingsDOM.getChildren("category").stream())
                    .orElseGet(Stream::empty)
                    .collect(Collectors.toMap(
                            categoryDOM -> Column.of(categoryDOM.getAttributeValue("name")),
                            categoryDOM -> categoryDOM.getChildren("nominee").stream()
                                    .collect(Collectors.toMap(
                                            nomineeDOM -> nomineeDOM.getAttributeValue("ballot"),
                                            nomineeDOM -> nomineeDOM.getAttributeValue("name"),
                                            (name1, name2) -> name2, LinkedHashMap::new))));
        } catch (Exception e) {
            throw new IOException("Error reading mappings file: " + MAPPINGS_FILE, e);
        }
    }

    private void writeCategoryMaps() throws IOException {
        Directory.DATA.write(IntStream.range(0, Column.ALL.size()).mapToObj(
                column -> categoryDOM(Column.ALL.get(column), ballots.headers().get(column)))
                .reduce(new Element("mappings"), Element::addContent), MAPPINGS_FILE, null);
    }

    private Element categoryDOM(Column inColumn, String inBallot) {
        return Optional.ofNullable(categoryMaps.get(inColumn))
                .map(mapping -> mapping.entrySet().stream()).orElseGet(Stream::empty)
                .map(map -> new Element("nominee").setAttribute("name", map.getValue())
                        .setAttribute("ballot", map.getKey()))
                .reduce(new Element("category").setAttribute("name", inColumn.header())
                        .setAttribute("ballot", inBallot), Element::addContent);
    }
}