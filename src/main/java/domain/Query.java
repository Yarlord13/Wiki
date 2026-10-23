package domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Query {
    @SerializedName("search")
    private List<SearchResult> search;

    public List<SearchResult> getSearch() { return search; }
    public void setSearch(List<SearchResult> search) { this.search = search; }
}
