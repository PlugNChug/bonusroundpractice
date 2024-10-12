package com.github.plugnchug.bonusround.scraper;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BaVScraper {
    private static final int MAX_THREADS = (int) (Runtime.getRuntime().availableProcessors() / 2);

    private static List<String> seasonPageUrlList = Collections.synchronizedList(new ArrayList<>());
    private static List<String> episodeLinks = Collections.synchronizedList(new ArrayList<>());

    private static List<String> bonusAnswers = Collections.synchronizedList(new ArrayList<>());

    public static void ConnectToForum() {
        
        final String url = "https://buyavowel.boards.net/board/84/season-40";
        episodeLinks.clear();
        try {
            long startTime = System.nanoTime();

            ScrapeSeasons(url);
            
            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println(totalTime / 1000000000.0);

            // if (nextPage.attr("class")) {
            //     executorService.submit(() -> {
            //         scrapeBoard();
            //     });
            // }

            // Now, it's time to use multiple threads to grab the bonus round words
            // for (String postUrl : postUrls) {
            //     executorService.submit(() -> {
            //         try {
            //             scrapePost(postUrl);
            //         } catch (IOException e) {
            //             System.err.println("Failed to scrape post: " + postUrl);
            //             e.printStackTrace();
            //         }
            //     });
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ScrapeSeasons(String url) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

        GetSeasonPageList(url);

        for (String page : seasonPageUrlList) {
            executorService.submit(() -> {
                try {
                    ScrapeSeasonPage(page);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(300, TimeUnit.SECONDS);

        Collections.sort(episodeLinks);

    }

    /**
     * Snipes the link on available "Next" buttons on the BaV forums, given an initial input URL.
     * Can (and is meant to) be called recursively.
     * @param url - The input URL
     * @throws IOException
     */
    private static void GetSeasonPageList(String url) throws IOException {
        final Document document = Jsoup.connect(url).get();

        // Add the current page
        seasonPageUrlList.add(url);

        String nextPageUrl = document.selectFirst("li.next").selectFirst("a").absUrl("href");
        if (!nextPageUrl.isEmpty()) {
            GetSeasonPageList(nextPageUrl);
        }
    }

    private static void ScrapeSeasonPage(String page) throws IOException {
        final Document document = Jsoup.connect(page).get();

        // In BaV boards, post links are nested in a link (a) of class "thread-link".
        Elements postLinks = document.select("a.thread-link");

        // Create a list of URLs. We grab the actual links for each post using absUrl.
        List<String> postUrls = new ArrayList<>();
        for (Element link : postLinks) {
            String postUrl = link.absUrl("href");
            
        }

        for (String x : postUrls) {
            episodeLinks.add(x);
        }
    }
    public static void main(String[] args) {

        // GetDecadeRecap("https://buyavowel.boards.net/thread/17400/season-31-40-recap-directory");
        // ConnectToForum();
        System.out.println(MAX_THREADS + " threads");
    }
}
