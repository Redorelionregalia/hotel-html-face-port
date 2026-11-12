package hotel.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import hotel.data.ShopException;
import hotel.data.ShopService;
import hotel.model.Cart;
import hotel.model.Category;
import hotel.model.Item;
import hotel.model.Order;
import hotel.model.OrderLine;
import hotel.model.OrderStatus;
import hotel.ui.Theme;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local, offline HTTP adapter over the existing ShopService and file repositories. */
public final class HtmlServer {
    private static final int PORT = 18080;
    private static final Object START_LOCK = new Object();
    private static final Map<String, Boolean> STAFF_SESSIONS = new ConcurrentHashMap<>();
    private static volatile HttpServer server;
    private static volatile ShopService shop;

    private HtmlServer() { }

    /** Starts exactly one listener in this process; the OS port bind prevents a second process listener. */
    public static void ensureStarted() {
        synchronized (START_LOCK) {
            if (server != null) return;
            try {
                shop = AppContext.create().shop();
                HttpServer candidate = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
                candidate.setExecutor(Executors.newCachedThreadPool(new DaemonThreadFactory()));
                candidate.createContext("/api/health", exchange -> sendJson(exchange, 200, "{\"ok\":true,\"mode\":\"offline\"}"));
                candidate.createContext("/api/menu", new MenuHandler());
                candidate.createContext("/api/orders", new OrdersHandler());
                candidate.createContext("/api/items", new ItemsHandler());
                candidate.createContext("/api/staff/login", new StaffLoginHandler());
                candidate.createContext("/api/staff/logout", new StaffLogoutHandler());
                candidate.createContext("/images/", new ImageHandler());
                candidate.createContext("/", new StaticHandler());
                candidate.start();
                server = candidate;
            } catch (BindException alreadyRunning) {
                // A server from another launcher/process already owns the local application port.
                // Verify it is this app's server rather than silently accepting a port collision.
                if (!isExistingHotelServer()) throw new ShopException("Port " + PORT + " is already in use.", alreadyRunning);
            } catch (IOException e) {
                throw new ShopException("Could not start the local hotel backend.", e);
            }
        }
    }

    public static void openBrowser(String page) {
        ensureStarted();
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create("http://127.0.0.1:" + PORT + "/" + page));
            }
        } catch (Exception ignored) {
            // Users can open http://127.0.0.1:18080 manually if browser launching is unavailable.
        }
    }

    private static boolean isExistingHotelServer() {
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                    new URI("http://127.0.0.1:" + PORT + "/api/health").toURL().openConnection();
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            boolean healthy = connection.getResponseCode() == 200;
            connection.disconnect();
            return healthy;
        } catch (Exception e) {
            return false;
        }
    }

    private static ShopService shop() {
        ensureStarted();
        return shop;
    }

    private static void sendJson(HttpExchange exchange, int status, String value) throws IOException {
        byte[] body = value.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) { out.write(body); }
    }

    private static String body(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> form(String encoded) {
        Map<String, String> values = new LinkedHashMap<>();
        if (encoded == null || encoded.isEmpty()) return values;
        for (String part : encoded.split("&")) {
            int split = part.indexOf('=');
            String key = split < 0 ? part : part.substring(0, split);
            String value = split < 0 ? "" : part.substring(split + 1);
            try {
                values.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Malformed form encoding.");
            } catch (java.io.UnsupportedEncodingException impossible) {
                throw new AssertionError(impossible);
            }
        }
        return values;
    }

    private static boolean staffAuthorized(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return false;
        for (String part : cookie.split(";")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && "hotel_staff".equals(pair[0]) && STAFF_SESSIONS.containsKey(pair[1])) return true;
        }
        return false;
    }

    private static boolean requireStaff(HttpExchange exchange) throws IOException {
        if (staffAuthorized(exchange)) return true;
        sendJson(exchange, 401, "{\"error\":\"Staff sign-in required.\"}");
        return false;
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        return out.toString();
    }

    private static String jsonItem(Item item) {
        File image = shop().images().find(item);
        return "{\"id\":" + item.getId()
                + ",\"name\":\"" + escapeJson(item.getName()) + "\""
                + ",\"description\":\"" + escapeJson(item.getDescription()) + "\""
                + ",\"category\":\"" + item.getCategory().name() + "\""
                + ",\"categoryLabel\":\"" + escapeJson(item.getCategory().getLabel()) + "\""
                + ",\"price\":" + item.getPrice()
                + ",\"stock\":" + item.getStock()
                + ",\"emoji\":\"" + escapeJson(item.getEmoji()) + "\""
                + ",\"image\":" + (image == null ? "null" : "\"/images/" + escapeJson(image.getName()) + "\"") + "}";
    }

    private static String jsonMenu(List<Item> items) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) out.append(',');
            out.append(jsonItem(items.get(i)));
        }
        return out.append(']').toString();
    }

    private static String jsonOrder(Order order) {
        StringBuilder lines = new StringBuilder("[");
        List<OrderLine> orderLines = order.getLines();
        for (int i = 0; i < orderLines.size(); i++) {
            if (i > 0) lines.append(',');
            OrderLine line = orderLines.get(i);
            lines.append("{\"name\":\"").append(escapeJson(line.getName())).append("\",\"quantity\":")
                    .append(line.getQuantity()).append(",\"unitPrice\":").append(line.getUnitPrice())
                    .append(",\"total\":").append(line.getTotal()).append('}');
        }
        lines.append(']');
        return "{\"id\":" + order.getId()
                + ",\"room\":\"" + escapeJson(order.getRoom()) + "\""
                + ",\"notes\":\"" + escapeJson(order.getNotes()) + "\""
                + ",\"status\":\"" + order.getStatus().name() + "\""
                + ",\"statusLabel\":\"" + escapeJson(order.getStatus().getLabel()) + "\""
                + ",\"createdAt\":" + order.getCreatedAt()
                + ",\"updatedAt\":" + order.getUpdatedAt()
                + ",\"total\":" + order.getTotal()
                + ",\"lines\":" + lines + "}";
    }

    private static String jsonOrders(List<Order> orders) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) out.append(',');
            out.append(jsonOrder(orders.get(i)));
        }
        return out.append(']').toString();
    }

    private static Map<Integer, Integer> parseCart(String json) {
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        Pattern itemPattern = Pattern.compile("\\{\\s*\"id\"\\s*:\\s*(\\d+)\\s*,\\s*\"quantity\"\\s*:\\s*(\\d+)\\s*\\}");
        Matcher matcher = itemPattern.matcher(json == null ? "" : json);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            int quantity = Integer.parseInt(matcher.group(2));
            if (quantity > 0) quantities.put(id, quantity);
        }
        return quantities;
    }

    private static String htmlEscape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static final class MenuHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            sendJson(exchange, 200, jsonMenu(shop().getMenu()));
        }
    }

    private static final class OrdersHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if ("GET".equals(method)) {
                if (!staffAuthorized(exchange)) {
                    String rawId = exchange.getRequestURI().getRawQuery();
                    if (rawId == null || !rawId.matches("id=\\d+")) {
                        sendJson(exchange, 401, "{\"error\":\"Order number required.\"}");
                        return;
                    }
                    int id = Integer.parseInt(rawId.substring(3));
                    Order order = shop().findOrder(id);
                    if (order == null) sendJson(exchange, 404, "{\"error\":\"Order not found.\"}");
                    else sendJson(exchange, 200, jsonOrder(order));
                    return;
                }
                sendJson(exchange, 200, jsonOrders(shop().getOrders()));
                return;
            }
            if ("POST".equals(method) && "/api/orders".equals(path)) {
                try {
                    Map<String, String> values = form(body(exchange));
                    Map<Integer, Integer> quantities = parseCart(values.get("cart"));
                    if (values.getOrDefault("room", "").trim().isEmpty() || quantities.isEmpty()) {
                        sendJson(exchange, 400, "{\"error\":\"Enter a room number and add at least one item.\"}");
                        return;
                    }
                    ShopService service = shop();
                    Cart cart = new Cart();
                    for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                        Item item = null;
                        for (Item candidate : service.getMenu()) if (candidate.getId() == entry.getKey()) item = candidate;
                        if (item == null) {
                            sendJson(exchange, 400, "{\"error\":\"An item in your cart is no longer on the menu.\"}");
                            return;
                        }
                        cart.add(item, entry.getValue());
                    }
                    Order order = service.placeOrder(values.get("room"), values.getOrDefault("notes", ""), cart);
                    sendJson(exchange, 201, jsonOrder(order));
                } catch (RuntimeException e) {
                    sendJson(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
                return;
            }
            if (path.matches("/api/orders/\\d+/status") && "PUT".equals(method)) {
                if (!requireStaff(exchange)) return;
                try {
                    int id = Integer.parseInt(path.replaceFirst("/api/orders/", "").replaceFirst("/status", ""));
                    OrderStatus status = OrderStatus.valueOf(form(body(exchange)).getOrDefault("status", ""));
                    shop().changeStatus(id, status);
                    sendJson(exchange, 200, "{\"ok\":true}");
                } catch (RuntimeException e) {
                    sendJson(exchange, 400, "{\"error\":\"Invalid order status.\"}");
                }
                return;
            }
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private static final class ItemsHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (!requireStaff(exchange)) return;
            try {
                if ("POST".equals(method) && "/api/items".equals(path)) {
                    Map<String, String> values = form(body(exchange));
                    String name = values.getOrDefault("name", "").trim();
                    if (name.isEmpty()) throw new IllegalArgumentException("Item name is required.");
                    Category category = Category.valueOf(values.getOrDefault("category", "FOOD"));
                    Item item = new Item(name, category, values.getOrDefault("description", ""),
                            Double.parseDouble(values.getOrDefault("price", "0")),
                            Integer.parseInt(values.getOrDefault("stock", "0")), values.getOrDefault("emoji", ""));
                    shop().addItem(item);
                    sendJson(exchange, 201, "{\"ok\":true}");
                    return;
                }
                if (path.matches("/api/items/\\d+") && "PUT".equals(method)) {
                    int id = Integer.parseInt(path.substring("/api/items/".length()));
                    Map<String, String> values = form(body(exchange));
                    shop().editItem(id, Double.parseDouble(values.getOrDefault("price", "0")),
                            Integer.parseInt(values.getOrDefault("stock", "0")));
                    sendJson(exchange, 200, "{\"ok\":true}");
                    return;
                }
                if (path.matches("/api/items/\\d+") && "DELETE".equals(method)) {
                    shop().removeItem(Integer.parseInt(path.substring("/api/items/".length())));
                    sendJson(exchange, 200, "{\"ok\":true}");
                    return;
                }
                if (path.matches("/api/items/\\d+/image") && "POST".equals(method)) {
                    int id = Integer.parseInt(path.replaceFirst("/api/items/", "").replaceFirst("/image", ""));
                    Map<String, String> values = form(body(exchange));
                    byte[] imageBytes = Base64.getDecoder().decode(values.getOrDefault("data", ""));
                    if (imageBytes.length == 0 || imageBytes.length > 5 * 1024 * 1024) throw new IllegalArgumentException("Image must be less than 5 MB.");
                    Item item = null;
                    for (Item candidate : shop().getMenu()) if (candidate.getId() == id) item = candidate;
                    if (item == null) throw new IllegalArgumentException("Menu item not found.");
                    String filename = shop().images().importImage(writeTemporaryImage(imageBytes, values.getOrDefault("filename", "photo.png")), item.getName());
                    shop().setItemImage(id, filename);
                    sendJson(exchange, 200, "{\"ok\":true}");
                    return;
                }
            } catch (RuntimeException e) {
                sendJson(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                return;
            }
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }

        private File writeTemporaryImage(byte[] data, String originalName) throws IOException {
            String extension = ".png";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) {
                String candidate = originalName.substring(dot).toLowerCase();
                if (candidate.matches("\\.(png|jpe?g|gif)")) extension = candidate;
            }
            File temp = File.createTempFile("hotel-upload-", extension, AppContext.baseDirectory());
            Files.write(temp.toPath(), data);
            temp.deleteOnExit();
            return temp;
        }
    }

    private static final class StaffLoginHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> values = form(body(exchange));
            Theme theme = new Theme(new File(AppContext.baseDirectory(), "config/theme.properties"));
            if (values.getOrDefault("username", "").equals(theme.text("staff.username"))
                    && values.getOrDefault("password", "").equals(theme.text("staff.password"))) {
                String token = UUID.randomUUID().toString();
                STAFF_SESSIONS.put(token, Boolean.TRUE);
                exchange.getResponseHeaders().add("Set-Cookie", "hotel_staff=" + token + "; Path=/; HttpOnly; SameSite=Strict");
                sendJson(exchange, 200, "{\"ok\":true}");
            } else {
                sendJson(exchange, 401, "{\"ok\":false,\"error\":\"Incorrect username or password.\"}");
            }
        }
    }

    private static final class StaffLogoutHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookie != null) {
                for (String part : cookie.split(";")) {
                    String[] pair = part.trim().split("=", 2);
                    if (pair.length == 2 && "hotel_staff".equals(pair[0])) STAFF_SESSIONS.remove(pair[1]);
                }
            }
            exchange.getResponseHeaders().add("Set-Cookie", "hotel_staff=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
            sendJson(exchange, 200, "{\"ok\":true}");
        }
    }

    private static final class ImageHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            String requested = exchange.getRequestURI().getPath().substring("/images/".length());
            if (requested.contains("/") || requested.contains("\\") || requested.contains("..")) {
                sendJson(exchange, 400, "{\"error\":\"Invalid image path.\"}");
                return;
            }
            File image = new File(AppContext.baseDirectory(), "images" + File.separator + requested).getCanonicalFile();
            File directory = new File(AppContext.baseDirectory(), "images").getCanonicalFile();
            if (!image.toPath().startsWith(directory.toPath()) || !image.isFile()) {
                sendJson(exchange, 404, "{\"error\":\"Image not found.\"}");
                return;
            }
            String lower = image.getName().toLowerCase();
            String type = lower.endsWith(".png") ? "image/png" : lower.endsWith(".gif") ? "image/gif" : "image/jpeg";
            byte[] data = Files.readAllBytes(image.toPath());
            exchange.getResponseHeaders().set("Content-Type", type);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream out = exchange.getResponseBody()) { out.write(data); }
        }
    }

    private static final class StaticHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || "/".equals(path)) path = "/customer.html";
            if (path.contains("..") || path.contains("\\")) {
                sendJson(exchange, 400, "{\"error\":\"Invalid path.\"}");
                return;
            }
            File root = new File(AppContext.baseDirectory(), "www").getCanonicalFile();
            File file = new File(root, path.substring(1)).getCanonicalFile();
            if (!file.toPath().startsWith(root.toPath()) || !file.isFile()) {
                sendJson(exchange, 404, "{\"error\":\"Not found.\"}");
                return;
            }
            String lower = file.getName().toLowerCase();
            String type = lower.endsWith(".html") ? "text/html; charset=UTF-8"
                    : lower.endsWith(".css") ? "text/css; charset=UTF-8" : "application/octet-stream";
            byte[] data = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", type);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream out = exchange.getResponseBody()) { out.write(data); }
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private int number;
        @Override public synchronized Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "hotel-http-" + (++number));
            thread.setDaemon(true);
            return thread;
        }
    }
}
