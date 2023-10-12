package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

            for (Row row : sheet) {
                for (Cell cell : row) {

                        switch (cell.getCellType()) {
                            case STRING:
                                getWord(cell.getStringCellValue().trim().toLowerCase());
                                break;
                        }
                        System.out.println("Перейти к следующему слову?");
                    while (!sc.nextLine().trim().equals("")){

                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getWord(String stringCellValue) throws IOException {
        Document doc = isFound(stringCellValue);
        Element entryContent = doc.getElementById("entryContent");
        Element mean = entryContent.child(0).child(1);
//        Заполняем информацию о искомом Слове
        Word word = new Word();
        word.setWord(stringCellValue);
        Element examplesElement  = null;
        ArrayList<String> meaning = new ArrayList<>();
        switch (mean.className()){
            case "sense_single":{
                Element single_mean = mean.child(0);
                meaning.add(entryContent.getElementsByClass("webtop").first().getElementsByClass("grammar").text() + single_mean.getElementsByClass("def").text());
                examplesElement  = single_mean;
            } default:{
                Elements different_mean = mean.children();
                for (Element e : different_mean) {
                    if (!e.className().equals("collapse")) {
                        meaning.add(e.getElementsByClass("grammar").text() + e.getElementsByClass("def").text());
                        examplesElement  = e;

                    }
                }
            }
        }



        System.out.println(word.getWord());
        ArrayList<String> samples = new ArrayList<>();









        if (examplesElement != null) {
            System.out.println(examplesElement);
            Elements examples = examplesElement.children();
            for (Element example : examples) {
                if (example.children() != null) {
                    StringBuffer sb = new StringBuffer();
                    for(Element childElement : example.children()){
                        sb.append(childElement.text() +" ");

                    }
                    System.out.println(sb);
                }else {
                    Element childElement = example.child(0);
                    String sample = childElement.text();
                    System.out.println(sample);
                }
            }
        }
    }

    private static Document isFound(String stringCellValue) throws IOException {
        String url = "https://www.oxfordlearnersdictionaries.com/spellcheck/english/?q=" + stringCellValue;
        Document doc = Jsoup.connect(url).get();
        if (!doc.getElementsByClass("result-list").isEmpty()) {
            doc = Jsoup.connect(doc.getElementsByClass("result-list").first().child(0).child(0).attr("href")).get();
        }
        return doc;
    }
}
