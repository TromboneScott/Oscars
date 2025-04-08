package oscars.ballot;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import oscars.Font;
import oscars.Results;
import oscars.column.Category;
import oscars.column.Column;
import oscars.file.Directory;
import oscars.file.XMLFile;

/** Ballots from the survey with answers mapped to the website values - Immutable */
public final class MappedBallots extends Ballots {
    private static final XMLFile MAPPINGS_FILE = new XMLFile(Directory.DATA, "mappings.xml");

    private final Map<Column, Map<String, String>> columnMaps = Column.ALL.stream()
            .collect(Collectors.toMap(column -> column, column -> new LinkedHashMap<>()));

    private final Map<String, String> matches = new HashMap<>();

    /** Download ballots, read any existing mappings, update them and write the new mappings */
    public MappedBallots() throws Exception {
        super();
        readMappings();
        updateMappings();
        writeMappings();
    }

    /** The players with their answers mapped to the website values */
    public ImmutableList<Player> players() {
        return answers().stream()
                .map(ballot -> new Player(column -> columnMaps.get(column)
                        .getOrDefault(ballot.answer(column), ballot.answer(column))))
                .collect(ImmutableList.toImmutableList());
    }

    /** The survey descriptions of the nominees in the each category */
    public ImmutableMap<Category, ImmutableMap<String, String>> nomineeMap() {
        return Category.ALL.stream().collect(ImmutableMap.toImmutableMap(category -> category,
                category -> columnMaps.get(category).entrySet().stream().collect(
                        ImmutableMap.toImmutableMap(Entry::getValue, Entry::getKey, (a, b) -> b))));
    }

    private void readMappings() throws IOException {
        try {
            MAPPINGS_FILE.read()
                    .ifPresent(mappingsDOM -> mappingsDOM.getChildren("column")
                            .forEach(columnDOM -> columnDOM.getChildren("nominee")
                                    .forEach(nomineeDOM -> columnMaps
                                            .get(Category.of(columnDOM.getAttributeValue("name")))
                                            .put(nomineeDOM.getAttributeValue("ballot"),
                                                    nomineeDOM.getAttributeValue("name")))));
        } catch (Exception e) {
            throw new IOException("Error reading mappings file: " + MAPPINGS_FILE, e);
        }
    }

    private void updateMappings() {
        for (Category category : Category.ALL) {
            Map<String, String> columnMap = columnMaps.get(category);
            answers().stream().map(ballot -> ballot.answer(category))
                    .filter(guess -> !columnMap.containsKey(guess))
                    .forEach(guess -> columnMap.put(guess, mapping(category,
                            StringUtils.substringBeforeLast(guess, " - ").toUpperCase())));
            for (String nominee : category.nominees())
                if (!columnMap.containsValue(nominee)) {
                    System.out.println(
                            "\n" + Font.TITLE + "Nominee not chosen on any Ballots" + Font.NONE);
                    System.out.println("CATEGORY: " + category.name());
                    System.out.println("NOMINEE: " + nominee);
                    System.out.print(Font.BROWN + "Enter Ballot Description: " + Font.NONE);
                    columnMap.put(Results.STDIN.nextLine(), nominee);
                }
        }
    }

    private String mapping(Category inCategory, String inGuess) {
        String match = matches.get(inGuess);
        if (inCategory.nominees().contains(match))
            return match;
        if (match != null)
            System.out.println("\n\n" + Font.YELLOW + "WARNING (" + inCategory.name() + ")"
                    + Font.NONE + "\nPrevious category mapped guess to different nominee\n"
                    + Font.CYAN + inGuess + " -> " + match + Font.NONE);

        ImmutableList<String> mappings = inCategory.nominees().stream()
                .filter(nominee -> inGuess.contains(nominee.toUpperCase()))
                .collect(ImmutableList.toImmutableList());
        String mapping = mappings.size() == 1 ? mappings.get(0)
                : prompt(inCategory, inGuess,
                        mappings.isEmpty() ? inCategory.nominees() : mappings);
        matches.put(inGuess, mapping);
        return mapping;
    }

    private static String prompt(Category inCategory, String inGuess,
            ImmutableList<String> inNominees) {
        System.out.println("\n" + Font.TITLE + inCategory.name() + Font.NONE);
        for (int nomineeNum = 0; nomineeNum < inNominees.size(); nomineeNum++)
            System.out.println(Results.formatNumber(nomineeNum) + inNominees.get(nomineeNum));
        System.out.print(Font.BROWN + inGuess + Font.NONE + " = ");
        String input = Results.STDIN.nextLine();
        try {
            return inNominees.get(Integer.parseInt(input) - 1);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            Results.outputInvalidInput(input);
            return prompt(inCategory, inGuess, inNominees);
        }
    }

    private void writeMappings() throws IOException {
        MAPPINGS_FILE.write(IntStream.range(0, Column.ALL.size()).mapToObj(column -> columnMaps
                .get(Column.ALL.get(column)).entrySet().stream()
                .map(map -> new Element("nominee").setAttribute("name", map.getValue())
                        .setAttribute("ballot", map.getKey()))
                .reduce(new Element("column").setAttribute("name", Column.ALL.get(column).name())
                        .setAttribute("ballot", headers().get(column)), Element::addContent))
                .reduce(new Element("mappings"), Element::addContent));
    }
}