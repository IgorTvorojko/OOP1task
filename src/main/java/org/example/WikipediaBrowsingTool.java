package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class WikipediaApiClient {
    public List<SearchResult> search(String query) throws Exception {
        String encodedRequest = URLEncoder.encode(query, "UTF-8");
        URL url = new URL("https://ru.wikipedia.org/w/api.php?action=query&list=search&utf8=&format=json&srsearch=" + encodedRequest);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
        JsonArray searchResults = jsonResponse.getAsJsonObject("query").getAsJsonArray("search");

        List<SearchResult> results = new ArrayList<>();
        for (int i = 0; i < searchResults.size(); i++) {
            JsonObject result = searchResults.get(i).getAsJsonObject();
            String title = result.get("title").getAsString();
            int pageId = result.get("pageid").getAsInt();
            results.add(new SearchResult(title, pageId));
        }
        return results;
    }
}

class SearchResult {
    private final String title;
    private final int pageId;

    public SearchResult(String title, int pageId) {
        this.title = title;
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return "https://ru.wikipedia.org/w/index.php?curid=" + pageId;
    }
}

public class WikipediaBrowsingTool {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        WikipediaApiClient apiClient = new WikipediaApiClient();

        System.out.println("Formulate request:");
        String request = sc.nextLine();

        try {
            List<SearchResult> results = apiClient.search(request);
            if (results.isEmpty()) {
                System.out.println("No information found");
                return;
            }

            System.out.println("Browsing results:");
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i).getTitle());
            }

            System.out.println("Enter 0 to exit or number of desired article");
            int choice = sc.nextInt();
            if (choice <= 0 || choice > results.size()) {
                System.out.println("Exiting the problem");
                return;
            }

            SearchResult selectedResult = results.get(choice - 1);
            openInBrowser(selectedResult.getUrl());
        } catch (Exception e) {
            System.out.println("An error occured: " + e.getMessage());
        }
    }

    private static void openInBrowser(String url) throws Exception {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(url));
            System.out.println("Article opened in browser: " + url);
        } else {
            System.out.println("Cannot open");
        }
    }
}