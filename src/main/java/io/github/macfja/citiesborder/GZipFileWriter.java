package io.github.macfja.citiesborder;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

/**
 * GZipFileWriter class.
 * Write GZipped string into a file
 *
 * @author MacFJA
 */
public class GZipFileWriter implements Closeable {
    /**
     * The final writer.
     * It encapsulate a GZip writer that encapsulate a FileStream
     */
    protected BufferedWriter writer;

    /**
     * The constructor.
     *
     * @param path The path to the file to write
     * @throws IOException if an error occurs during the file opening
     */
    public GZipFileWriter(String path, boolean append) throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path, append))));
    }

    /**
     * Write data into the file.
     *
     * @param data The uncompressed data to write
     * @throws IOException if an error occurs during the writing
     */
    public void write(String data) throws IOException {
        writer.write(data);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
