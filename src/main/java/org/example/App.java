package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        String filePath = "bin/input/Anki_cards___2023-10-11T10-45-32.xlsx";

        try (InputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // 0 - индекс листа, если у вас есть несколько листов
            Scanner sc = new Scanner(System.in);
            ArrayList<Word> words = new ArrayList<>();
//            получение слов из xmls
            for (Row row : sheet) {
                for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
//                              заполнение информации о словах
                                words.add(fillWord(cell.getStringCellValue().trim().toLowerCase()));
                                break;
                        }
                }
            }
            System.out.println(words);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Word fillWord(String stringCellValue) throws IOException {
        Word word = new Word();
        word.setWord(stringCellValue);
        Map<String,ArrayList<String>> meaning = new HashMap<>();
        Document doc = getDoc(stringCellValue);
        Element entryContent = doc.getElementById("entryContent");
        Element mean = entryContent.child(0).child(1);
//        Заполняем информацию о искомом Слове
        switch (mean.className()){
            case "sense_single":{
                Element single_mean = mean.child(0);
                String meanSentence = entryContent.getElementsByClass("webtop").first().getElementsByClass("grammar").text() + single_mean.getElementsByClass("def").text();
                meaning.put(meanSentence ,  addSample(single_mean));
                System.out.println(meanSentence);
                break;
            } default: {
                Elements different_mean = mean.children();
                for (Element e : different_mean) {
                    if (!e.className().equals("collapse")) {
                        String meanSentence = e.getElementsByClass("grammar").text() + e.getElementsByClass("def").text();
                        System.out.println(meanSentence);
                        meaning.put(meanSentence,addSample(e));
                    }
                }
            }
        }
        word.setMeaning(meaning);
        return word;
    }

    private static ArrayList<String> addSample(Element e) {
        ArrayList<String> samples = new ArrayList<>();

//        проверка на несколько несколько примеров
        try {
            Elements examples = e.getElementsByClass("examples").first().children();
            for (Element example : examples){
                samples.add(example.text());
            }
        }catch (NullPointerException exception){
//            случай на наличие всего одного примера
            Elements example = e.getElementsByClass("examples");
            samples.add(example.text());
        }
        return samples;
    }

    private static Document getDoc(String stringCellValue) throws IOException {
        String url = "https://www.oxfordlearnersdictionaries.com/spellcheck/english/?q=" + stringCellValue;
        Document doc = Jsoup.connect(url).get();
        if (!doc.getElementsByClass("result-list").isEmpty()) {
            doc = Jsoup.connect(doc.getElementsByClass("result-list").first().child(0).child(0).attr("href")).get();
        }
        return doc;
    }




}
