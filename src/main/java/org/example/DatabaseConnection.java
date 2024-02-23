package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/Article_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    private Connection connection;

    public DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = getConnection();
            System.out.println("Connected to the database.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        return conn;
    }

    public void saveToDb(String author, String dateFrom, String articleTitle, String abstractText, String releaseDate,
                         String magazineTitle, String doi, int quoteNub) {

            try {
                connection.setAutoCommit(false);
                String questionsInsertQuery = "INSERT INTO Questions (author, from_date) VALUES (?, ?)";
                PreparedStatement questionsStatement = connection.prepareStatement(questionsInsertQuery, Statement.RETURN_GENERATED_KEYS);
                questionsStatement.setString(1, author);
                questionsStatement.setString(2, dateFrom);
                questionsStatement.executeUpdate();

                ResultSet generatedKeys = questionsStatement.getGeneratedKeys();

                if (generatedKeys.next()) {
                    int questionId = generatedKeys.getInt(1);

                    String responseInsertQuery = "INSERT INTO Response (article_title, abstract, authors, release_date, magazine_title, doi, quote_nub, query_reference_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement responseStatement = connection.prepareStatement(responseInsertQuery);
                    responseStatement.setString(1, articleTitle);
                    responseStatement.setString(2, abstractText);
                    responseStatement.setString(3, author);
                    responseStatement.setString(4, releaseDate);
                    responseStatement.setString(5, magazineTitle);
                    responseStatement.setString(6, doi);
                    responseStatement.setInt(7, quoteNub);
                    responseStatement.setInt(8, questionId);
                    responseStatement.executeUpdate();

                    connection.commit();
                } else {
                    System.out.println("Failed to obtain the generated ID for the question.");
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

    }


    public List<Response> getAllResponses() {
        List<Response> responses = new ArrayList<>();
        try {
            String selectQuery = "SELECT r.article_title, r.abstract, r.authors, r.release_date, " +
                    "r.magazine_title, r.doi, r.quote_nub, q.author, q.from_date " +
                    "FROM Response r " +
                    "JOIN Questions q ON r.query_reference_id = q.id";


            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery);
            while (resultSet.next()) {
                String articleTitle = resultSet.getString("article_title");
                String abstractText = resultSet.getString("abstract");
                String authors = resultSet.getString("authors");
                String releaseDate = resultSet.getString("release_date");
                String magazineTitle = resultSet.getString("magazine_title");
                String doi = resultSet.getString("doi");
                int quoteNub = resultSet.getInt("quote_nub");

                String author = resultSet.getString("author");
                String dateFrom = resultSet.getString("from_date");

                Response response = new Response(articleTitle, abstractText, authors,
                        releaseDate, magazineTitle, doi, quoteNub, author, dateFrom);
                responses.add(response);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return responses;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Closed the database connection.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
