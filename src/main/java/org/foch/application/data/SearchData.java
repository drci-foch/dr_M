package org.foch.application.data;

public class SearchData {

    public String freeText;
    public String titre;
    public String origin;
    public String age1;
    public String age2;
    public String date1;
    public String date2;
    public String sex;

    public SearchData(){

    }
    public SearchData(String freeText, String titre, String origin, String age1, String age2, String date1, String date2) {
        this.freeText = freeText;
        this.titre = titre;
        this.origin = origin;
        this.age1 = age1;
        this.age2 = age2;
        this.date1 = date1;
        this.date2 = date2;
    }
}
