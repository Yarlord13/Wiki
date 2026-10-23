package util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import domain.WikiResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WikiRequest {
    private static final Gson GSON = new Gson();

    private static final String API_URL = "https://ru.wikipedia.org/w/api.php";
    private static final String USER_AGENT = "WikiSearchApp/1.0 (https://example.org; contact@example.org)";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
                                                        .connectTimeout(CONNECT_TIMEOUT)
                                                        .followRedirects(HttpClient.Redirect.NORMAL)
                                                        .build();

    public static WikiResponse request(String req, int limit) {
        if (req == null || req.isBlank()) {
            throw new IllegalArgumentException("Запрос должен быть не пустым");
        }

        String encoded = URLEncoder.encode(req, StandardCharsets.UTF_8);

        String url = API_URL
                    + "?action=query"
                    + "&list=search"
                    + "&utf8="
                    + "&format=json"
                    + "&srlimit=" + limit
                    + "&srsearch=" + encoded;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = send(request);

        checkStatus(response);
        return parse(response.body());
    }

    public static WikiResponse request(String req) {
        return request(req, 10);
    }

    private static HttpResponse<String> send(HttpRequest request) {
        try {
            return CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        } catch (HttpConnectTimeoutException e) {
            throw new WikiRequestException(WikiRequestException.Kind.TIMEOUT,
                    "Не удалось подключиться к серверу: превышено время ожидания", e);

        } catch (HttpTimeoutException e) {
            throw new WikiRequestException(WikiRequestException.Kind.TIMEOUT,
                    "Сервер не ответил вовремя. Попробуйте позже.", e);

        } catch (UnknownHostException e) {
            throw new WikiRequestException(WikiRequestException.Kind.NO_INTERNET,
                    "Нет подключения к интернету", e);

        } catch (ConnectException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("network is unreachable") || msg.contains("no route to host")) {
                throw new WikiRequestException(WikiRequestException.Kind.NO_INTERNET,
                        "Нет подключения к интернету", e);
            }
            throw new WikiRequestException(WikiRequestException.Kind.SERVER_UNAVAILABLE,
                    "Сервер недоступен", e);

        } catch (IOException e) {
            throw new WikiRequestException(WikiRequestException.Kind.SERVER_UNAVAILABLE,
                    "Ошибка сети: " + e.getMessage(), e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WikiRequestException(WikiRequestException.Kind.INTERRUPTED,
                    "Запрос был прерван", e);
        }
    }

    private static void checkStatus(HttpResponse<String> response) {
        int code = response.statusCode();
        if (code == 200) return;

        if (code >= 500) {
            throw new WikiRequestException(WikiRequestException.Kind.SERVER_UNAVAILABLE,
                    "Сервер временно недоступен (HTTP " + code + "). Попробуйте позже.", null);
        }
        if (code == 403) {
            throw new WikiRequestException(WikiRequestException.Kind.BAD_RESPONSE,
                    "Сервер отклонил запрос (HTTP 403). Проверьте User-Agent.", null);
        }
        throw new WikiRequestException(WikiRequestException.Kind.BAD_RESPONSE,
                "Сервер вернул ошибку (HTTP " + code + ")", null);
    }

    private static WikiResponse parse(String body) {
        try {
            WikiResponse parsed = GSON.fromJson(body, WikiResponse.class);
            if (parsed == null || parsed.getQuery() == null) {
                throw new WikiRequestException(WikiRequestException.Kind.BAD_RESPONSE,
                        "Сервер вернул неожиданный ответ", null);
            }
            return parsed;

        } catch (JsonSyntaxException e) {
            throw new WikiRequestException(WikiRequestException.Kind.BAD_RESPONSE,
                    "Сервер вернул некорректный JSON", e);
        }
    }
}
