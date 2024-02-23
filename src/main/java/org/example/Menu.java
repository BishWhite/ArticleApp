package org.example;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class Menu extends JFrame {

    private static final int BUTTON_WIDTH = 400;
    private static final int BUTTON_HEIGHT = 50;
    private JPanel downloadedPanel;
    private JPanel mainPanel;
    private DatabaseConnection con;

    private static String truncateAbstract(String originalAbstract, int maxLength) {
        if (originalAbstract.length() > maxLength) {
            return originalAbstract.substring(0, maxLength);
        } else {
            return originalAbstract;
        }
    }

    private void showDownloadedPanel() {
        if (downloadedPanel == null) {
            downloadedPanel = new JPanel();
            downloadedPanel.setLayout(new BorderLayout());
        }

        DefaultTableModel tableModel = new DefaultTableModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        tableModel.addColumn("Article Title");
        tableModel.addColumn("Abstract");
        tableModel.addColumn("Authors");
        tableModel.addColumn("Release date");
        tableModel.addColumn("Magazine");
        tableModel.addColumn("doi");
        tableModel.addColumn("Reference count");
        tableModel.addColumn("Author query");
        tableModel.addColumn("Date from query");

        List<Response> responses = con.getAllResponses();

        for (Response response : responses) {
            Object[] rowData = {response.getArticleTitle(), response.getAbstractText(), response.getAuthors(),
                    response.getReleaseDate(), response.getMagazineTitle(), response.getDoi(), response.getQuoteNub(),
                    response.getAuthorQuery(), response.getDateFromQuery()};
            tableModel.addRow(rowData);
        }

        JTable dataTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        downloadedPanel.removeAll();
        downloadedPanel.add(scrollPane, BorderLayout.CENTER);

        Dimension buttonSize = new Dimension(400, 80);
        JButton backButtonDownloaded = new JButton("Back");
        backButtonDownloaded.setPreferredSize(buttonSize);
        downloadedPanel.add(backButtonDownloaded, BorderLayout.SOUTH);
        backButtonDownloaded.addActionListener(e -> {
            downloadedPanel.setVisible(false);
            mainPanel.setVisible(true);
        });

        downloadedPanel.setPreferredSize(new Dimension(600, 600));
        downloadedPanel.setVisible(true);

        getContentPane().add(downloadedPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }




    public Menu() {
        this.con = new DatabaseConnection();
        setTitle("Article Downloader App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 1200);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(80, 10, 50, 10); // Margins

        JTextField authorField = new JTextField();

        JLabel fromDateLabel = new JLabel("From Date: ");
        JComboBox<String> fromDatePicker = createDatePicker();
        fromDatePicker.setEditable(false);

        authorField.setPreferredSize(new Dimension(400, 40));

        fromDatePicker.setPreferredSize(new Dimension(250, 50));

        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("Author: "), gbc);
        gbc.gridx = 1;
        filterPanel.add(authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        filterPanel.add(fromDateLabel, gbc);
        gbc.gridx = 1;
        filterPanel.add(fromDatePicker, gbc);

        JButton downloadButton = new JButton("Download Articles");
        JButton downloadedButton = new JButton("Downloaded Articles");

        Dimension buttonSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        downloadButton.setPreferredSize(buttonSize);
        downloadedButton.setPreferredSize(buttonSize);
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String author = authorField.getText();
                String fromDate = (String) fromDatePicker.getSelectedItem();
                String apiUrl1 = "https://api.crossref.org/works";
                StringBuilder queryBuilder = new StringBuilder("?");

                if (!author.isEmpty()) {
                    try {
                        queryBuilder.append("query.author=").append(URLEncoder.encode(author, "UTF-8")).append("&");
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    return;
                }

                if (!fromDate.isEmpty()) {
                    try {
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate localDate = LocalDate.parse(fromDate, inputFormatter);

                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String formattedDate = localDate.format(outputFormatter);

                        queryBuilder.append("filter=from-pub-date:"+formattedDate);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

                if (queryBuilder.charAt(queryBuilder.length() - 1) == '&') {
                    queryBuilder.setLength(queryBuilder.length() - 1);
                }

                String finalApiUrl = apiUrl1 + queryBuilder.toString();

                System.out.println("Crossref API URL: " + finalApiUrl);
                try {
                    String responseData = ApiConnector.fetchDataFromApi(finalApiUrl);

                    ObjectMapper objectMapper = new ObjectMapper();

                    try {
                        JsonNode root = objectMapper.readTree(responseData);
                        JsonNode items = root.path("message").path("items");

                        if (items.isArray()) {
                            for (JsonNode articleNode : items) {
                                String title = articleNode.path("title").isArray() ?
                                        articleNode.path("title").get(0).asText() :
                                        articleNode.path("title").asText();
                                String abstractText = articleNode.path("abstract").asText();
                                abstractText = truncateAbstract(abstractText, 255);
                                String doi = articleNode.path("DOI").asText();
                                JsonNode publishedDateNode = articleNode.path("published").path("date-parts").get(0);
                                StringBuilder publishedDateBuilder = new StringBuilder();
                                for (JsonNode datePart : publishedDateNode) {
                                    publishedDateBuilder.append(datePart.asText()).append("-");
                                }
                                String publishedDate = publishedDateBuilder.toString().replaceAll("-$", "");

                                String magazine = articleNode.path("container-title").isArray() ?
                                        articleNode.path("container-title").get(0).asText() :
                                        articleNode.path("container-title").asText();

                                int referencedCount = articleNode.path("is-referenced-by-count").asInt();

                                StringBuilder authorsBuilder = new StringBuilder();
                                JsonNode authorsArray = articleNode.path("author");
                                for (JsonNode authorNode : authorsArray) {
                                    String givenName = authorNode.path("given").asText();
                                    String familyName = authorNode.path("family").asText();
                                    authorsBuilder.append(givenName).append(" ").append(familyName).append(", ");
                                }
                                String authors = authorsBuilder.toString().replaceAll(", $", "");

                                System.out.println("Title: " + title);
                                System.out.println("Abstract: " + abstractText);
                                System.out.println("DOI: " + doi);
                                System.out.println("Published Date: " + publishedDate);
                                System.out.println("Magazine: " + magazine);
                                System.out.println("Referenced Count: " + referencedCount);
                                System.out.println("Authors: " + authors);
                                System.out.println("--------------");
                                con.saveToDb(author, fromDate, title, abstractText, publishedDate, magazine, doi, referencedCount);
                            }
                        } else {
                            System.out.println("No articles found in the response.");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        downloadedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.setVisible(false);
                showDownloadedPanel();
            }
        });

        mainPanel.add(filterPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(downloadButton);
        buttonPanel.add(downloadedButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        getContentPane().add(mainPanel);
    }

    private JComboBox<String> createDatePicker() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Calendar startDate = Calendar.getInstance();
        startDate.set(2000, Calendar.JANUARY, 1);

        List<String> dateList = new LinkedList<>();
        Calendar currentDate = Calendar.getInstance();

        while (startDate.before(currentDate)) {
            dateList.add(dateFormat.format(startDate.getTime()));
            startDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        String[] dates = dateList.toArray(new String[0]);

        JComboBox<String> datePicker = new JComboBox<>(dates);

        return datePicker;
    }

}