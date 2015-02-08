package editor;

import java.io.*;

/**
 * @author Vsevolod Kosulnikov
 */
public class Application {
    private Application() {}

    public void run() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String currentLine = br.readLine();
            int dictionaryLength;
            try {
                dictionaryLength = Integer.parseInt(currentLine);
            } catch (NumberFormatException e) {
                throw new IOException("Cannot resolve dictionary length N, trying to parse \'"
                        +currentLine+"\'");
            }
            Dictionary dictionary = new Dictionary(dictionaryLength);
            for (int line = 0; line < dictionaryLength; ++line) {
                currentLine = br.readLine();
                String[] wordAndFrequency = currentLine.split(" ");
                if (wordAndFrequency.length != 2) {
                    throw new IOException("Cannot resolve word at "+line+" position: " +
                            "it contains more that 2 columns");
                }
                try {
                    dictionary.addWord(wordAndFrequency[0], Integer.parseInt(wordAndFrequency[1]));
                } catch (NumberFormatException e) {
                    throw new IOException("Cannot resolve word at "+line+" position: " +
                            "it has non-numeric frequency value ("+wordAndFrequency[1]+")");
                }
            }

            dictionary.prepareForWork();

            int queriesCount = Integer.parseInt(br.readLine());
            for (int query = 0; query<queriesCount; ++query) {
                dictionary.getSelection(br.readLine());
            }
        }
    }

    public static void main(String[] args) {
//        long timeMillis = -System.currentTimeMillis();
        try {
            Application app = new Application();
            app.run();
        } catch (Throwable e) {
            System.err.println("Oops! Exception occurred, program will be terminated.");
            e.printStackTrace();
            System.exit(1);
        }

//        timeMillis += System.currentTimeMillis();
//        System.out.println("Total executing time: "+(timeMillis/1000.0)+"s");
        System.exit(0);
    }
}
