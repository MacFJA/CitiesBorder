package io.github.macfja.citiesborder;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * InputReader class.
 * Read the CitiesBorder generated file
 *
 * @author MacFJA
 */
public class InputReader implements Closeable {
    /**
     * The final reader.
     * It encapsulate a GZIP reader that encapsulate a FileStream
     */
    protected BufferedReader reader;
    /**
     * The city that is currently read.
     * Can be {@code null} (at start and if the data of a city is already read)
     */
    protected Map<Key, Object> currentLine;

    /**
     * The constructor.
     *
     * @param path The path to the file to read
     * @throws IOException if an error occurs during the file opening
     */
    public InputReader(String path) throws IOException {
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
    }

    /**
     * Parse the "header" city line.
     * The line format is:
     * <pre>"{" + NameOfTheCity +  "}:" + NumberOfDataChar</pre>
     *
     * @param line The line to parse
     * @return A map that contains info (name of the city + number of data char)
     */
    protected Map<Key, Object> parseLine(String line) {
        Map<Key, Object> info = new HashMap<>();
        String name = line.substring(line.indexOf("{") + 1, line.lastIndexOf("}"));
        Integer count = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1));

        info.put(Key.CityName, name);
        info.put(Key.DataCount, count);

        return info;
    }

    /**
     * Read the next city.
     *
     * @return The name of the city, or {@code null} if the end of the file is reach
     * @throws IOException if an error occurs during the reading
     */
    public String readEntry() throws IOException {
        if (currentLine == null) {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            currentLine = parseLine(line);
            return (String) currentLine.get(Key.CityName);
        } else {
            Integer dataSize = (Integer) currentLine.get(Key.DataCount);
            reader.skip(dataSize + 1);
            currentLine = null;
            return readEntry();
        }
    }

    /**
     * Read the data of the current city
     *
     * @return The data of the city, or {@code null} if the end of the file is reach or no city is read
     * @throws IOException if an error occurs during the reading
     */
    public String readData() throws IOException {
        if (currentLine == null) {
            return null;
        } else {
            Integer dataSize = (Integer) currentLine.get(Key.DataCount);
            char[] buffer = new char[dataSize];
            reader.read(buffer);
            return new String(buffer);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * List of possible key
     */
    protected enum Key {
        /**
         * The data associated to this key is the name of a city.
         */
        CityName,
        /**
         * The data associated to this key is the number of char of the data.
         */
        DataCount
    }
}
