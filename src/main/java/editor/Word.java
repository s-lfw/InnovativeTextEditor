package editor;

import java.util.Comparator;

/**
* @author Vsevolod Kosulnikov
*/
class Word {
    public  final String word;
    public final int frequency;

    public Word(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return word;
    }

    public static Comparator<Word> getWordComparator() {
        return new Comparator<Word>() {
            @Override
            public int compare(Word o1, Word o2) {
                return o1.word.compareTo(o2.word);
            }
        };
    }

    public static Comparator<Word> getFrequencyComparator() {
        return new Comparator<Word>() {
            @Override
            public int compare(Word o1, Word o2) {
                return o2.frequency-o1.frequency;
            }
        };
    }
}
