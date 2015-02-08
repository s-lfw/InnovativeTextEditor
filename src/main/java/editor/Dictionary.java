package editor;

import java.util.Arrays;

/**
 * @author Vsevolod Kosulnikov
 */
public class Dictionary {
    private static final int MAX_SELECTION_LENGTH = 10;
    private static final int INDICES_DEPTH = 4;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final char FIRST_CHAR = ALPHABET.charAt(0);

    private final Word[] words;
    private Index baseIndex;

    public Dictionary(int initialLength) {
        if (initialLength<=0) {
            throw new IllegalArgumentException("Cannot create dictionary with non-positive length");
        }
        words = new Word[initialLength];
    }

    private int addingIndex = 0;
    public void addWord(String word, int frequency) {
        if (word==null || word.isEmpty()) {
            System.err.println("Empty word skipped");
            return;
        }
        if (frequency<1) {
            System.err.println("Non-used word skipped (frequency <= 0");
            return;
        }
        words[addingIndex] = new Word(word.toLowerCase(), frequency);
        ++addingIndex;
    }

    public void prepareForWork() {
        Arrays.sort(words, Word.getWordComparator());
        baseIndex = new Index(0, words.length, "", 0);
        baseIndex.split();
        baseIndex.prepare();
//        Index newIndex = null;
//        for (int charIndex = 0; charIndex<ALPHABET.length(); ++charIndex) {
//            int startPosition = newIndex==null ? 0 : newIndex.end;
//            newIndex = buildIndex(startPosition, words.length, ALPHABET.charAt(charIndex), 0, "");
//            newIndex.split();
//            newIndex.prepare();
//            indexList.add(newIndex);
//        }
    }

    public void getSelection(String prefix) {
        String indexedPrefix;
        if (prefix.length()>INDICES_DEPTH) {
            indexedPrefix = prefix.substring(0, INDICES_DEPTH);
        } else {
            indexedPrefix = prefix;
        }

        Index nearestIndex = baseIndex;
        for (int charPosition = 0; charPosition<indexedPrefix.length(); ++charPosition) {
            nearestIndex = nearestIndex.getNestedIndex(indexedPrefix.charAt(charPosition));
        }

        int foundWords = 0;
        for (Word indexedWord : nearestIndex.sortedList) {
            if (indexedWord.word.startsWith(prefix)) {
                sendAnswer(indexedWord.word);
                ++foundWords;
            }
            if (foundWords==MAX_SELECTION_LENGTH) {
                break;
            }
        }
    }

    private void sendAnswer(String s) {
        System.out.println(s);
    }

    private Index buildIndex(int from, int to, char character, int charIndex, String prefix) {
        prefix = prefix + character;
        if (from>=to)
            return new Index(from, from, prefix, charIndex+1);
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
            if (depthLevel<INDICES_DEPTH) {
                nestedIndices = new Index[ALPHABET.length()];
            } else {
                nestedIndices = null;
            }
            this.sortedList = new Word[end-start];
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

        private void prepare() { //todo may be optimized in depthLevel==0
            System.arraycopy(words, start, sortedList, 0, end-start);
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
