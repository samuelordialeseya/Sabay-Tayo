package com.sabaytayo;

import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    public void initialize() {
        System.out.println("Start Menu Loaded!");
    }

    @FXML
    private void onStartGame() {
        System.out.println("Start Game clicked!");
    }

    @FXML
    private void onLoadGame() {
        System.out.println("Load Game clicked!");
    }

    @FXML
    private void onSettings() {
        System.out.println("Settings clicked!");
    }
}
