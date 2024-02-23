package org.example;

public class Response {
    private String articleTitle;
    private String abstractText;
    private String authors;
    private String releaseDate;
    private String magazineTitle;
    private String doi;
    private int quoteNub;

    private String authorQuery;

    private String dateFromQuery;

    public Response(String articleTitle, String abstractText, String authors,
                    String releaseDate, String magazineTitle, String doi, int quoteNub, String authorQuery, String dateFromQuery) {
        this.articleTitle = articleTitle;
        this.abstractText = abstractText;
        this.authors = authors;
        this.releaseDate = releaseDate;
        this.magazineTitle = magazineTitle;
        this.doi = doi;
        this.quoteNub = quoteNub;
        this.authorQuery = authorQuery;
        this.dateFromQuery = dateFromQuery;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getMagazineTitle() {
        return magazineTitle;
    }

    public void setMagazineTitle(String magazineTitle) {
        this.magazineTitle = magazineTitle;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public int getQuoteNub() {
        return quoteNub;
    }

    public void setQuoteNub(int quoteNub) {
        this.quoteNub = quoteNub;
    }

    public String getAuthorQuery(){
        return this.authorQuery;
    }

    public String getDateFromQuery(){
        return this.dateFromQuery;
    }
}

