package editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vsevolod Kosulnikov
 */
public class Dictionary {
    private static final int MAX_SELECTION_LENGTH = 10;
    private static final int INDICES_DEPTH = 4;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final char FIRST_CHAR = ALPHABET.charAt(0);

    private final Word[] words;
    private List<Index> indexList = new ArrayList<>();

    public Dictionary(int initialLength) {
        //todo check input
        words = new Word[initialLength];
    }

    private int addingIndex = 0;
    public void addWord(String word, int frequency) {
        words[addingIndex] = new Word(word.toLowerCase(), frequency);
        ++addingIndex;
    }

    public void prepareForWork() {
        Arrays.sort(words, Word.getWordComparator());
        Index newIndex = null;
        for (int charIndex = 0; charIndex<ALPHABET.length(); ++charIndex) {
            int startPosition = newIndex==null ? 0 : newIndex.end;
            newIndex = buildIndex(startPosition, words.length, ALPHABET.charAt(charIndex), 0, "");
            indexList.add(newIndex);
        }
        // starting from 1 because one depth level was created above
        for (int depthLevel = 1; depthLevel<INDICES_DEPTH; ++depthLevel) {
            // it must be snapped because indexList will be changed
            List<Index> newIndexList = new ArrayList<>();
            for (int indexPosition = 0; indexPosition<indexList.size(); ++indexPosition) {
                List<Index> bulkOfIndices = splitIndex(depthLevel, indexPosition);
                newIndexList.addAll(bulkOfIndices);
            }
            indexList = newIndexList;
        }
    }

    private List<Index> splitIndex(int depthLevel, int indexPosition) {
//        System.out.println("Parsing "+depthLevel+" level, indexPosition = "+indexPosition+", character "+indexPosition%26);
        Index parsingIndex = indexList.get(indexPosition);
        List<Index> newIndices = new ArrayList<>();
        Index newIndex = null;
        for (int charIndex = 0; charIndex<ALPHABET.length(); ++charIndex) {
            int startPosition = newIndex==null ? parsingIndex.start : newIndex.end;
            newIndex = buildIndex(startPosition, parsingIndex.end,
                    ALPHABET.charAt(charIndex), depthLevel, parsingIndex.prefix);
            newIndices.add(newIndex);
        }
        return newIndices;
    }

    public String[] getSelection(String prefix) {
        if (prefix==null) {
            throw new NullPointerException("Prefix argument is null");
        }
        if (prefix.isEmpty()) {
            return new String[0];
        }

        int startIndex, endIndex;
        int startIndexPosition = 0;
        for (int charIndex = 0; charIndex < Math.min(prefix.length(), INDICES_DEPTH); ++charIndex) {
            startIndexPosition += ALPHABET.indexOf(prefix.charAt(charIndex))*Math.pow(ALPHABET.length(),
                    INDICES_DEPTH-charIndex-1);
        }
        Index index = indexList.get(startIndexPosition);
        startIndex = index.start;

        for (int charPosition = INDICES_DEPTH; charPosition<prefix.length(); ++charPosition) {
            char checkingChar = prefix.charAt(charPosition);
            while (words[startIndex].word.length()<=charPosition ||
                    words[startIndex].word.charAt(charPosition)<checkingChar)
                ++startIndex;
            if (startIndex>=words.length ||
                    !words[startIndex].word.startsWith(prefix.substring(0, charPosition))) {
                return new String[0];
            }
        }

        int endIndexPosition = 0;
        if (prefix.length()>=INDICES_DEPTH) {
            endIndex = index.end;
        } else {
            for (int charIndex = 0; charIndex < prefix.length()-1; ++charIndex) {
                endIndexPosition += ALPHABET.indexOf(prefix.charAt(charIndex))*Math.pow(ALPHABET.length(),
                        INDICES_DEPTH-charIndex-1);
            }
            endIndexPosition += (ALPHABET.indexOf(prefix.charAt(prefix.length()-1)) + 1)*Math.pow(ALPHABET.length(),
                    INDICES_DEPTH-prefix.length());
            if (endIndexPosition==indexList.size()) {
                --endIndexPosition;
            }
            index = indexList.get(endIndexPosition);
            endIndex = index.end;
        }

        for (int charPosition = INDICES_DEPTH; charPosition<prefix.length(); ++charPosition) {
            char checkingChar = prefix.charAt(charPosition);
            while (words[endIndex-1].word.length()<=charPosition ||
                    words[endIndex-1].word.charAt(charPosition)>checkingChar) {
                --endIndex;
            }
            if (!words[endIndex-1].word.startsWith(prefix.substring(0, charPosition))) {
                return new String[0];
            }
        }


        Word[] selection = new Word[endIndex-startIndex];
        System.arraycopy(words, startIndex, selection, 0, selection.length);

        Arrays.sort(selection, Word.getFrequencyComparator());

        String[] result = new String[Math.min(selection.length, MAX_SELECTION_LENGTH)];
        for (int i = 0; i<result.length; ++i) {
            result[i] = selection[i].word;
        }

        return result;
    }

    private Index buildIndex(int from, int to, char character, int charIndex, String prefix) {
        prefix = prefix + character;
        if (from>=to)
            return new Index(from, from, prefix);
        int start = from;
        char currentChar = ' ';
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
                    return new Index(0, 0, prefix);
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

        return new Index(start, from, prefix);
    }

    private class Index {
        public final int start;
        public final int end;
        public final String prefix;

        private Index(int start, int end, String prefix) {
            this.start = start;
            this.end = end;
            this.prefix = prefix;
        }
    }
}
