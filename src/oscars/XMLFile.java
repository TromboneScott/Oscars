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

/** An XML file - Immutable (though the file system itself can change) */
@SuppressWarnings("serial")
public final class XMLFile extends File {
    public XMLFile(Directory inDirectory, String inFilename) {
        super(inDirectory, inFilename);
    }

    /** Get the root element of this XML file or empty if file doesn't exist */
    public Optional<Element> read() throws JDOMException, IOException {
        return exists() ? Optional.of(new SAXBuilder().build(this).getRootElement())
                : Optional.empty();
    }

    /** Write the given root element to this XML file */
    public void write(Element inRootElement) throws IOException {
        try (PrintWriter writer = new PrintWriter(this, StandardCharsets.UTF_8.name())) {
            String name = inRootElement.getName();
            new XMLOutputter(Format.getPrettyFormat()).output(
                    new Document(inRootElement, new DocType(name, "../dtd/" + name + ".dtd"))
                            .addContent(0, new Comment("OSCARS website created by Scott McDonald"))
                            .addContent(0, new ProcessingInstruction("xml-stylesheet", ImmutableMap
                                    .of("type", "text/xsl", "href", "../xsl/" + name + ".xsl"))),
                    writer);
        }
    }
}