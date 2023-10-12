package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public void setMeaning(Map<String, ArrayList<String>> meaning) {
        this.meaning = meaning;
    }
}
