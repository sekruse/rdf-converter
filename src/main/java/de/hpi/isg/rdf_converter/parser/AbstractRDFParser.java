/***********************************************************************************************************************
 * Copyright (C) 2014 by Sebastian Kruse
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/
package de.hpi.isg.rdf_converter.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract subclass for RDF parsers.
 * 
 * @author Sebastian Kruse
 */
public abstract class AbstractRDFParser implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRDFParser.class);

    protected final BufferedReader reader;

    /**
     * Creates a new parser for the given input format.
     * 
     * @param inputStream
     *        is the input stream on which the parser shall work
     * @param inputFormat
     *        is the input format ("nt", "nq", ...)
     * @return the parser
     */
    public static AbstractRDFParser createRdfParser(InputStream inputStream, String inputFormat) {
        switch (inputFormat) {
        case "nt":
            return inputStream == null ? new NTriplesParser() : new NTriplesParser(inputStream);
        case "nq":
            return inputStream == null ? new NQuadsParser() : new NQuadsParser(inputStream);
        default:
            throw new IllegalArgumentException("Unknown input format: " + inputFormat);
        }
    }

    /**
     * @param reader
     *        is the origin of RDF data to be parsed
     */
    public AbstractRDFParser(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Creates a reader that wont be able to call {@link #readNextTriple()}.
     */
    public AbstractRDFParser() {
        this.reader = null;
    }

    /**
     * @param inputStream
     *        is the stream from which RDF tuples shall be parsed
     * @throws UnsupportedEncodingException
     */
    public AbstractRDFParser(InputStream inputStream) {
        this(new BufferedReader(createReader(inputStream)));
    }

    /**
     * Creates a {@link Reader} that wraps the given input stream using UTF-8 encoding.
     */
    private static Reader createReader(InputStream inputStream) {
        try {
            return new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported.", e);
        }
    }

    public String[] readNextTriple() throws IOException {
        if (this.reader == null) {
            throw new IllegalStateException("Parser was not given an input stream to read from.");
        }

        String line;
        do {
            line = this.reader.readLine();
            if (line == null) {
                return null;
            }
        } while (line.trim().isEmpty() || line.startsWith("#"));

        String[] triple;
        triple = parse(line);

        return triple;
    }

    /**
     * Parse the RDF triple on the given line.
     * 
     * @return the parsed triple from the line or {@code null} if the input line is {@code null}
     */
    abstract public String[] parse(String line);

    @Override
    public void close() throws IOException {
        try {
            this.reader.close();
        } catch (IOException e) {
            LOGGER.error("Could not close reader.", e);
        }
    }

}
