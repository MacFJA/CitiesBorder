package io.github.macfja.citiesborder;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class XmlHandler.
 * Read the Osmosis generated XML and create CitiesBorder file.
 *
 * @author MacFJA
 */
public class XmlHandler extends DefaultHandler {
    /**
     * The map that contains all nodes (id => GPS position).
     */
    protected final Map<Long, String> nodes = new HashMap<>();
    /**
     * The map that contains all ways (id => Way (List of GPS position))
     */
    protected final Map<Long, Way> ways = new HashMap<>();
    /**
     * The file writer to use
     */
    protected GZipFileWriter writer;
    /**
     * The current way. (store data of the currently read way)
     * Can be {@code null} if the current read element is not a way or in a way element.
     */
    protected Way currentWay;
    /**
     * The current relation. (store data of the currently read relation)
     * Can be {@code null} if the current read element is not a relation or in a relation element.
     */
    protected Relation currentRelation;

    /**
     * The Constructor.
     *
     * @param writer The GZip writer to use.
     */
    public XmlHandler(GZipFileWriter writer) {
        this.writer = writer;
    }

    /**
     * Set the writer to use
     *
     * @param writer The writer
     */
    public void setWriter(GZipFileWriter writer) {
        this.writer = writer;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        /*
         * Test what is the name of the current element.
         *  - For <node> we keep "id", "lat" and "lon"
         *  - For <way> we keep "id", create new currentWay
         *  - For <relation> we create new currentRelation
         *  - For <way><nd> we add to currentWay the stored <node> data
         *  - For <way><member type=way> we add to the currentRelation the stored <relation> data
         *  - For <way><member type=node> we do nothing (center point is not relevant)
         *  - For <way><tag k=name> we keep "v"
         * The rest is ignored
         */
        if (qName.equals("node")) {
            nodes.put(Long.parseLong(attributes.getValue("id")), attributes.getValue("lat") + " " + attributes.getValue("lon"));
        } else if (qName.equals("nd")) {
            currentWay.addNode(nodes.get(Long.parseLong(attributes.getValue("ref"))));
        } else if (qName.equals("way")) {
            currentWay = new Way();
            currentWay.id = Long.parseLong(attributes.getValue("id"));
        } else if (qName.equals(("relation"))) {
            currentRelation = new Relation();
        } else if (qName.equals("member")) {
            if (attributes.getValue("type").equals("way")) {
                currentRelation.addWay(ways.get(Long.parseLong(attributes.getValue("ref"))));
            }
        } else if (qName.equals("tag") && currentRelation != null && attributes.getValue("k").equals("name")) {
            currentRelation.name = attributes.getValue("v");
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            ways.put(currentWay.id, currentWay);
            currentWay = null;
        } else if (qName.equals("relation")) {
            writeCurrentRelation();
            currentRelation = null;
        }
    }

    /**
     * Write the content of the current relation into the file.
     */
    protected void writeCurrentRelation() {
        try {
            writer.write(currentRelation.toString());
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Clear all storage
     */
    public void clear() {
        currentRelation = null;
        currentWay = null;
        nodes.clear();
        ways.clear();
    }

    /**
     * A representation of an OSM way
     */
    protected class Way {
        /**
         * The id of the way.
         * Useful to search the way as it's referenced by its id
         */
        Long id;
        /**
         * The list of GPS position
         */
        String nodes = "";

        /**
         * add a node (its GPS position)
         *
         * @param node The GPS position of a node
         */
        public void addNode(String node) {
            if (node == null) {
                return;
            }
            if (nodes.contains(node)) {
                return;
            }
            nodes = nodes + '\n' + node;
        }

        @Override
        public String toString() {
            if (nodes.length() == 0) {
                return "";
            }
            String result = nodes;
            if (result.startsWith("\n")) {
                result = result.substring(1);
            }
            return result;
        }
    }

    /**
     * A representation of an OSM relation
     */
    protected class Relation {
        /**
         * The list of the way (that are a list of GPS position)
         */
        final StringBuilder ways = new StringBuilder();
        /**
         * The name of the relation, which is the name of the city
         */
        String name;

        /**
         * Add a way
         *
         * @param way The way to add
         */
        public void addWay(Way way) {
            if (way == null) {
                return;
            }
            ways.append('\n').append(way);
        }

        /**
         * Get the list of GPS position.
         * Remove the first "\n" is present
         *
         * @return The list of GPS position
         */
        private String getRelationContent() {
            String result = ways.toString();
            if (result.startsWith("\n")) {
                result = result.substring(1);
            }
            return result;
        }

        @Override
        public String toString() {
            String content = getRelationContent();
            long count = content.length();
            return "{" + name + "}:" + Long.toString(count) + "\n" + content + "\n";
        }
    }
}
