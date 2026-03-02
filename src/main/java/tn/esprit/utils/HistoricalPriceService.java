package tn.esprit.utils;

import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class HistoricalPriceService {

    private static final String BINANCE_KLINE_API = "https://api.binance.com/api/v3/klines";

    /**
     * Fetches historical closing prices for a given symbol over the last 30 days.
     * 
     * @param symbol The asset symbol (e.g., BTC, ETH)
     * @return Map of Date String (MM-dd) to Closing Price
     */
    public static Map<String, Double> getHistoricalPrices(String symbol) {
        Map<String, Double> priceHistory = new LinkedHashMap<>();

        try {
            // Assume pairing against USDT for crypto assets
            String endpoint = BINANCE_KLINE_API + "?symbol=" + symbol.toUpperCase() + "USDT&interval=1d&limit=30";
            URL url = new URL(endpoint);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5 seconds timeout
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONArray klines = new JSONArray(content.toString());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd").withZone(ZoneId.systemDefault());

                // Binance Kline array format:
                // [0] Open time
                // [1] Open
                // [2] High
                // [3] Low
                // [4] Close <-- We want this
                // ...
                for (int i = 0; i < klines.length(); i++) {
                    JSONArray kline = klines.getJSONArray(i);
                    long openTimeMs = kline.getLong(0);
                    double closePrice = Double.parseDouble(kline.getString(4));

                    String dateLabel = formatter.format(Instant.ofEpochMilli(openTimeMs));
                    priceHistory.put(dateLabel, closePrice);
                }
            } else {
                System.err.println("API Request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical prices for " + symbol + ": " + e.getMessage()
                    + ". Generating mock data.");
            // Fallback to mock data if API is unreachable
            return generateMockHistory(symbol);
        }

        return priceHistory;
    }

    private static Map<String, Double> generateMockHistory(String symbol) {
        Map<String, Double> mockHistory = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd").withZone(ZoneId.systemDefault());

        // Base price approximation
        double basePrice = 100.0;
        switch (symbol.toUpperCase()) {
            case "BTC":
                basePrice = 60000.0;
                break;
            case "ETH":
                basePrice = 3000.0;
                break;
            case "SOL":
                basePrice = 140.0;
                break;
            case "BNB":
                basePrice = 600.0;
                break;
        }

        // Generate 30 days of synthetic sine-wave data with random noise
        double currentPrice = basePrice;
        long currentTime = Instant.now().toEpochMilli();
        long dayMs = 24L * 60 * 60 * 1000;
        long startTime = currentTime - (30 * dayMs);

        for (int i = 0; i < 30; i++) {
            String dateLabel = formatter.format(Instant.ofEpochMilli(startTime + (i * dayMs)));

            // Random walk fluctuation (+/- 2%)
            double fluctuation = currentPrice * (0.04 * Math.random() - 0.02);
            currentPrice += fluctuation;

            mockHistory.put(dateLabel, currentPrice);
        }

        return mockHistory;
    }
}
