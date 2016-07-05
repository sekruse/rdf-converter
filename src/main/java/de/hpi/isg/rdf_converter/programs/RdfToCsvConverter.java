package de.hpi.isg.rdf_converter.programs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import de.hpi.isg.rdf_converter.parser.AbstractRDFParser;

/**
 * Converts NTriple files into a single CSV file with three columns, one per subject, predicate, and object. The output
 * file is semicolon-separated and partially double-quoted.
 * 
 * @author Sebastian Kruse
 */
public class RdfToCsvConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdfToCsvConverter.class);

    private final Parameters parameters;

    public RdfToCsvConverter(Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Reads all the input N3 files and writes them in CSV format to the output file
     * 
     * @param inputFiles
     *        are N3 files to be parsed
     * @param writer2
     *        is a CSV file to be to
     * @throws IOException
     */
    public void convertAndMerge(Collection<File> inputFiles, ICsvListWriter writer) throws IOException {
        for (File inputFile : inputFiles) {
            LOGGER.info("Converting file {}.", inputFile);
            convert(new FileInputStream(inputFile), writer);
        }
    }

    /**
     * Converts the RDF data from the input stream and writers it to the writer
     * 
     * @param in
     *        is the inputStream containing the RDF data
     * @param writer
     *        is the writer that accepts the converted data
     * @throws IOException 
     */
    public void convert(InputStream in, ICsvListWriter writer) throws IOException {
        try (AbstractRDFParser parser = AbstractRDFParser.createRdfParser(in, this.parameters.inputFormat)) {
            forward(parser, writer);
        }
    }

    /**
     * Streams all the contents from the parser to the writer (while converting the file format).
     */
    private void forward(AbstractRDFParser parser, ICsvListWriter writer) throws IOException {
        String[] triple;
        while ((triple = parser.readNextTriple()) != null) {
            writer.write(triple);
        }
    }

    /**
     * @see Parameters
     */
    public static void main(String[] args) throws IOException {

        // Parse parameters.
        Parameters parameters = parseParameters(args);

        RdfToCsvConverter converter = new RdfToCsvConverter(parameters);

        // Create the output writer.
        try (ICsvListWriter writer = createCsvWriter(parameters)) {
            // Load input files (if any).
            if (parameters.input != null) {
                final Collection<File> inputFiles = gatherInputFiles(parameters);
                if (inputFiles.isEmpty()) {
                    LOGGER.error("No input files found.");
                    System.exit(2);
                }
                converter.convertAndMerge(inputFiles, writer);
            } else {
                converter.convert(System.in, writer);
            }

        } catch (Exception e) {
            LOGGER.error("Execution failed.", e);
        }

    }

    private static Collection<File> gatherInputFiles(Parameters parameters) throws IOException {
        final Collection<File> inputFiles = new LinkedList<>();
        File input = new File(parameters.input);
        if (input.isDirectory()) {
            Files.walkFileTree(Paths.get(input.toURI()), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    inputFiles.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            inputFiles.add(input);
        }
        return inputFiles;
    }

    /**
     * Creates a configured CSV writer for the output.
     */
    private static ICsvListWriter createCsvWriter(Parameters parameters) throws IOException {
        OutputStream outputStream = System.out;
        if (parameters.outputFile != null) {
            File outputFile = new File(parameters.outputFile);
            outputFile.getParentFile().mkdirs();
            outputStream = new FileOutputStream(outputFile);
        }
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        ICsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
        return csvWriter;
    }

    private static Parameters parseParameters(String[] args) {
        try {
            final Parameters parameters = new Parameters();
            new JCommander(parameters, args);
            return parameters;
        } catch (final ParameterException e) {
            LOGGER.error("Could not parse command line.", e);
            new JCommander(new Parameters()).usage();
            System.exit(1);
            throw new RuntimeException();
        }
    }

    public static class Parameters {

        @Parameter(names = { "-i", "--input" }, required = false)
        String input;

        @Parameter(names = { "-o", "--output-file" }, required = false)
        String outputFile;

        @Parameter(names = { "-f", "--input-format" }, required = false)
        String inputFormat = "nt";

    }
}
