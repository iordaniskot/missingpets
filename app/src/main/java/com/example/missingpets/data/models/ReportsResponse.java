package com.example.missingpets.data.models;

import java.util.List;

public class ReportsResponse {
    private List<Report> data;
    private Pagination pagination;

    // Constructors
    public ReportsResponse() {}

    public ReportsResponse(List<Report> data, Pagination pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    // Getters and Setters
    public List<Report> getData() {
        return data;
    }

    public void setData(List<Report> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    // Inner class for pagination info
    public static class Pagination {
        private int currentPage;
        private int totalPages;
        private int totalItems;
        private int itemsPerPage;
        private boolean hasNextPage;
        private boolean hasPrevPage;

        // Constructors
        public Pagination() {}

        // Getters and Setters
        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public void setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public boolean isHasPrevPage() {
            return hasPrevPage;
        }

        public void setHasPrevPage(boolean hasPrevPage) {
            this.hasPrevPage = hasPrevPage;
        }
    }
}
