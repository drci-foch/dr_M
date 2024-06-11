package org.foch.application.utils;

import com.manticoresearch.client.ApiClient;
import com.manticoresearch.client.Configuration;
import com.manticoresearch.client.api.IndexApi;
import com.manticoresearch.client.model.InsertDocumentRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SqliteImport {

    public static void main(String[] args) throws Exception {
        SqliteImport sqliteImport = new SqliteImport();
        IndexApi api = new IndexApi();
        ApiClient client = Configuration.getDefaultApiClient();
        client.setBasePath("http://127.0.0.1:9308");
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection con = DriverManager.getConnection("jdbc:sqlite:D:/sqlite/db/chinook.db");
        Statement stmt = con.createStatement();
        stmt.setFetchSize(100);
        List<DocModel> docModels = new ArrayList<>();
        System.out.println("Start");
        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader("D:/Manticore/doc_list0.csv"))) {
            String line;
            br.readLine();
            int index = 0;
            while ((line = br.readLine()) != null) {
                index++;
                if (index >= 0) {
                    DocModel docModel = new DocModel();
                    String[] sp = line.split("\\|", -1);
                    if (sp.length == 9) {
                        docModels.add(docModel);
                        docModel.id = Long.parseLong(sp[0]);
                        docModel.docDate = sp[1].substring(0, 10).replace("-", "");
                        docModel.title = sp[2];
                        docModel.originCode = sp[3];
                        docModel.numPatient = sp[4].replace("\"", "");
                        docModel.docType = sp[5];
                        docModel.age = Double.parseDouble(sp[6]);
                        docModel.sex = sp[7];
                        docModel.birthDate = sp[8].substring(0, 10).replace("-", "");
                    } else {
                        System.out.println(line);
                    }
                    if (index % 1000 == 0) {
                        sqliteImport.writeToManticore(docModels, stmt, index, api);
                    }
                }
            }
            sqliteImport.writeToManticore(docModels, stmt, index, api);
        } catch (Exception e0) {
            e0.printStackTrace();
        }
        try {
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void writeToManticore(List<DocModel> docModels, Statement stmt, int index, IndexApi api){
        try {
            String query = "SELECT DOCUMENT_NUM, DISPLAYED_TEXT\n" +
                    "FROM\n" +
                    "    documents WHERE document_num IN (" +
                    docModels.stream().map(d -> d.id + "").collect(Collectors.joining(",")) +
                    ") ";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                long docNum = rs.getLong(1);
                String doc = rs.getString(2);
                if (doc.length() < 8000000) {
                    DocModel docFound = docModels.stream().filter(d -> d.id == docNum).findFirst().get();
                    InsertDocumentRequest newdoc = new InsertDocumentRequest();
                    newdoc.setId(docFound.id);
                    HashMap<String, Object> docMap = new HashMap<String, Object>() {{
                        put("title", docFound.title);
                        put("patient_num", Integer.parseInt(docFound.numPatient));
                        put("document_date", Integer.parseInt(docFound.docDate));
                        put("origin_code", docFound.originCode);
                        put("document", doc);
                        put("age", docFound.age);
                        put("sex", docFound.sex);
                        put("birth_date", Integer.parseInt(docFound.birthDate));
                        if (docFound.docType != null) put("doc_type", docFound.docType);
                    }};
                    newdoc.index("wh_docs_f").setDoc(docMap);
                    try {
                        api.insert(newdoc);
                    } catch (Exception e1) {
                        System.out.println("Error in doc id " + docFound.id);
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        docModels.clear();
        System.out.println(LocalDateTime.now() + " : " + index);
    }
}
