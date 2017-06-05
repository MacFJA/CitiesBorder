package io.github.macfja.citiesborder;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Main.
 * The CLI entry point
 *
 * @author MacFJA
 */
public class Main {
    /**
     * The OSM PBF raw file path
     */
    @Argument(alias = "i")
    public String input;
    /**
     * The CitiesBorder file path
     */
    @Argument(alias = "o")
    public String output;
    /**
     * The level of administration relation to keep
     */
    @Argument(alias = "l")
    public Integer level = 8;
    /**
     * The city name to search
     */
    @Argument(alias = "s")
    public String search;
    /**
     * If specified, the generation of CitiesBorder file will be skip
     */
    @Argument(value = "search-only", alias = "S")
    public boolean searchOnly = false;

    /**
     * The path where the result of the Osmosis will be put
     */
    protected String tmpPath;
    /**
     * The application logger
     */
    protected Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Main app = new Main();
        if (!Worker.validateDependency("com.sampullara.cli.Args")) {
            app.logger.log(Level.SEVERE, "There are missing dependencies for the application to run.");
            System.exit(1);
            return;
        }
        app.run(args);
    }

    /**
     * Constructor.
     */
    public Main() {
        tmpPath = System.getProperty("java.io.tmpdir") + File.separator + "io.github.macfja.cities-border" + File.separator + "osmosis.xml";
    }

    /**
     * Run all action according to the args
     * @param args List of cli arguments
     */
    public void run(String[] args) {
        Args.parseOrExit(this, args);

        if (input != null) {
            logger.log(Level.INFO, "Start Osmosis transformation");
            try {
                Worker.runOsmosis(tmpPath, input, level);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, e.getMessage());
                logger.log(Level.INFO, "Osmosis transformation aborted.");
            }
            logger.log(Level.INFO, "End Osmosis transformation");
        }

        if (output != null && !searchOnly) {
            logger.log(Level.INFO, "Start file generation");
            try {
                Worker.runBuildCitiesBorderFile(tmpPath, output, false);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
            logger.log(Level.INFO, "End file generation");
        }

        if (search != null && output != null) {
            logger.log(Level.INFO, "Start border searching");
            try {
                System.out.println(Arrays.asList(Worker.search(output, search)));
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
            logger.log(Level.INFO, "End border searching");
        }
    }
}
