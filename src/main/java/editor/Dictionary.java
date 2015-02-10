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
    private static final char FIRST_CHAR = ALPHABET.charAt(0);
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
        indicesDepth = Math.max(indicesDepth, word.length());
        ++addingIndex;
    }

    /**
     * Building indices for dictionary
     */
    public void prepareForWork() {
        indicesDepth = Math.min(indicesDepth, INDICES_DEPTH);
        Arrays.sort(words, Word.getWordComparator());
        baseIndex = new Index(0, words.length, "", 0);
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
        }
        String indexedPrefix;
        if (prefix.length()>indicesDepth) {
            indexedPrefix = prefix.substring(0, indicesDepth);
        } else {
            indexedPrefix = prefix;
        }

        Index nearestIndex = baseIndex;
        for (int charPosition = 0; charPosition<indexedPrefix.length(); ++charPosition) {
            nearestIndex = nearestIndex.getNestedIndex(indexedPrefix.charAt(charPosition));
        }

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

    private Index buildIndex(int from, int to, char character, int charIndex, String prefix) {
        prefix = prefix + character;
        if (from>=to) {
            return new Index(from, from, prefix, charIndex + 1);
        }
        int start = from;
        char currentChar;
        while (words[from].word.length()<=charIndex) {
            ++from;
        }
        if (character!=FIRST_CHAR) {
            while (from<to) {
                currentChar = words[from].word.charAt(charIndex);
                if (currentChar==character) {
                    start = from;
                    break;
                } else if (currentChar>character) {
                    return new Index(0, 0, prefix, charIndex+1);
                }
                ++from;
            }
        }
        while (from<to) {
            currentChar = words[from].word.charAt(charIndex);
            if (currentChar!=character) {
                break;
            }
            ++from;
        }

        return new Index(start, from, prefix, charIndex+1);
    }

    private class Index {
        public final int start;
        public final int end;
        public final String prefix;
        public final int depthLevel;
        private final Index[] nestedIndices;
        private final Word[] sortedList;

        private Index(int start, int end, String prefix, int depthLevel) {
            this.start = start;
            this.end = end;
            this.prefix = prefix;
            this.depthLevel = depthLevel;
            if (depthLevel<indicesDepth) {
                nestedIndices = new Index[ALPHABET.length()];
            } else {
                nestedIndices = null;
            }
            if (depthLevel==0) {
                this.sortedList = words;
            } else {
                this.sortedList = new Word[end - start];
            }
        }

        private void split() {
            if (nestedIndices==null) {
                return;
            }
            Index newIndex = null;
            for (int charIndex = 0; charIndex<ALPHABET.length(); ++charIndex) {
                int startPosition = newIndex==null ? start : newIndex.end;
                newIndex = buildIndex(startPosition, end, ALPHABET.charAt(charIndex), depthLevel, prefix);
                newIndex.split();
                nestedIndices[charIndex] = newIndex;
            }
        }

        private void prepare() {
            if (depthLevel!=0) {
                System.arraycopy(words, start, sortedList, 0, end-start);
                Arrays.sort(sortedList, Word.getFrequencyComparator());
            }
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
