package domain;

import com.google.gson.annotations.SerializedName;

public class SearchResult {
    @SerializedName("title")
    private String title;

    @SerializedName("pageid")
    private long pageId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getPageId() { return pageId; }
    public void setPageId(long pageId) { this.pageId = pageId; }

    @Override
    public String toString() {
        return "SearchResult {" +
                "pageId=" + pageId +
                ", title='" + title + '\'' +
                '}';
    }
}
