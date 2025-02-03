package oscars;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.jdom2.Comment;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.google.common.collect.ImmutableMap;

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

    /** Write an XML file in this directory with the given element */
    public void write(String inXMLFile, Element inElement) throws IOException {
        try (PrintWriter writer = new PrintWriter(file(inXMLFile), StandardCharsets.UTF_8.name())) {
            String name = inElement.getName();
            new XMLOutputter(Format.getPrettyFormat()).output(
                    new Document(inElement, new DocType(name, "../dtd/" + name + ".dtd"))
                            .addContent(0, new Comment("OSCARS website created by Scott McDonald"))
                            .addContent(0, new ProcessingInstruction("xml-stylesheet", ImmutableMap
                                    .of("type", "text/xsl", "href", "../xsl/" + name + ".xsl"))),
                    writer);
        }
    }
}