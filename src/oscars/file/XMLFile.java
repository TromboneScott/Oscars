package oscars.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

/** An XML file - Immutable (though the file system itself can change) */
@SuppressWarnings("serial")
public final class XMLFile extends File {
    private static final XMLFile DEFINITIONS_FILE = new XMLFile(Directory.DATA, "definitions.xml");

    public XMLFile(Directory inDirectory, String inFilename) {
        super(Objects.requireNonNull(inDirectory), inFilename);
    }

    /** Get the root element of this XML file or empty if file doesn't exist */
    public Optional<Element> read() throws JDOMException, IOException {
        return Optional.ofNullable(exists() ? new SAXBuilder().build(this).getRootElement() : null);
    }

    /** Write the given root element to this XML file */
    public void write(Element inRootElement) throws IOException {
        Document document = new Document()
                .addContent(new ProcessingInstruction("xml-stylesheet",
                        ImmutableMap.of("type", "text/xsl", "href",
                                "../xsl/" + inRootElement.getName() + ".xsl")))
                .addContent(new Comment("OSCARS website created by Scott McDonald"))
                .addContent(inRootElement.clone().addContent(0,
                        new Element("script", "http://www.w3.org/1999/xhtml").setAttribute("src",
                                "/js/xslt-polyfill.min.js")));
        try (Writer writer = Files.newWriter(this, StandardCharsets.UTF_8)) {
            new XMLOutputter(Format.getPrettyFormat()).output(document, writer);
        }
    }

    /** Read the root element of the definitions file which is required to exist */
    public static Element readDefinitionsFile() {
        try {
            return DEFINITIONS_FILE.read().orElseThrow(FileNotFoundException::new);
        } catch (Exception e) {
            throw new RuntimeException("Error reading definitions file: " + DEFINITIONS_FILE, e);
        }
    }
}