import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Takes a list of terms and definitions from an input file, outputs them to
 * HTML files in a specified folder, including a top-level index file with links
 * to individual term pages with definitions of the terms.
 *
 * @author Hunt Wagner.851
 *
 */
public final class Glossary {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private Glossary() {
    }

    /**
     * Compare {@code String}s in lexicographic order.
     */
    private static class StringLT implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Takes all the terms and definitions inside {@code fileName} and maps them
     * in {@code glossaryMap} as well as adds the terms to {@code glossaryTerms}
     * .
     *
     * @param fileName
     *            the {@code String} of the input file
     * @param glossaryMap
     *            the {@code Map} to be populated
     * @param glossaryTerms
     *            the {@code Queue} to be filled with terms for easy sorting
     * @ensures glossaryMap = entries(fileName)
     * @ensures glossaryTerms = entries(fileName)
     */
    public static void mapGlossary(String fileName,
            Map<String, String> glossaryMap, Queue<String> glossaryTerms) {
        assert fileName != null : "Violation of: fileName is not null";
        assert glossaryMap != null : "Violation of: glossaryMap is not null";
        assert glossaryTerms != null : "Violation of: glossaryTerms is not null";
        SimpleReader inFile = new SimpleReader1L(fileName);
        // Iterate through input file
        while (!inFile.atEOS()) {
            // Utilize structure of document to map terms and definitions
            String term = inFile.nextLine();
            String definition = inFile.nextLine();
            if (!inFile.atEOS()) {
                // Handle multiline definitions and the blank line between terms
                boolean done = false;
                String nextLine = inFile.nextLine();
                while (!done && !inFile.atEOS()) {
                    if (nextLine.equals("")) {
                        done = true;
                    } else {
                        // Space added for line breaks in input file
                        definition += " " + nextLine;
                        nextLine = inFile.nextLine();
                    }
                }
            }
            // Add terms to queue, and map terms and definitions.
            glossaryTerms.enqueue(term);
            glossaryMap.add(term, definition);
        }
        // Close input stream
        inFile.close();
    }

    /**
     * Creates an individual html page for the {@code term} inside the correct
     * {@code folder}, populating the page with the definition from
     * {@code glossaryMap}.
     *
     * @param glossaryMap
     *            The {@code Map} to get term definitions to input into file
     * @param term
     *            The {@code String} of the individual term being added
     * @param folder
     *            The {@code String} of the folder to add the file to
     */
    public static void createTermFile(Map<String, String> glossaryMap,
            String term, String folder) {
        String fileName = folder + term + ".html";
        // Open output stream
        SimpleWriter output = new SimpleWriter1L(fileName);
        // Insert term into html
        String page = "<html>\n<head>\n<title>" + term
                + "</title>\n</head>\n<body>\n<h2><b><i><font color=\"red\">"
                + term + "</font></i></b></h2>\n<blockquote>"
                + glossaryMap.value(term) + "</blockquote><hr />"
                + "\n<p>Return to <a href=\"index.html\">index</a>.</p>\n"
                + "</body>\n</html>";
        // Write html to file
        output.println(page);
        // Close output stream
        output.close();
    }

    /**
     * Iterates through {@code glossaryTerms} to build the list of individual
     * term pages within the specified {@code folder}. {@code glssaryMap} is
     * passed to the creatTermFile function to automate the entire process.
     *
     * @param glossaryMap
     *            {@code Map} of all terms and definitions
     * @param glossaryTerms
     *            {@code Queue} of sorted terms to be iterated through
     * @param folder
     *            {@code String} of the desired folder
     * @return the completed list of list elements of terms from
     *         {@code glossaryTerms}
     */
    public static String createTermList(Map<String, String> glossaryMap,
            Queue<String> glossaryTerms, String folder) {
        // Get individual term from queue
        String term = glossaryTerms.dequeue();
        // Build list element
        String list = "<li><a href=\"" + term + ".html\">" + term
                + "</a></li>\n";
        // Call function to create individual page for term
        createTermFile(glossaryMap, term, folder);
        if (glossaryTerms.length() > 0) {
            // Recursively call to build the rest of list elements and pages
            list += createTermList(glossaryMap, glossaryTerms, folder);
        }
        // Return the finished list of list elements
        return list;
    }

    /**
     * Creates index page in the desired {@code folder}, including a list of
     * {@code glossaryTerms} containing links to individual pages of term
     * definitions from {@code glossaryMap}.
     *
     * @param folder
     *            The {@code String} of the specified folder
     * @param glossaryMap
     *            {@code Map} of all terms and definitions
     * @param glossaryTerms
     *            {@code Queue} of sorted terms comprising the list
     */
    public static void createFiles(String folder,
            Map<String, String> glossaryMap, Queue<String> glossaryTerms) {
        assert folder != null : "Violation of: folder is not null";
        String fileName = folder + "index.html";
        // Open output stream
        SimpleWriter output = new SimpleWriter1L(fileName);
        // Build header of html
        String header = "<html>\n<head>\n<title>Glossary</title>\n"
                + "<style>li{list-style: square;}</style>\n</head>\n<body>\n"
                + "<h2>Glossary</h2>\n<hr /><h3>Index</h3>\n<ul>";
        // Build footer of html
        String footer = "</ul>\n</body>\n</html>";
        // Build list of term links
        header += createTermList(glossaryMap, glossaryTerms, folder) + footer;
        // Write built html to file
        output.println(header);
        output.close();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        // Set up necessary variables
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        Map<String, String> glossaryMap = new Map1L<String, String>();
        Queue<String> glossaryKeys = new Queue1L<String>();
        Comparator<String> sorter = new StringLT();

        // Get output folder and input text file
        out.print("Input output folder name (must already exist): ");
        String outputFolder = in.nextLine();
        if (outputFolder.length() > 0
                && outputFolder.charAt(outputFolder.length() - 1) != '/') {
            outputFolder += "/";
        }
        out.print("Input glossary text file: ");
        String inputText = in.nextLine();

        // Use input text file to map glossary terms => definitions, then sort them
        mapGlossary(inputText, glossaryMap, glossaryKeys);
        glossaryKeys.sort(sorter);

        // Build the index file, and simultaneously each term file as we go.
        createFiles(outputFolder, glossaryMap, glossaryKeys);
        // Notify user program has completed successfully
        out.println("Complete.");

        // Close streams
        out.close();
        in.close();
    }

}
