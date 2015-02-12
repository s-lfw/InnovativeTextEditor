package editor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vsevolod Kosulnikov
 */
public class Dictionary {
    /**
     * Maximum allowed prompts for one prefix
     */
    private static final int MAX_SELECTION_LENGTH = 10;

    /**
     * Performance parameter. The more this parameter, the more time indices building will require,
     * but the less time will be spent on each query
     */
    private static final int INDICES_DEPTH = 4;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private int indicesDepth = 0;

    private final Word[] words;
    private Index baseIndex;

    public static Dictionary initDictionary(File dictionaryFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(dictionaryFile))) {
            return initDictionary(reader);
        }
    }
    public static Dictionary initDictionary(BufferedReader reader) throws IOException {
        // initializing dictionary
        String currentLine = reader.readLine();
        int dictionaryLength;
        try {
            dictionaryLength = Integer.parseInt(currentLine);
        } catch (NumberFormatException e) {
            throw new IOException("Cannot resolve dictionary length N, trying to parse \'"
                    +currentLine+"\'");
        }
        Dictionary dictionary = new Dictionary(dictionaryLength);
        for (int line = 0; line < dictionaryLength; ++line) {
            currentLine = reader.readLine();
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

        // building dictionary indices
        dictionary.prepareForWork();
        return dictionary;
    }

    public Dictionary(int initialLength) {
        if (initialLength<=0) {
            throw new IllegalArgumentException("Cannot create dictionary with non-positive length");
        }
        words = new Word[initialLength];
    }

    private int addingIndex = 0;
    public void addWord(String word, int frequency) {
        if (addingIndex>=words.length) {
            System.err.println("Dictionary is packed already, cannot add more words");
            return;
        }
        if (word==null || word.isEmpty()) {
            System.err.println("Empty word skipped");
            return;
        }
        if (frequency<1) {
            System.err.println("Non-used word skipped (frequency <= 0");
            return;
        }
        words[addingIndex] = new Word(word.toLowerCase(), frequency);
        // here is determined maximum length of dictionary words.
        // For details see prepareForWork() method
        indicesDepth = Math.max(indicesDepth, word.length());
        ++addingIndex;
    }

    /**
     * Building indices for dictionary
     */
    public void prepareForWork() {
        // If this value is less than INDICES_DEPTH, then real indices depth must be
        // truncated, because there are explicitly no words longer,
        // so such depth will be excess
        indicesDepth = Math.min(indicesDepth, INDICES_DEPTH);
        Arrays.sort(words, Word.getWordComparator());
        baseIndex = new Index(0, words.length, words, "", 0);
        baseIndex.split();
        baseIndex.prepare();
    }

    public List<String> getSelection(String prefix) {
        if (prefix==null) {
            throw new NullPointerException("Prefix is null!");
        }
        List<String> result = new ArrayList<>();
        if (prefix.isEmpty()) {
            result.add("");
            return result;
        }
        String indexedPrefix;
        // even if prefix longer than indicesDepth there are no way to search by whole prefix;
        // it is necessary to truncate the prefix to find appropriate index
        if (prefix.length()>indicesDepth) {
            indexedPrefix = prefix.substring(0, indicesDepth);
        } else {
            indexedPrefix = prefix;
        }

        // Searching the index...
        Index nearestIndex = baseIndex;
        for (int charPosition = 0; charPosition<indexedPrefix.length(); ++charPosition) {
            nearestIndex = nearestIndex.getNestedIndex(indexedPrefix.charAt(charPosition));
        }

        // ...And finding appropriate words from sortedList
        // (list is sorted by frequency, see class Index)
        for (Word indexedWord : nearestIndex.sortedList) {
            if (indexedWord.word.startsWith(prefix)) {
                result.add(indexedWord.word);
            }
            if (result.size()==MAX_SELECTION_LENGTH) {
                break;
            }
        }
        if (result.size()==0) {
            result.add("");
        }
        return result;
    }

    private class Index {
        /**
         * Prefix of this index. All words in this index are started with it
         */
        public final String prefix;
        /**
         * Depth of this index in index hierarchy
         */
        public final int depthLevel;
        /**
         * Array indices with next depth level
         */
        private final Index[] nestedIndices;
        /**
         * Words for this index sorted by frequency (sorting is performed in prepare() method)
         */
        private final Word[] sortedList;

        private Index(int start, int end, Word[] sourceArray, String prefix, int depthLevel) {
            this.prefix = prefix;
            this.depthLevel = depthLevel;
            if (depthLevel<indicesDepth) {
                nestedIndices = new Index[ALPHABET.length()];
            } else {
                nestedIndices = null;
            }
            this.sortedList = new Word[end - start];
            System.arraycopy(sourceArray, start, sortedList, 0, end-start);
        }

        private void split() {
            if (nestedIndices==null) {
                return;
            }
            int position = 0;
            // Splitting the index...
            for (int charIndex = 0; charIndex<ALPHABET.length(); ++charIndex) {
                String newPrefix = prefix+ALPHABET.charAt(charIndex);
                Index newIndex;
                // If there are no more words, then next index will be empty
                if (position>=sortedList.length) {
                    newIndex = new Index(position, position, sortedList, newPrefix, depthLevel+1);
                // ...else here some work must be performed
                } else {
                    // Length of first word in depthLevel index must be shorter than depthLevel+1
                    // Exactly one word may be shorter, so one increment is enough
                    if (sortedList[position].word.length()<=depthLevel) {
                        ++position;
                    }
                    int startPosition = position;
                    // Increasing end position of new index (relative to this index words)
                    while (position<sortedList.length &&
                            sortedList[position].word.startsWith(newPrefix)) {
                        ++position;
                    }
                    newIndex = new Index(startPosition, position, sortedList,
                            newPrefix, depthLevel+1);
                }
                // New index must be split too
                newIndex.split();
                nestedIndices[charIndex] = newIndex;
            }
        }


        private void prepare() {
            // Sorting this index and all inner indices lists
            Arrays.sort(sortedList, Word.getFrequencyComparator());
            if (nestedIndices==null) {
                return;
            }
            for (Index nestedIndex : nestedIndices) {
                nestedIndex.prepare();
            }
        }

        private Index getNestedIndex(char character) {
            return nestedIndices[ALPHABET.indexOf(character)];
        }
    }
}
