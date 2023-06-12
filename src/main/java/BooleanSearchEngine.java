import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> indexing = new HashMap<>();

    public Map<String, List<PageEntry>> getIndexing() {
        return indexing;
    }

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        List<String> stop = new ArrayList<>(Files.readAllLines(Path.of("stop-ru.txt")));

        File[] pdfFiles = pdfsDir.listFiles();
        for(File pdf : pdfFiles) {
            try (var doc = new PdfDocument(new PdfReader(pdf))) {
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
                            PageEntry pe = new PageEntry(pdf.getName(), i, freqs.get(word));

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
        String[] words = word.toLowerCase().split(" ");
        List<PageEntry> total = new ArrayList<>();
        for (String s : words) {
            if (indexing.containsKey(s)) {
                List<PageEntry> value = indexing.get(s);
                total.addAll(value);
            }
        }
        if (words.length > 1) {
            List<PageEntry> mergedValues = new ArrayList<>();
            for (int i = 0; i < total.size(); i++) {
                for (int j = 0; j < total.size(); j++) {
                    if (i > j) {
                        if (total.get(i).getPdfName().equals(total.get(j).getPdfName())) {
                            if (total.get(i).getPage() == total.get(j).getPage()) {
                                PageEntry mergedValue = new PageEntry(total.get(i).getPdfName(), total.get(i).getPage(), total.get(i).getCount() + total.get(j).getCount());
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