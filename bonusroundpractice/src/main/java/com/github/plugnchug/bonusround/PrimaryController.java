package com.github.plugnchug.bonusround;

import java.io.IOException;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        BaVScraper.ConnectToForum();
        // Window.setRoot("secondary");
    }
}
