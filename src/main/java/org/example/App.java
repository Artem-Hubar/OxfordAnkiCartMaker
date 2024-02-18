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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        String filePath = "bin/input/Anki_cards___2023-10-11T10-45-32.xlsx";

        try (InputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis); Workbook wb = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("bin/output/descriptions.xlsx");
             Workbook wb2 = new XSSFWorkbook();
             FileOutputStream fo2 = new FileOutputStream("bin/output/writing.xlsx")) {
            Sheet sheet1 = wb.createSheet("Sheet1");
            Sheet sheet2 = wb2.createSheet("Sheet2");

            Sheet sheet = workbook.getSheetAt(0); // 0 - индекс листа, если у вас есть несколько листов

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
//          Создание карточек
            ArrayList<CartWord> cartWords = new ArrayList<>();
            for (Word word : words) {
//                добавление карточек где нужно описывать значение слова(у одного слова может быть несколько контекстов)


                cartWords.addAll(getCartsDefinitions(word));
//                добавление карточек (с вводом слов)
//                должно быть несколько примеров, в каждом из котором пропущено искомое слово
// так же описание значения слова
                cartWords.addAll(getCartWriting(word));
            }
            int cdI = 0;
            int cwI = 0;
            for (CartWord cart : cartWords) {
                if (cart.getWord().length() ==0){
                    continue;
                }
                Row row1 = null;
                if (cart instanceof CartDefinition) {
                    row1 = sheet1.createRow(cdI);
                    cdI++;

                } else if (cart instanceof CartWriting) {
                    row1 = sheet2.createRow(cwI);
                    cwI++;

                }


                assert row1 != null;
                Cell cell1 = row1.createCell(0);
                Cell cell2 = row1.createCell(1);
                Cell cell3 = row1.createCell(2);
                Cell cell4 = row1.createCell(3);

                cell1.setCellValue(cart.getText());
                cell2.setCellValue(cart.getBackView());
                cell3.setCellValue(cart.getQuestion());
                cell4.setCellValue(cart.getWord());


            }
            wb.write(fileOut);
            wb2.write(fo2);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<CartWord> getCartsDefinitions(Word word) {
        ArrayList<CartWord> arrayList = new ArrayList<>();
        Map<String, ArrayList<String>> meaning = word.getMeaning();

        for (String m : meaning.keySet()) {
            CartDefinition cartWord = new CartDefinition();
            StringBuffer sb = new StringBuffer();
            sb.append(word.getWord()).append("\n");
            for (String sample : meaning.get(m)) {
                if(!sample.isEmpty()){
                    sb.append(sample).append("\n");
                }
            }
            cartWord.setText(sb.toString());
            cartWord.setBackView(m);
            cartWord.setWord(word.getWord());
            cartWord.setQuestion("Что значит слово?");
            if (cartWord.getBackView().trim().length() != 0){
                System.out.println("this is word: "+ cartWord.getWord());
                System.out.println("this is text: "+cartWord.getText());
                System.out.println("this is text: "+cartWord.getBackView());

                arrayList.add(cartWord);
            }
        }

        return arrayList;
    }


    private static ArrayList<CartWord> getCartWriting(Word word) {

        ArrayList<CartWord> cartWriting = new ArrayList<>();
        Map<String, ArrayList<String>> meaning = word.getMeaning();

        for (String m : meaning.keySet()) {
            CartWriting cartWord = new CartWriting();
            StringBuffer sb = new StringBuffer();
            sb.append(m).append("\n");
            int i = 0;
            for (String sample : meaning.get(m)) {
                if (i > 5) {
                    break;
                }
                sb.append(getCoveredWord(sample, word.getWord())).append("\n");
                i++;
            }
            cartWord.setText(sb.toString());
            cartWord.setBackView(word.getWord());
            cartWord.setWord(word.getWord());
            cartWord.setQuestion("Введите слово которое подходит");

            cartWriting.add(cartWord);
        }

        return cartWriting;
    }


    private static String getCoveredWord(String sample, String findingWord) {
        int firstIndex = sample.indexOf(findingWord);
        int lastIndex = sample.lastIndexOf(findingWord);
        String answer = "{{c1::" + findingWord + "}}";
        if (firstIndex == -1 || lastIndex == -1) {

        } else if (firstIndex == 0) {
            answer += sample.substring(findingWord.length());

        } else if (lastIndex == sample.length()) {
            answer = sample.substring(0, firstIndex) + answer;

        } else {

            answer = sample.substring(0, firstIndex) + answer + sample.substring(findingWord.length());
        }
        return answer;
    }


    private static Word fillWord(String stringCellValue) throws IOException {
        Word word = new Word();
        LinkedList<String> list = new LinkedList<>();
        word.setWord(stringCellValue);
        Map<String, ArrayList<String>> meaning = new HashMap<>();
        Document doc = getDoc(stringCellValue);
        Element entryContent = doc.getElementById("entryContent");
        Element mean = entryContent.child(0).child(1);
//        Заполняем информацию о искомом Слове
        switch (mean.className()) {
            case "sense_single": {
                Element single_mean = mean.child(0);
                String meanSentence = entryContent.getElementsByClass("webtop").first().getElementsByClass("grammar").text() + single_mean.getElementsByClass("def").text();
                meaning.put(meanSentence, addSample(single_mean));
                break;
            }
            default: {
                Elements different_mean = mean.children();
                for (Element e : different_mean) {
                    if (!e.className().equals("collapse")) {
                        String meanSentence = e.getElementsByClass("grammar").text() + e.getElementsByClass("def").text();
                        meaning.put(meanSentence, addSample(e));
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
            for (Element example : examples) {
                samples.add(example.text());
            }
        } catch (NullPointerException exception) {
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
