package oscars;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/** Map the data from the survey to the website - Immutable */
public final class Mapper {
    private static final String MAPPINGS_FILE = "mappings.xml";

    private final Ballots ballots = new Ballots();

    private final Map<Column, Map<String, String>> columnMaps = Column.ALL.stream()
            .collect(Collectors.toMap(column -> column, column -> new LinkedHashMap<>()));

    private final Map<String, String> matches = new HashMap<>();

    /** Read any existing mappings, update with the ballots and write the mappings */
    public Mapper() throws IOException {
        readMappings();
        updateMappings();
        writeMappings();
    }

    /** The players with their entries */
    public ImmutableList<Player> players() {
        return ballots.players().stream()
                .map(player -> new Player(Column.ALL.stream()
                        .map(column -> columnMaps.get(column).getOrDefault(player.answer(column),
                                player.answer(column)))
                        .toArray(String[]::new)))
                .collect(ImmutableList.toImmutableList());
    }

    /** The survey descriptions of the nominees in the given category */
    public ImmutableMap<String, String> nomineeMapping(Column inCategory) {
        return columnMaps.get(inCategory).entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(Entry::getValue, Entry::getKey, (a, b) -> b));
    }

    private void readMappings() throws IOException {
        try {
            Directory.DATA.getRootElement(MAPPINGS_FILE)
                    .map(mappingsDOM -> mappingsDOM.getChildren("column").stream())
                    .orElseGet(Stream::empty)
                    .forEach(columnDOM -> columnDOM.getChildren("nominee")
                            .forEach(nomineeDOM -> columnMaps
                                    .get(Column.of(columnDOM.getAttributeValue("name")))
                                    .put(nomineeDOM.getAttributeValue("ballot"),
                                            nomineeDOM.getAttributeValue("name"))));
        } catch (Exception e) {
            throw new IOException("Error reading mappings file: " + MAPPINGS_FILE, e);
        }
    }

    private void updateMappings() {
        for (Column category : Column.CATEGORIES) {
            Map<String, String> columnMap = columnMaps.get(category);
            ballots.players().stream().map(player -> player.answer(category))
                    .filter(guess -> !columnMap.containsKey(guess))
                    .forEach(guess -> columnMap.put(guess, mapping(category,
                            StringUtils.substringBeforeLast(guess, " - ").toUpperCase())));
            for (String nominee : category.nominees())
                if (!columnMap.containsValue(nominee)) {
                    System.out.println("\n--Nominee not chosen on any Ballots--");
                    System.out.println("CATEGORY: " + category);
                    System.out.println("NOMINEE: " + nominee);
                    System.out.print("Enter Ballot Description: ");
                    columnMap.put(Results.STDIN.nextLine(), nominee);
                }
        }
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
                .collect(ImmutableList.toImmutableList());
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

    private void writeMappings() throws IOException {
        Directory.DATA.write(IntStream.range(0, Column.ALL.size()).mapToObj(column -> columnMaps
                .get(Column.ALL.get(column)).entrySet().stream()
                .map(map -> new Element("nominee").setAttribute("name", map.getValue())
                        .setAttribute("ballot", map.getKey()))
                .reduce(new Element("column").setAttribute("name", Column.ALL.get(column).name())
                        .setAttribute("ballot", ballots.headers().get(column)),
                        Element::addContent))
                .reduce(new Element("mappings"), Element::addContent), MAPPINGS_FILE, null);
    }
}