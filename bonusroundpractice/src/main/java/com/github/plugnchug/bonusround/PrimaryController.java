package com.github.plugnchug.bonusround;

import java.io.IOException;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        try {
            BaVScraper.ScrapeSeasons("https://buyavowel.boards.net/page/compendiumindex");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Window.setRoot("secondary");
    }
}
