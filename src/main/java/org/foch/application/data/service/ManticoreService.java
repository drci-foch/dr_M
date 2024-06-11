package org.foch.application.data.service;

import com.manticoresearch.client.ApiClient;
import com.manticoresearch.client.Configuration;
import com.manticoresearch.client.api.UtilsApi;
import org.foch.application.model.DocContent;
import org.foch.application.model.GridData;
import org.foch.application.data.SearchData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ManticoreService {


    public UtilsApi utilsApi;

    public ManticoreService(@Value("${manticore.client}") String manticoreClient) {
        ApiClient client = Configuration.getDefaultApiClient();
        client.setBasePath(manticoreClient);
        utilsApi = new UtilsApi(client);

    }

    public String countDocs(SearchData searchData) {
        try {
            Map<String, Object> sqlresult = (Map<String, Object>) utilsApi.sql("SELECT count(*) as c," +
                    " COUNT(DISTINCT patient_num) AS d FROM wh_docs_f WHERE MATCH('" + searchData.freeText + "');", true).get(0);
            System.out.println(sqlresult);
            List a = (List) sqlresult.get("data");
            String countDocs = ((Map<String, Object>) a.get(0)).get("c").toString();
            String countPatients = ((Map<String, Object>) a.get(0)).get("d").toString();
            return countDocs + " document(s) & " + countPatients + " patient(s)";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return "Pas de résultats";
        }

    }

    public List<DocContent> batchDocs(List<String> docIds) {
        List<DocContent> data = new ArrayList<>();
        try {
            String fullQuery = "SELECT id, document FROM wh_docs_f WHERE id in (" + String.join(",", docIds) + ") LIMIT 500;";
            System.out.println(fullQuery);
            Map<String, Object> sqlresult = (Map<String, Object>) utilsApi.sql(fullQuery, true).get(0);
            List dataResult = (List) sqlresult.get("data");
            int i = 0;
            for (var a : dataResult) {
                i++;
                Map<?, ?> b = (Map<?, ?>) a;
                String idDoc = b.get("id").toString();
                String document = b.get("document").toString();
                DocContent d = new DocContent(idDoc, document);
                data.add(d);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return data;
    }

    public List<GridData> queryDocs(SearchData searchData, int offset) {
        List<GridData> data = new ArrayList<>();
        try {

            String fullQuery = "SELECT id, sex, patient_num, birth_date, document_date, title, highlight() as h FROM wh_docs_f WHERE MATCH('"
                    + searchData.freeText + "') ORDER BY patient_num ASC LIMIT 500 OFFSET " + offset + " option max_matches=20000;";
            System.out.println(fullQuery);
            Map<String, Object> sqlresult = (Map<String, Object>) utilsApi.sql(fullQuery, true).get(0);
            List dataResult = (List) sqlresult.get("data");
            int i = 0;
            for (var a : dataResult) {
                i++;
                Map<?, ?> b = (Map<?, ?>) a;
                String idDoc = b.get("id").toString();
                String number = "" + (i + offset);
                String ipp = "0" + b.get("patient_num");
                String sex = b.get("sex").toString();
                String ddnDate = b.get("birth_date").toString();
                String ddn = ddnDate.substring(6, 8) + "/" + ddnDate.substring(4, 6) + "/" + ddnDate.substring(0, 4);
                String titre = b.get("title").toString();
                String documentDate = b.get("document_date").toString();
                String date = documentDate.substring(6, 8) + "/" + documentDate.substring(4, 6) + "/" + documentDate.substring(0, 4);
                String contexte = b.get("h").toString();
                GridData d = new GridData(idDoc, sex, number, ipp, ddn, date, titre, contexte);
                data.add(d);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return data;
    }

    @SuppressWarnings({"unchecked"})
    public String getContentDoc(Long id) {
        try {
            Map<String, Object> sqlresult = (Map<String, Object>) utilsApi
                    .sql("SELECT document FROM wh_docs_f WHERE id=" + id + ";", true).get(0);
            List a = (List) sqlresult.get("data");
            return ((Map<String, Object>) a.get(0)).get("document").toString();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return "";
    }

    private String constructFilterQuery(SearchData searchData) {
        String result = "";
        if (searchData.age1 != null && searchData.age1.length() > 0) {
            result += " and age >= " + searchData.age1;
        }
        if (searchData.age2 != null && searchData.age2.length() > 0) {
            result += " and age <= " + searchData.age2;
        }

        if (searchData.date1 != null && searchData.date1.length() > 8) {
            //TODO convert to int
            result += " and document_date >= " + convertDateToInt(searchData.date1);
        }

        if (searchData.date2 != null && searchData.date2.length() > 8) {
            //TODO convert to int
            result += " and document_date >= " + convertDateToInt(searchData.date2);
        }

//        if (searchData.origin != null && searchData.origin.length() > 5) {
//            //TODO convert to int
//            result += " and document_date >= " + convertDateToInt(searchData.date2);
//        }
        return result;
    }

    private int convertDateToInt(String date) {
        return 0;
    }

}
