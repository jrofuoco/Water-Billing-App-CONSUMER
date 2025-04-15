package com.example.consumerapp;

public class Announcement {
    private String title;
    private String content;
    private String datePosted;
    private String expirationDate;
    private String audience;

    // Zero-argument constructor
    public Announcement() {
        // Initialize with default values
        this.title = "";
        this.content = "";
        this.datePosted = "";
        this.expirationDate = "";
        this.audience = "";
    }

    // Parameterized constructor
    public Announcement(String title, String content, String datePosted, String expirationDate, String audience) {
        this.title = title;
        this.content = content;
        this.datePosted = datePosted;
        this.expirationDate = expirationDate;
        this.audience = audience;
    }

    // Getters for the fields
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getAudience() {
        return audience;
    }
}
