package org.foch.application.utils;


import java.sql.*;

public class SQLiteDBCounter {
    public static void main(String[] args) {
        // Define the base name of the SQLite databases
        String baseName = "text";
        int numDatabases = 10;

        // Define the total count of documents
        int totalCount = 0;

        // Loop through each database
        for (int i = 2; i <= numDatabases; i++) {
            String dbName = "C:/Users/elsmou/sqlite/" + baseName + i + ".db";
            int count = countDocuments(dbName);
            System.out.println("Database " + dbName + ": " + count + " documents");
            totalCount += count;
        }

        // Output the total count
        System.out.println("Total count of documents across all databases: " + totalCount);
    }

    // Method to count documents in a SQLite database
    private static int countDocuments(String dbName) {
        int count = 0;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Connect to the database
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);

            // Create a statement
            statement = connection.createStatement();

            // Execute query to count tables named 'documents'
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM documents");

            // Retrieve the count
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count;
    }
}
