package editor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vsevolod Kosulnikov
 */
public class Application {
    private final boolean debugMode;
    private String dictionaryFileName = "test.in";
    private File dictionaryFile;
    private Dictionary dictionary;
    private List<String> queries = new ArrayList<>();
    public Application() {
        this(false);
    }

    public Application(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void run() throws IOException {
        dictionaryFile = new File(System.getProperty("user.dir"), dictionaryFileName);
        System.out.println("Reading dictionary data and test selection from \""+
                dictionaryFile.getAbsolutePath()+"\"");
        initDictionary();
        System.out.println("Dictionary initialization is finished. Now working.");
        doWork();
    }

    private void initDictionary() throws IOException {
        long timeMillis = -System.currentTimeMillis();
        BufferedReader br = new BufferedReader(new FileReader(dictionaryFile));
        String currentLine = br.readLine();
        int dictionaryLength = 0;
        try {
            dictionaryLength = Integer.parseInt(currentLine);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("File "+dictionaryFile+" does not contain proper data"); //todo
        }
        dictionary = new Dictionary(dictionaryLength);
        for (int line = 0; line<dictionaryLength; ++line) {
            currentLine = br.readLine();
            String[] wordAndFrequency = currentLine.split(" ");
            if (wordAndFrequency.length!=2) {
                // text editors usually start line numeration from 1
                throw new IOException("Bad input file, line number: " + (line + 2));
            }
            try {
                dictionary.addWord(wordAndFrequency[0], Integer.parseInt(wordAndFrequency[1]));
            } catch (NumberFormatException e) {
                e.printStackTrace(); //todo
                throw new IOException(); //todo
            }
        }

        dictionary.prepareForWork();

        timeMillis += System.currentTimeMillis();
        System.out.println("Dictionary parsed in "+(timeMillis/1000.0)+"s");

        currentLine = br.readLine(); //todo
        Integer.parseInt(currentLine);
        while ((currentLine=br.readLine())!=null) {
            queries.add(currentLine);
        }
    }

    private void doWork() {
        long timeMillis = -System.currentTimeMillis();
        if (debugMode) {
            testLaunch();
        } else {
            actuallyDoWork();
        }
        timeMillis += System.currentTimeMillis();
        System.out.println("Working time: "+(timeMillis/1000.0)+"s");
    }

    private void actuallyDoWork() {
        for (String q : queries) {
            dictionary.getSelection(q);
//            for (String s : r) {
//                System.out.println(s);
//            }
//            System.out.println(Arrays.toString(dictionary.getSelection(q)));
        }
    }

    private void testLaunch() {
        dictionary.getSelection("ac");
    }

    public static void main(String[] args) {
        long timeMillis = -System.currentTimeMillis();
        try {
            Application app = new Application();
            app.run();
        } catch (Throwable e) {
            System.err.println("Oops! Exception occurred, program will be terminated.");
            e.printStackTrace();
            System.exit(1);
        }

        timeMillis += System.currentTimeMillis();
        System.out.println("Total executing time: "+(timeMillis/1000.0)+"s");
        System.exit(0);
    }
}
