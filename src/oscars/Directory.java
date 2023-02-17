package oscars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/** Define the directories to be used and how to write and clean up those directories */
@SuppressWarnings("serial")
public class Directory extends File {
    public static final Directory CURRENT = new Directory(".");

    public static final Directory CATEGORY = new Directory("category");

    public static final Directory PLAYER = new Directory("player");

    public static final Directory RANK = new Directory("rank");

    private static final Instant START = Instant.now();

    private Directory(String inPathname) {
        super(inPathname);
        if (!exists())
            mkdir();
    }

    /** Write the Element to an XML file in this directory */
    public void writeDocument(Element inElement, String inXMLFile, String inXSLFile)
            throws IOException {
        Document xmlDocument = new Document();
        if (inXSLFile != null)
            xmlDocument.addContent(new ProcessingInstruction("xml-stylesheet", Stream.of(
                    new String[][] { { "type", "text/xsl" }, { "href", "../xsl/" + inXSLFile } })
                    .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]))));
        try (Writer writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(this + "/" + inXMLFile), "UTF-8"))) {
            new XMLOutputter(Format.getPrettyFormat()).output(
                    xmlDocument.addContent(new Comment("OSCARS website created by Scott McDonald"))
                            .addContent(inElement),
                    writer);
        }
    }

    /** Delete any files in this directory that haven't been modified since the program started */
    public void cleanUp() throws IOException {
        for (File file : listFiles())
            if (Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime()
                    .toInstant().isBefore(START))
                file.delete();
    }

    /** Delete all charts we don't want to keep */
    public void cleanUpCharts(Stream<String> inChartsToKeep) {
        Set<String> chartsToKeep = inChartsToKeep.collect(Collectors.toSet());
        for (File file : listFiles())
            if (file.getName().endsWith(".png") && !chartsToKeep.contains(file.getName()))
                file.delete();
    }
}