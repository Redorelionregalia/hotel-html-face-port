package hotel.tests;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hotel.app.HtmlServer;

public class WebFlowSmokeTest {
    public static void main(String[] args) throws Exception {
        HtmlServer.ensureStarted();
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest menuRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:18080/api/menu"))
                .GET()
                .build();
        HttpResponse<String> menuResponse = client.send(menuRequest, HttpResponse.BodyHandlers.ofString());
        if (menuResponse.statusCode() != 200) {
            throw new AssertionError("Menu request failed: " + menuResponse.statusCode());
        }
        if (!menuResponse.body().contains("Club Sandwich") || !menuResponse.body().contains("House Red Wine")) {
            throw new AssertionError("Menu payload missing expected records.");
        }

        String payload = "[{\"id\":6,\"quantity\":1}]";
        String orderBody = "room=" + URLEncoder.encode("501", StandardCharsets.UTF_8)
                + "&notes=" + URLEncoder.encode("Smoke test", StandardCharsets.UTF_8)
                + "&cart=" + URLEncoder.encode(payload, StandardCharsets.UTF_8);

        HttpRequest orderRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:18080/api/orders"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(orderBody))
                .build();
        HttpResponse<String> orderResponse = client.send(orderRequest, HttpResponse.BodyHandlers.ofString());
        if (orderResponse.statusCode() != 200) {
            throw new AssertionError("Order placement failed: HTTP " + orderResponse.statusCode() + " body=" + orderResponse.body());
        }
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(orderResponse.body());
        if (!matcher.find()) {
            throw new AssertionError("Order response missing id: " + orderResponse.body());
        }
        int orderId = Integer.parseInt(matcher.group(1));

        HttpRequest loginRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:18080/api/staff/login"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("username=staff&password=staff123"))
                .build();
        HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        if (loginResponse.statusCode() != 200 || !loginResponse.body().contains("\"ok\":true")) {
            throw new AssertionError("Staff login failed: " + loginResponse.body());
        }

        HttpRequest statusRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:18080/api/orders/" + orderId + "/status"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString("status=COMPLETED"))
                .build();
        HttpResponse<String> statusResponse = client.send(statusRequest, HttpResponse.BodyHandlers.ofString());
        if (statusResponse.statusCode() != 200) {
            throw new AssertionError("Status update failed: " + statusResponse.body());
        }

        HttpRequest ordersRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:18080/api/orders"))
                .GET()
                .build();
        HttpResponse<String> ordersResponse = client.send(ordersRequest, HttpResponse.BodyHandlers.ofString());
        if (ordersResponse.statusCode() != 200 || !ordersResponse.body().contains("COMPLETED")) {
            throw new AssertionError("Orders response missing updated status: " + ordersResponse.body());
        }

        System.out.println("HTML backend smoke test passed.");
    }
}
