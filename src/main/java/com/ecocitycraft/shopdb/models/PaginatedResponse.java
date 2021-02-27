package com.ecocitycraft.shopdb.models;

import java.util.List;

public class PaginatedResponse<T> {
    private int page;
    private long totalPages;
    private long totalElements;
    private List<T> results;

    public PaginatedResponse(int page, long totalPages, long totalElements, List<T> results) {
        this.page = page;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.results = results;
    }

    public int getPage() {
        return page;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
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

    public void setTotalElements(long totalResults) {
        this.totalElements = totalResults;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
