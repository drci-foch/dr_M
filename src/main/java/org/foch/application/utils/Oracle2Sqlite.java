package org.foch.application.utils;



import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Oracle2Sqlite {

    public static void main(String[] args) throws Exception {
        String sqliteUrl = "jdbc:sqlite:C:/Users/elsmou/sqlite/text11.db";
        Connection sqliteConn = DriverManager.getConnection(sqliteUrl);
        createTableIfNotExists(sqliteConn);
        sqliteConn.setAutoCommit(false);
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@XXX", "XXX", "XXX");
        Statement stmt = con.createStatement();
        stmt.setFetchSize(100);

        List<DocModel> docModels = new ArrayList<>();
        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader("C:/Users/elsmou/sqlite/doc_list1.csv"))) {
            String line;
            br.readLine();
            int index = 0;
            while ((line = br.readLine()) != null) {
                index++;
                //text6 s'arrete a 14540986
                //text7 s'arrete a 15150000
                //test 8 183654 + 15150000
                //600037+15333654
                if (index >= 0) {
                    DocModel docModel = new DocModel();
                    String[] sp = line.split("\\|", -1);
                    if (sp.length == 9) {
                        docModels.add(docModel);
                        docModel.id = Long.parseLong(sp[0]);
                    } else {
                        System.out.println(line);
                    }
                    if (index % 100 == 0) {
                        writeToSqlite(docModels, stmt, sqliteConn, index);
                    }
                }
            }
        } catch (Exception e0) {
            e0.printStackTrace();
        }
        sqliteConn.close();
        con.close();
    }

    private static void writeToSqlite(List<DocModel> docModels, Statement stmt, Connection sqliteConn, int index) {
        try {
            String query = "SELECT DOCUMENT_NUM, DISPLAYED_TEXT\n" +
                    "FROM\n" +
                    "    DWH.DWH_DOCUMENT WHERE document_num IN (" +
                    docModels.stream().map(d -> d.id + "").collect(Collectors.joining(",")) +
                    ") ";
            String insertSQL = "INSERT INTO documents(document_num, text) VALUES(?,?)";
            ResultSet rs = stmt.executeQuery(query);
            PreparedStatement pstmt = sqliteConn.prepareStatement(insertSQL);
            while (rs.next()) {
                long docNum = rs.getLong(1);
                String doc = rs.getString(2);
                if (doc.length() < 8000000) {
                    pstmt.setLong(1, docNum);
                    pstmt.setBytes(2, doc.getBytes(StandardCharsets.UTF_8));
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            sqliteConn.commit();
        } catch (Exception e2) {
            System.out.println("error at : " +docModels.get(0).id);
            e2.printStackTrace();
        }
        docModels.clear();
        System.out.println(LocalDateTime.now() + " : " + index);
    }

    public static void createTableIfNotExists(Connection sqliteConn) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS documents (" +
                "document_num LONG PRIMARY KEY, " +
                "text BLOB)";
        try (Statement stmt = sqliteConn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }
}
