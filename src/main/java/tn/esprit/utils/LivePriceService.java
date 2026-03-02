package tn.esprit.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONObject;

public class LivePriceService {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)) // Don't block UI for too long if offline
            .build();

    /**
     * Fetches the live price of a crypto asset from Binance Public API.
     * 
     * @param symbol The asset symbol (e.g., BTC, ETH)
     * @return The live price as double, or -1.0 if not found/error.
     */
    public static double getPrice(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return -1.0;
        }

        try {
            // Binance requires pairs like BTCUSDT
            String pair = symbol.toUpperCase() + "USDT";
            String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + pair;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                return json.getDouble("price");
            }
        } catch (Exception e) {
            System.err.println(
                    "Could not fetch live price for " + symbol + ": " + e.getMessage() + ". Using mock fallback data.");
        }

        // --- Demo Fallback Data in case of Network Failure ---
        return getMockPrice(symbol);
    }

    private static double getMockPrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC":
                return 64230.50;
            case "ETH":
                return 3450.75;
            case "SOL":
                return 145.20;
            case "BNB":
                return 580.10;
            case "ADA":
                return 0.45;
            case "XRP":
                return 0.55;
            default:
                return 100.00;
        }
    }
}
