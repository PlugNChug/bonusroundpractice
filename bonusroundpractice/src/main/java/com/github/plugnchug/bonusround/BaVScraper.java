package com.github.plugnchug.bonusround;

import java.io.*;

import java.util.*;
import java.util.concurrent.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BaVScraper {
    private static final int MAX_THREADS = (int) (Runtime.getRuntime().availableProcessors() / 2);

    private static List<String> seasonPageUrlList = Collections.synchronizedList(new ArrayList<>());

    private static Set<Puzzle<String, String, Integer>> bonusAnswers = new HashSet<>();

    /**
     * Performs a web scrape of the Buy a Vowel Boards website to obtain all bonus round answers and categories
     * @param url The link to the compendium
     * @throws IOException
     * @throws InterruptedException
     */
    public static void scrapeSeasons(String url) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

        getSeasonPageList(url);

        for (String page : seasonPageUrlList) {
            executorService.submit(() -> {
                try {
                    scrapeSeasonPage(page);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(300, TimeUnit.SECONDS);

        File file = new File("answers.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (var a : bonusAnswers) {
            if (a.getAnswer().isEmpty() || a.getCategory().isEmpty() || a.getCategory().equals("N/A")) {
                continue;
            }
            writer.write(a.getAnswer() + "," + a.getCategory() + "," + a.getSeason() + "\n");
        }
        writer.close();
    }

    /**
     * Given the URL to the compendium's home page, modify seasonPageUrlList to be a list of URLs for each WoF season's compendium page.
     * @param url The input URL
     * @throws IOException
     */
    private static void getSeasonPageList(String url) throws IOException {
        final Document document = Jsoup.connect(url).get();

        // Clear the list so there won't be duplicates on subsequent calls
        seasonPageUrlList.clear();

        // Grabs all the season links and adds it to the list
        Elements links = document.select("div.widget-content").select("a");
        for (Element e : links) {
            seasonPageUrlList.add(e.absUrl("href"));
        }

        // Removes the special links
        for (int i = 0; i < 6; i++) {
            seasonPageUrlList.remove(seasonPageUrlList.size() - 1);
        }
    }

    /*
     * Gets all the bonus round answers/categories in the page and adds it to the bonus round list
     */
    private static void scrapeSeasonPage(String page) throws IOException {
        final Document document = Jsoup.connect(page).get();

        char c = page.charAt(page.length() - 1);
        int counter = 2;
        while (Character.isDigit(c)) {
            c = page.charAt(page.length() - counter);
            counter++;
        }
        int season = Integer.parseInt(page.substring(page.length() - counter + 2, page.length()));


        // In BaV boards, post links are nested in a link (a) of class "thread-link".
        Element table = document.select("div.widget-content").last();
        Elements days = table.select("tr[style='background-color:#5ab473']");
        for (Element bonusContents : days) {
            // A given puzzle will have an answer, category, and season associated with it
            bonusAnswers.add(new Puzzle<String, String, Integer>(bonusContents.select("td").get(0).ownText(), bonusContents.select("td").get(1).ownText(), season));
        }
    }

    public static void main(String[] args) {

        // GetDecadeRecap("https://buyavowel.boards.net/thread/17400/season-31-40-recap-directory");
        try {
            scrapeSeasons("https://buyavowel.boards.net/page/compendiumindex");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(bonusAnswers.size() + " bonus answers loaded");
        System.out.println(MAX_THREADS + " threads");
    }
}
