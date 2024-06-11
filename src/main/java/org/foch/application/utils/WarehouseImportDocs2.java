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

public class WarehouseImportDocs2 {


    public static void main(String[] args) throws Exception {
        IndexApi api = new IndexApi();
        ApiClient client = Configuration.getDefaultApiClient();
        client.setBasePath("http://127.0.0.1:9308");
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection con = DriverManager.getConnection("jdbc:oracle:thin:@XXX", "XXX", "XXXX");
        Statement stmt = con.createStatement();
        stmt.setFetchSize(100);
        List<DocModel> docModels = new ArrayList<>();
        System.out.println("Start");
        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader("D:/manticore/doc_list.csv"))) {
            String line;
            br.readLine();
            int index = 0;
            while ((line = br.readLine()) != null && index < 20000000) {
                index++;
                if (index > 0) {
                    DocModel docModel = new DocModel();
                    docModels.add(docModel);
                    String[] sp = line.split("\\|", -1);
                    docModel.id = Long.parseLong(sp[1]);
                    docModel.docDate = sp[2].substring(0, 10).replace("-", "");
                    docModel.title = sp[3];
                    docModel.originCode = sp[4];
                    docModel.numPatient = sp[5].replace("\"", "");
                    docModel.docType = sp[6];
                    docModel.age = Double.parseDouble(sp[7]);
                    docModel.sex = sp[8];
                    docModel.birthDate = sp[9].substring(0, 10).replace("-", "");
                    if (index % 1000 == 0) {
                        try {
                            String query = "SELECT DOCUMENT_NUM, DISPLAYED_TEXT\n" +
                                    "FROM\n" +
                                    "    DWH.DWH_DOCUMENT WHERE document_num IN (" +
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
            }
        } catch (Exception e0) {
            e0.printStackTrace();
        }
        try {
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}