import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {

    Map<String, List<PageEntry>> indexing = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        List<String> stop = new ArrayList<>(Files.readAllLines(Paths.get("C:\\Users\\pomog\\IdeaProjects\\diplom\\diplom_search-engine\\stop-ru.txt")));
        String[] pdfNames = pdfsDir.list();
        for (String pdfName : pdfNames) {
            try (var doc = new PdfDocument(new PdfReader(pdfName))) {
                for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                    String text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                    var words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = new HashMap<>();
                    for (var word : words) {
                        if (word.isEmpty()) {
                            continue;
                        }
                        word = word.toLowerCase();
                        if (!stop.contains(word)) {
                            freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                            PageEntry pe = new PageEntry(pdfName, i, freqs.get(word));

                            if (indexing.containsKey(word)) {
                                List<PageEntry> previousValue = indexing.get(word);
                                Iterator<PageEntry> iterator = previousValue.iterator();
                                while (iterator.hasNext()) {
                                    PageEntry pageEntry = iterator.next();
                                    if (pageEntry.getPage() == i) {
                                        iterator.remove();
                                    }
                                }
                                previousValue.add(pe);
                            } else {
                                List<PageEntry> value = new ArrayList<>();
                                value.add(pe);
                                indexing.put(word, value);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        String[] words = word.split(" ");
        List<PageEntry> total = new ArrayList<>();
        for (String s : words) {
            if (indexing.containsKey(s)) {
                List<PageEntry> value = indexing.get(s);
                total.addAll(value);
            }
        }
        if (words.length > 1) {
            List<PageEntry> mergedValues = new ArrayList<>();
            for (int x = 0; x < total.size(); x++) {
                for (int y = 0; y < total.size(); y++) {
                    if (x > y) {
                        if (total.get(x).getPdfName().equals(total.get(y).getPdfName())) {
                            if (total.get(x).getPage() == total.get(y).getPage()) {
                                PageEntry mergedValue = new PageEntry(total.get(x).getPdfName(), total.get(x).getPage(), total.get(x).getCount() + total.get(y).getCount());
                                mergedValues.add(mergedValue);
                            }
                        }
                    }
                }
            }
            Set<PageEntry> resultSet = new HashSet<>();
            for (PageEntry pageEntry : total) {
                for (PageEntry pageEntry1 : mergedValues) {
                    if (pageEntry.getPdfName().equals(pageEntry1.getPdfName())) {
                        if (pageEntry.getPage() == pageEntry1.getPage()) {
                            if (pageEntry.getCount() < pageEntry1.getCount()) {
                                resultSet.add(pageEntry1);
                            }
                        } else {
                            resultSet.add(pageEntry);
                        }
                    }
                }
                resultSet.add(pageEntry);
            }
            List<PageEntry> result = new ArrayList<>(resultSet);
            return result.stream().sorted().collect(Collectors.toList());
        } else {
            return total.stream().sorted().collect(Collectors.toList());
        }
    }
}