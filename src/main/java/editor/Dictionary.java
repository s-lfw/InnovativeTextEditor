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

    private final Word[] words;
    private final List<Index> indexList = new ArrayList<>();

    //todo all words to lowercase
    public Dictionary(int initialLength) {
        //todo check input
        words = new Word[initialLength];
    }

    private int addingIndex = 0;
    public void addWord(Word word) {
        words[addingIndex] = word;
        ++addingIndex;
    }

    public void prepareForWork() {
        Arrays.sort(words, Word.getWordComparator());
        for (int charIndex = 0; charIndex<ALPHABET.length(); ++charIndex) {
            Index newIndex = buildIndex(0, words.length, ALPHABET.charAt(charIndex), 0);
            indexList.add(newIndex);
        }
        for (int depthLevel = 0; depthLevel<INDICES_DEPTH; ++depthLevel) {
            for (int indexPosition = 0; indexPosition<indexList.size(); ++indexPosition) {
                splitIndex(depthLevel+1, indexPosition);
            }
        }
    }

    private void splitIndex(int depthLevel, int indexPosition) {
        Index parsingIndex = indexList.remove(indexPosition);
        List<Index> newIndices = new ArrayList<>();
        for (int charIndex = 0; charIndex<ALPHABET.length(); ++charIndex) {
            Index newIndex = buildIndex(parsingIndex.getFrom(), parsingIndex.getTo(),
                    ALPHABET.charAt(charIndex), depthLevel);
            newIndices.add(newIndex);
        }
        indexList.addAll(indexPosition, newIndices);
    }

    public String[] getSelection(String prefix) {
        if (prefix==null) {
            throw new NullPointerException("Prefix argument is null");
        }
        if (prefix.isEmpty()) {
            return new String[0];
        }

        Index index = null;
        if (prefix.length()<=indexList.size()) {
            index = indexList.get(prefix.length()-1);

        } else {

        }

        int startIndex = index.getFrom();
        int endIndex = index.getTo();

        Word[] selection = new Word[endIndex-startIndex];
        System.arraycopy(words, startIndex, selection, 0, selection.length);

        Arrays.sort(words, Word.getFrequencyComparator());

        String[] result = new String[Math.min(selection.length, MAX_SELECTION_LENGTH)];
        for (int i = 0; i<result.length; ++i) {
            result[i] = selection[i].word;
        }

        return result;
    }

    private Index buildIndex(int from, int to, char character, int charIndex) {
        int start = 0;
        int length = 0;
        char currentChar;
        while (from<to) {
            currentChar = words[from].word.charAt(charIndex);
            if (currentChar==character) {
                start = from;
                break;
            } else if (currentChar>character) {
                return new Index(0, 0);
            }
        }
        while (from<to) {
            currentChar = words[from].word.charAt(charIndex);
            if (currentChar!=character) {
                length = from-length;
                break;
            }
        }
        if (length==0) {
            length = to-start;
        }

        return new Index(start, length);
    }

    private class Index {
        private final int start;
        private final int length;

        private Index(int start, int length) {
            this.start = start;
            this.length = length;
        }

        public int getFrom() {}
        public int getTo() {}
    }
}
