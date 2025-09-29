package org.example;

// import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * FastCGI сервер для проверки попадания точки в область
 */
public class FastCGIServer {
    private static final int PORT = 8080;
    // private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Queue<PointResult> results = new ConcurrentLinkedQueue<>();
    
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Обработчик для статических файлов
            server.createContext("/", new StaticFileHandler());
            
            // Обработчик для CGI запросов
            server.createContext("/cgi-bin/point-checker", new PointCheckerHandler());
            
            server.setExecutor(null);
            server.start();
            
            System.out.println("FastCGI сервер запущен на порту " + PORT);
            System.out.println("Откройте http://localhost:" + PORT + " в браузере");
            
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Обработчик статических файлов
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/") || path.equals("/index.html")) {
                // Отдаем главную страницу
                serveFile(exchange, "index.html", "text/html");
            } else if (path.equals("/styles.css")) {
                // Отдаем CSS файл
                serveFile(exchange, "styles.css", "text/css");
            } else {
                // 404 для остальных запросов
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private void serveFile(HttpExchange exchange, String filename, String contentType) throws IOException {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is == null) {
                    String response = "404 Not Found";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    return;
                }
                
                byte[] content = is.readAllBytes();
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            }
        }
    }
    
    /**
     * Обработчик для проверки попадания точки в область
     */
    static class PointCheckerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startTime = System.currentTimeMillis();
            
            try {
                // Получаем параметры из URL
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQuery(query);
                
                // Валидация параметров
                if (!validateParameters(params)) {
                    sendErrorResponse(exchange, "Неверные параметры");
                    return;
                }
                
                double x = Double.parseDouble(params.get("x"));
                double y = Double.parseDouble(params.get("y"));
                double r = Double.parseDouble(params.get("r"));
                
                // Проверяем попадание точки в область
                boolean hit = PointChecker.isPointInArea(x, y, r);
                
                long executionTime = System.currentTimeMillis() - startTime;
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                // Создаем результат
                PointResult result = new PointResult(x, y, r, hit, timestamp, executionTime);
                results.add(result);
                
                // Отправляем JSON ответ (простая сериализация)
                String jsonResponse = createJsonResponse(result);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                exchange.sendResponseHeaders(200, jsonResponse.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
                }
                
            } catch (Exception e) {
                System.err.println("Ошибка обработки запроса: " + e.getMessage());
                e.printStackTrace();
                sendErrorResponse(exchange, "Внутренняя ошибка сервера");
            }
        }
        
        private Map<String, String> parseQuery(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        try {
                            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            params.put(key, value);
                        } catch (Exception e) {
                            // Игнорируем некорректные параметры
                        }
                    }
                }
            }
            return params;
        }
        
        private boolean validateParameters(Map<String, String> params) {
            try {
                if (!params.containsKey("x") || !params.containsKey("y") || !params.containsKey("r")) {
                    return false;
                }
                
                double x = Double.parseDouble(params.get("x"));
                double y = Double.parseDouble(params.get("y"));
                double r = Double.parseDouble(params.get("r"));
                
                // Проверяем диапазоны значений
                return x >= -4 && x <= 4 && 
                       y >= -5 && y <= 5 && 
                       r >= 1 && r <= 3;
                       
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        private void sendErrorResponse(HttpExchange exchange, String message) throws IOException {
            String response = "{\"error\": \"" + message + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    /**
     * Простая сериализация в JSON
     */
    private static String createJsonResponse(PointResult result) {
        return String.format(
            "{\"x\":%.1f,\"y\":%.1f,\"r\":%.1f,\"hit\":%s,\"timestamp\":\"%s\",\"executionTime\":%d}",
            result.x, result.y, result.r, result.hit, result.timestamp, result.executionTime
        );
    }
    
    /**
     * Класс для хранения результата проверки точки
     */
    public static class PointResult {
        public double x;
        public double y;
        public double r;
        public boolean hit;
        public String timestamp;
        public long executionTime;
        
        public PointResult(double x, double y, double r, boolean hit, String timestamp, long executionTime) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.hit = hit;
            this.timestamp = timestamp;
            this.executionTime = executionTime;
        }
    }
}
