package org.example;

import java.util.*;

public class Word {
    private String word;
    private Map<String,ArrayList<String>> meaning;

    public Word() {
        this.meaning = new HashMap<>();
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Map<String, ArrayList<String>> getMeaning() {
        return meaning;
    }

    @Override
    public String toString() {
        return "Word{" +
                "word='" + word + '\'' +
                ", meaning=" + meaning +
                '}';
    }

    public void setMeaning(Map<String, ArrayList<String>> meaning) {
        this.meaning = meaning;
    }


    public class AllMeaningValuesIterator implements Iterator<String> {
        private Iterator<ArrayList<String>> listIterator;
        private Iterator<String> valueIterator;

        public AllMeaningValuesIterator() {
            listIterator = meaning.values().iterator();
            valueIterator = new ArrayList<String>().iterator(); // Изначально пустой
        }

        @Override
        public boolean hasNext() {
            while ((valueIterator == null || !valueIterator.hasNext()) && listIterator.hasNext()) {
                valueIterator = listIterator.next().iterator();
            }
            return valueIterator != null && valueIterator.hasNext();
        }

        @Override
        public String next() {
            if (hasNext()) {
                return valueIterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    // Метод для получения итератора для всех значений
    public Iterator<String> getAllMeaningValuesIterator() {
        return new AllMeaningValuesIterator();
    }


}
