package oscars;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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

/**
 * Define the directories to be used and how to read and write in those directories - Immutable
 * (though the file system itself can change)
 */
@SuppressWarnings("serial")
public final class Directory extends File {
    public static final Directory DATA = new Directory("data");

    public static final Directory CATEGORY = new Directory("category");

    public static final Directory PLAYER = new Directory("player");

    private Directory(String inPathname) {
        super(inPathname);
        if (!exists())
            mkdir();
    }

    /** Get a File object in this Directory */
    public File file(String inFilename) {
        return new File(this, inFilename);
    }

    /** Get the root element of the XML file or empty if file doesn't exist */
    public Optional<Element> getRootElement(String inXMLFile) throws IOException {
        File xmlFile = file(inXMLFile);
        try {
            return xmlFile.exists() ? Optional.of(new SAXBuilder().build(xmlFile).getRootElement())
                    : Optional.empty();
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
        try (Writer writer = new PrintWriter(file(inXMLFile), StandardCharsets.UTF_8.name())) {
            new XMLOutputter(Format.getPrettyFormat()).output(document, writer);
        }
    }
}