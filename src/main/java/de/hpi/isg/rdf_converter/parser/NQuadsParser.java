package de.hpi.isg.rdf_converter.parser;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses an NQuads file.
 * 
 * @author Sebastian Kruse
 */
public class NQuadsParser extends AbstractRDFParser {

    static final Logger LOGGER = LoggerFactory.getLogger(NQuadsParser.class);

    /**
     * @see {@link AbstractRDFParser#AbstractRDFParser()}
     */
    public NQuadsParser() {
        super();
    }
    
    /**
     * Creates a new instance with the given stream as input.
     */
    public NQuadsParser(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public String[] parse(String line) {
        if (line == null) {
            return null;
        }

        // Find first, second, last space.
        int firstSpacePos = line.indexOf(' ');
        int secondSpacePos = line.indexOf(' ', firstSpacePos + 1);
        int lastSpacePos = line.lastIndexOf(' ');
        int secondToLastSpacePos = line.lastIndexOf(' ', lastSpacePos - 1);
        return new String[] {
                line.substring(0, firstSpacePos),
                line.substring(firstSpacePos + 1, secondSpacePos),
                line.substring(secondSpacePos + 1, secondToLastSpacePos)
        };
    }

}
