package com.ecocitycraft.shopdb.models;

import java.util.List;

public class PaginatedResponse<T> {
    private int page;
    private long totalPages;
    private long totalResults;
    private List<T> results;

    public PaginatedResponse(int page, long totalPages, long totalResults, List<T> results) {
        this.page = page;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
        this.results = results;
    }

    public int getPage() {
        return page;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public List<T> getResults() {
        return results;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
