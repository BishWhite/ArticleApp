package org.example;

import javax.swing.*;

public class Main {

    private void createGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Menu menu = new Menu();
                menu.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        new Main().createGUI();
    }
}