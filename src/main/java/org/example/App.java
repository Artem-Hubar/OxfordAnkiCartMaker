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

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        String filePath = "bin/input/Anki_cards___2023-10-11T10-45-32.xlsx";

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // 0 - индекс листа, если у вас есть несколько листов

            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            getWord(cell.getStringCellValue().trim().toLowerCase());
                            break;
                        // Другие типы ячеек можно обработать аналогично
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
        if (mean.className().equals("sense_single")) {
            Element single_mean = mean.child(0);
            System.out.println(entryContent.getElementsByClass("webtop").first().getElementsByClass("grammar").text() + single_mean.getElementsByClass("def").text());
        } else {
            Elements different_mean = mean.children();
            for (Element e : different_mean) {
                if (e.className().equals("shcut-g")) {
                    System.out.println("" + e.getElementsByClass("grammar").text() + e.getElementsByClass("def").text());
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
