package oscars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public abstract class IOUtils {
    public static final BufferedReader STDIN = new BufferedReader(new InputStreamReader(System.in));

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

    private static Document xmlDocument(String inXSLFile) {
        return inXSLFile == null ? new Document()
                : new Document().addContent(new ProcessingInstruction("xml-stylesheet",
                        Stream.of(new String[][] { { "type", "text/xsl" }, { "href", inXSLFile } })
                                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]))));
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