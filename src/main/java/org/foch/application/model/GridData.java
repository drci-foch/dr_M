package org.foch.application.model;

public class GridData {

    public String idDoc;
    public String sex;
    public String number;
    public String ipp;
    public String ddn;
    public String date;
    public String titre;
    public String contexte;

    public GridData(String idDoc, String sex, String number, String ipp, String ddn, String date, String titre, String contexte) {
        this.idDoc = idDoc;
        this.sex = sex;
        this.number = number;
        this.ipp = ipp;
        this.ddn = ddn;
        this.date = date;
        this.titre = titre;
        this.contexte = contexte;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIpp() {
        return ipp;
    }

    public void setIpp(String ipp) {
        this.ipp = ipp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContexte() {
        return contexte;
    }

    public void setContexte(String contexte) {
        this.contexte = contexte;
    }

    @Override
    public String toString() {
        return idDoc + "|"
                + sex + "|"
                + number + "|"
                + ipp + "|"
                + ddn + "|"
                + date + "|"
                + titre + "|"
                + contexte;
    }
}
