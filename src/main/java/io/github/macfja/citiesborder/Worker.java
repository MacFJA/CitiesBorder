package io.github.macfja.citiesborder;

import org.openstreetmap.osmosis.core.Osmosis;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * Class Worker.
 *
 * @author MacFJA
 */
public final class Worker {
    /**
     * Protect class creation
     */
    private Worker() {
        throw new RuntimeException("Can not be instantiate");
    }

    /**
     * Run Osmosis to transform the heavy pbf file into a less heavy and more relevant Xml file
     *
     * @param outputPath          Where to generate the file
     * @param inputPath           The OpenStreetMap PBF file
     * @param administrationLevel The administration level to extract
     * @throws RuntimeException if All required Osmosis plugin are not available
     */
    public static void runOsmosis(String outputPath, String inputPath, int administrationLevel) throws RuntimeException {
        if (!validateDependencies(new String[]{
                "crosby.binary.osmosis.BinaryPluginLoader", // PBF OSM plugin
                "org.openstreetmap.osmosis.tagfilter.TagFilterPluginLoader", // Tag Filter OSM plugin
                "org.openstreetmap.osmosis.xml.XmlPluginLoader"// XML OSM plugin
        })) {
            throw new RuntimeException("Some Osmosis plugin are missing");
        }
        (new File(outputPath)).getParentFile().mkdirs();

        String[] args = new String[]{
                "--read-pbf",
                "file=" + inputPath,
                "--tf",
                "accept-relation",
                "admin_level=" + administrationLevel,
                "--tf",
                "accept-relation",
                "ref:INSEE=*",
                "--used-way",
                "--used-node",
                "--write-xml",
                outputPath
        };
        Osmosis.run(args);
    }

    /**
     * Transform the transformed Osmosis Xml file into a CitiesBorder file
     *
     * @param inputPath  The path use on Osmosis output
     * @param outputPath Where to generate the file
     * @param append     Indicate if the data must be add to an existing file (if {@code false} the file will be emptied before execution)
     * @throws ParserConfigurationException if a parser cannot be created which satisfies the requested configuration.
     * @throws IllegalArgumentException     If the File object is null.
     * @throws IOException                  If any IO errors occur.
     * @throws SAXException                 If any SAX errors occur during processing.
     */
    public static void runBuildCitiesBorderFile(String inputPath, String outputPath, boolean append) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

        GZipFileWriter writer = new GZipFileWriter(outputPath, append);

        XmlHandler handler = new XmlHandler(writer);
        parser.parse(new File(inputPath), handler);
        handler.clear();
        writer.close();
    }

    /**
     * Search a city into a CitiesBorder file
     *
     * @param inputPath The path to the CitiesBorder file
     * @param name      The name of the city to search
     * @return The list of GPS position (or an empty list if the city is not found)
     * @throws IOException if an error occur while reading the file
     */
    public static String[] search(String inputPath, String name) throws IOException {
        InputReader reader = new InputReader(inputPath);

        String readName;
        while ((readName = reader.readEntry()) != null) {
            if (readName.equals(name)) {
                String[] result = reader.readData().split("\n");
                reader.close();
                return result;
            }
        }
        reader.close();
        return new String[0];
    }

    /**
     * Check if a class exist
     *
     * @param className The FQCN to check
     * @return {@code true} if the class exist
     */
    protected static boolean validateDependency(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if all classes exist
     *
     * @param classesName The list of all classes to check
     * @return {@code false} if any of the classes is missing
     */
    protected static boolean validateDependencies(String[] classesName) {
        for (String className : classesName) {
            if (!validateDependency(className)) {
                return false;
            }
        }
        return true;
    }
}
