package org.example;

public abstract class CartWord {
    private String Text;
    private  String backView;
    private String Question;

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getBackView() {
        return backView;
    }

    public void setBackView(String backView) {
        this.backView = backView;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getWord() {
        return Word;
    }

    public void setWord(String word) {
        Word = word;
    }

    private String Word;
}
