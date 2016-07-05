package de.hpi.isg.rdf_converter.parser;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a NTriple file.
 * 
 * @author Sebastian Kruse
 */
public class NTriplesParser extends AbstractRDFParser {

    static final Logger LOGGER = LoggerFactory.getLogger(NTriplesParser.class);

    public static final char DEFAULT_SEPARATOR = ' ';

    private final char separator;

    /**
     * @see {@link AbstractRDFParser#AbstractRDFParser()}
     */
    public NTriplesParser() {
        this(' ');
    }

    /**
     * Creates a new parser.
     * @param separator is the character that separates subject, predicate, and object.
     */
    public NTriplesParser(char separator) {
        this.separator = separator;
    }

    public NTriplesParser(InputStream inputStream) {
        super(inputStream);
        this.separator = DEFAULT_SEPARATOR;
    }

    @Override
    public String[] parse(String line) {
        if (line == null) {
            return null;
        }

        // Find first, second, last space.
        int firstSpacePos = line.indexOf(this.separator);
        int secondSpacePos = line.indexOf(this.separator, firstSpacePos + 1);

        // Deal with arbitrary many spaces in the en.
        int lastSpacePos = line.lastIndexOf('.');
        while (line.charAt(lastSpacePos - 1) == this.separator) {
            lastSpacePos--;
        }

        return new String[] {
                line.substring(0, firstSpacePos),
                line.substring(firstSpacePos + 1, secondSpacePos),
                line.substring(secondSpacePos + 1, lastSpacePos)
        };
    }

}
