package oscars;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/** Define the directories to be used and how to read, write and clean up those directories */
@SuppressWarnings("serial")
public class Directory extends File {
    public static final Directory DATA = new Directory("data");

    public static final Directory CATEGORY = new Directory("category");

    public static final Directory PLAYER = new Directory("player");

    private static final Instant START = Instant.now();

    private Directory(String inPathname) {
        super(inPathname);
        if (!exists())
            mkdir();
    }

    /** Get the root element of the XML file or null if file doesn't exist */
    public Element getRootElement(String inXMLFile) throws IOException {
        File xmlFile = new File(this, inXMLFile);
        if (!xmlFile.exists())
            return null;
        try {
            return new SAXBuilder().build(xmlFile).getRootElement();
        } catch (JDOMException e) {
            throw new IOException("ERROR: Unable to read xml file: " + inXMLFile, e);
        }
    }

    /** Write the Element to an XML file in this directory */
    public void write(Element inElement, String inXMLFile, String inXSLFile) throws IOException {
        Document document = new Document();
        if (inXSLFile != null)
            document.addContent(new ProcessingInstruction("xml-stylesheet", Stream.of(
                    new String[][] { { "type", "text/xsl" }, { "href", "../xsl/" + inXSLFile } })
                    .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]))));
        document.addContent(new Comment("OSCARS website created by Scott McDonald"));
        document.addContent(inElement);
        try (Writer writer = new PrintWriter(new File(this, inXMLFile),
                StandardCharsets.UTF_8.name())) {
            new XMLOutputter(Format.getPrettyFormat()).output(document, writer);
        }
    }

    /** Delete all files in this directory that haven't been modified since the program started */
    public void cleanUp() throws IOException {
        for (File file : listFiles())
            if (Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime()
                    .toInstant().isBefore(START))
                file.delete();
    }
}