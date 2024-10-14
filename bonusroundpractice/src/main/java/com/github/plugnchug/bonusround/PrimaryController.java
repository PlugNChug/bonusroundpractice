package com.github.plugnchug.bonusround;

import java.io.IOException;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.fxml.FXML;
import javafx.util.Pair;

public class PrimaryController {

    @FXML
    private void webScrape() throws IOException {
        try {
            BaVScraper.scrapeSeasons("https://buyavowel.boards.net/page/compendiumindex");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    @FXML
    private void beginPuzzle() throws IOException {
        Pair<String, String> answer = BonusGameBackend.getRandomAnswer();
        System.out.println(answer.toString());
    }
}
