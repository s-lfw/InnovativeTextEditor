package editor;

import java.io.*;
import java.util.Arrays;

/**
 * Date: 09.02.15
 *
 * @author Vsevolod Kosulnikov
 */
public class PlainEditor {
    private static Word[] words;
    private static PrintStream out;

    public static void main(String[] args) {
        try {
            long t = -System.currentTimeMillis();
            File dictionaryFile = new File(System.getProperty("user.dir"), "test.in");
            File resultFile = new File(System.getProperty("user.dir"), "example_test.out");
            out = new PrintStream(resultFile);
            BufferedReader reader = new BufferedReader(new FileReader(dictionaryFile));
            String currentLine = reader.readLine();
            int dictionaryLength;
            try {
                dictionaryLength = Integer.parseInt(currentLine);
            } catch (NumberFormatException e) {
                throw new IOException("Cannot resolve dictionary length N, trying to parse \'"
                        +currentLine+"\'");
            }
            words = new Word[dictionaryLength];
            for (int line = 0; line < dictionaryLength; ++line) {
                currentLine = reader.readLine();
                String[] wordAndFrequency = currentLine.split(" ");
                if (wordAndFrequency.length != 2) {
                    throw new IOException("Cannot resolve word at "+line+" position: " +
                            "it contains more that 2 columns");
                }
                try {
                    words[line] = new Word(wordAndFrequency[0], Integer.parseInt(wordAndFrequency[1]));
                } catch (NumberFormatException e) {
                    throw new IOException("Cannot resolve word at "+line+" position: " +
                            "it has non-numeric frequency value ("+wordAndFrequency[1]+")");
                }
            }

            Arrays.sort(words, Word.getFrequencyComparator());

            int queriesCount;
            currentLine = reader.readLine();
            try {
                queriesCount = Integer.parseInt(currentLine);
            } catch (NumberFormatException e) {
                throw new IOException("Cannot resolve queries count M, trying to parse \'"
                        +currentLine+"\'");
            }
            for (int query = 0; query<queriesCount; ++query) {
                if (query%500==0) {
                    System.out.println(query + " strings processed");
                }
                findWords(reader.readLine());
            }
            System.out.println("Time spent: "+((t+System.currentTimeMillis())/1000.0) + "s");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void findWords(String prefix) {
        int foundWords = 0;
        for (Word w : words) {
            if (w.word.startsWith(prefix)) {
                out.println(w.word);
                ++foundWords;
            }
            if (foundWords==10) {
                break;
            }
        }
    }
}
