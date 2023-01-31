package oscars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public final class IOUtils {
    private static final String CATEGORIES_FILE = "categories.csv";

    private static final String CATEGORY_MAPS_FILE = "categoryMaps.xml";

    private static final String VALUE_DELIMITER = ",";

    private static final String COMMA_REPLACEMENT = "`";

    public static List<String[]> readCategoryValues() throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(CATEGORIES_FILE),
                StandardCharsets.ISO_8859_1)) {
            return stream.map(line -> Stream.of(line.split(VALUE_DELIMITER, -1))
                    .map(value -> value.replace(COMMA_REPLACEMENT, VALUE_DELIMITER))
                    .toArray(String[]::new)).collect(Collectors.toList());
        }
    }

    public static Map<String, Map<String, String>> readCategoryMaps() throws IOException {
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

    public static void writeCategoryMaps(Map<String, Map<String, String>> inCategoryMaps)
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

    public static Map<String, Map<String, String>> categoryMaps(List<String[]> inCategoryValues,
            Collection<Ballot> ballots, List<? extends List<String>> inCategoryNominees)
            throws IOException {
        Map<String, Map<String, String>> categoryMaps = IOUtils.readCategoryMaps();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        for (int categoryNum = 0; categoryNum < inCategoryValues.get(0).length; categoryNum++) {
            String categoryName = inCategoryValues.get(0)[categoryNum];
            Map<String, String> categoryMap = categoryMaps.computeIfAbsent(categoryName,
                    k -> new HashMap<>());
            List<String> nominees = inCategoryNominees.get(categoryNum);
            if (!nominees.isEmpty())
                for (Ballot ballot : ballots)
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
        IOUtils.writeCategoryMaps(categoryMaps);
        return categoryMaps;
    }

    public static List<? extends List<String>> categoryNominees(List<String[]> inCategoryValues) {
        return IntStream.range(0, inCategoryValues.get(0).length)
                .mapToObj(categoryNum -> inCategoryValues.stream().skip(1)
                        .map(guesses -> guesses[categoryNum]).filter(guess -> !guess.isEmpty())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static Category[] buildCategories(String[] inCategoryNames,
            List<? extends List<String>> inCategoryNominees,
            Map<String, Map<String, String>> inCategoryMaps, Collection<Ballot> inBallots)
            throws IOException {
        return IntStream.range(0, inCategoryNames.length).mapToObj(categoryNum -> Category.of(
                inCategoryNames[categoryNum],
                inCategoryNominees.get(categoryNum).stream()
                        .collect(Collectors.toMap(nominee -> nominee, nominee -> inBallots.stream()
                                .map(ballot -> inCategoryMaps.get(inCategoryNames[categoryNum])
                                        .get(ballot.get(categoryNum)))
                                .filter(nominee::equals).count())),
                inCategoryMaps.get(inCategoryNames[categoryNum]).entrySet().stream()
                        .collect(Collectors.toMap(Entry::getValue, Entry::getKey))))
                .toArray(Category[]::new);
    }

    public static List<Player> buildPlayers(Collection<Ballot> inBallots,
            Category[] inCategoryArray, String[] inCategoryNames,
            Map<String, Map<String, String>> inCategoryMaps) {
        return inBallots.stream().map(ballot -> new Player(IntStream
                .range(0, inCategoryArray.length).boxed()
                .collect(Collectors.toMap(categoryNum -> inCategoryArray[categoryNum],
                        categoryNum -> inCategoryMaps.get(inCategoryNames[categoryNum]).isEmpty()
                                ? ballot.get(categoryNum)
                                : inCategoryMaps.get(inCategoryNames[categoryNum])
                                        .get(ballot.get(categoryNum))))))
                .collect(Collectors.toList());
    }

    public static void mkdir(String inDirectory) {
        File directory = new File(inDirectory);
        if (!directory.exists())
            directory.mkdir();
    }

    public static void writeDocument(Element inElement, String inXMLFile, String inXSLFile)
            throws IOException {
        try (Writer writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(inXMLFile), "UTF-8"))) {
            new XMLOutputter(Format.getPrettyFormat()).output(xmlDocument(inXSLFile)
                    .addContent(new Comment("OSCARS website created by Scott McDonald"))
                    .addContent(inElement), writer);
        }
    }

    public static Document xmlDocument(String inXSLFile) {
        return inXSLFile == null ? new Document()
                : new Document().addContent(new ProcessingInstruction("xml-stylesheet", Stream
                        .of(new String[][] { { "type", "text/xsl" }, { "href", inXSLFile } })
                        .collect(Collectors.toMap(element -> element[0], element -> element[1]))));
    }

    public static void cleanUpCharts(String inDirectory, Stream<String> inChartsToKeep)
            throws IOException {
        Set<String> chartsToKeep = inChartsToKeep.collect(Collectors.toSet());
        Files.list(Paths.get(inDirectory))
                .filter(file -> file.toString().endsWith(".png")
                        && !chartsToKeep.contains(file.getFileName().toString()))
                .map(Path::toFile).forEach(File::delete);
    }

    public static void deleteOldData() {
        Stream.of(Category.DIRECTORY, Player.DIRECTORY, RankChart.DIRECTORY).map(File::new)
                .map(File::listFiles).flatMap(Stream::of).forEach(File::delete);
    }
}