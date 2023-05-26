import java.util.Objects;

public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;
    private final int page;
    private final int count;

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }

    public String getPdfName() {
        return pdfName;
    }

    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;
    }

    @Override
    public int compareTo(PageEntry o) {
        return Integer.compare(o.count, this.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pdfName, page);
    }

    @Override
    public boolean equals(Object obj) {
        PageEntry pageEntry = (PageEntry) obj;
        return this.pdfName.equals(pageEntry.pdfName) && this.page == pageEntry.page;
    }
}