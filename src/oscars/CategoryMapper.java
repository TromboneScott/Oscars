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

    private final Map<String, LinkedHashMap<String, String>> categoryMaps;

    private final HashMap<String, String> matches = new HashMap<>();

    /** Read the ballots and write the website mappings */
    public CategoryMapper() throws IOException {
        categoryMaps = readCategoryMaps();
        for (Category category : Category.ALL) {
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(category.getName(),
                    k -> new LinkedHashMap<>());
            ballots.getPlayers().stream().sorted(Comparator.comparing(Player::getTimestamp))
                    .map(player -> player.getPick(category.getName()))
                    .filter(guess -> !categoryMap.containsKey(guess))
                    .forEach(guess -> categoryMap.put(guess, mapping(category,
                            StringUtils.substringBeforeLast(guess, " - ").toUpperCase())));
            for (String nominee : category.getNominees())
                if (!categoryMap.containsValue(nominee)) {
                    System.out.println("\n--Nominee not chosen on any Ballots--");
                    System.out.println("CATEGORY: " + category.getName());
                    System.out.println("NOMINEE: " + nominee);
                    System.out.print("Enter Ballot Description: ");
                    categoryMap.put(Results.STDIN.nextLine(), nominee);
                }
        }
        writeCategoryMaps();
    }

    /** Get the players with their entries */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(ballots.getPlayers().stream()
                .map(player -> new Player(Category.DEFINED.stream().map(Category::getName)
                        .collect(Collectors.toMap(category -> category,
                                category -> Optional.ofNullable(categoryMaps.get(category))
                                        .map(map -> map.get(player.getPick(category)))
                                        .orElseGet(() -> player.getPick(category))))))
                .collect(Collectors.toList()));
    }

    /** Get the survey descriptions of the nominees in each Category */
    public Map<String, Map<String, String>> getNomineeDescriptions() {
        return Collections.unmodifiableMap(categoryMaps.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey,
                        entry -> Collections.unmodifiableMap(entry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(Entry::getValue, Entry::getKey))))));
    }

    private String mapping(Category inCategory, String inGuess) {
        String match = matches.get(inGuess);
        if (inCategory.getNominees().contains(match))
            return match;
        if (match != null)
            System.out.println(
                    "\n\n*** WARNING - Existing mapping found ***\n" + inGuess + "\n" + match);

        List<String> mappings = inCategory.getNominees().stream()
                .filter(nominee -> inGuess.contains(nominee.toUpperCase()))
                .collect(Collectors.toList());
        String mapping = mappings.size() == 1 ? mappings.get(0)
                : prompt(inCategory, inGuess,
                        mappings.isEmpty() ? inCategory.getNominees() : mappings);
        matches.put(inGuess, mapping);
        return mapping;
    }

    private static String prompt(Category inCategory, String inGuess, List<String> inNominees) {
        System.out.println("\nCATEGORY: " + inCategory.getName());
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
        return Directory.DATA.getRootElement(MAPPINGS_FILE)
                .map(mappingsDOM -> mappingsDOM.getChildren("category").stream())
                .orElseGet(Stream::empty)
                .collect(Collectors.toMap(categoryDOM -> categoryDOM.getAttributeValue("name"),
                        categoryDOM -> categoryDOM.getChildren("nominee").stream()
                                .collect(Collectors.toMap(
                                        nomineeDOM -> nomineeDOM.getAttributeValue("ballot"),
                                        nomineeDOM -> nomineeDOM.getAttributeValue("name"),
                                        (name1, name2) -> name2, LinkedHashMap::new))));
    }

    private void writeCategoryMaps() throws IOException {
        Directory.DATA.write(IntStream.range(0, ballots.getCategories().size())
                .mapToObj(column -> categoryDOM(Category.DEFINED.get(column).getName(),
                        ballots.getCategories().get(column)))
                .reduce(new Element("mappings"), Element::addContent), MAPPINGS_FILE, null);
    }

    private Element categoryDOM(String inName, String inBallot) {
        return Optional.ofNullable(categoryMaps.get(inName))
                .map(mapping -> mapping.entrySet().stream()).orElseGet(Stream::empty)
                .map(map -> new Element("nominee").setAttribute("name", map.getValue())
                        .setAttribute("ballot", map.getKey()))
                .reduce(new Element("category").setAttribute("name", inName).setAttribute("ballot",
                        inBallot), Element::addContent);
    }
}