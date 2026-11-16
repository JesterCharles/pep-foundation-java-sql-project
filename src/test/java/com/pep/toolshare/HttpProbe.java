package com.pep.toolshare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Starts a Javalin app on a free localhost port and sends real HTTP requests to it. */
public final class HttpProbe implements AutoCloseable {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final Javalin app;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public record Reply(int status, String contentType, String body) {
        public JsonNode json() {
            try {
                return JSON.readTree(body);
            } catch (IOException exception) {
                throw new AssertionError("Expected a JSON body but got: " + body, exception);
            }
        }

        public String describe() {
            return "HTTP " + status + " " + body;
        }
    }

    public HttpProbe(Javalin app) {
        this.app = app;
        app.start("127.0.0.1", 0);
    }

    public Reply get(String path) {
        return send(HttpRequest.newBuilder(uri(path)).GET());
    }

    public Reply post(String path, String jsonBody) {
        return post(path, jsonBody, null);
    }

    public Reply post(String path, String jsonBody, String adminToken) {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(jsonBody == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(jsonBody));
        if (adminToken != null) {
            request.header("X-Admin-Token", adminToken);
        }
        return send(request);
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + app.port() + path);
    }

    private Reply send(HttpRequest.Builder request) {
        try {
            HttpResponse<String> response = client.send(request.timeout(Duration.ofSeconds(10)).build(),
                    HttpResponse.BodyHandlers.ofString());
            return new Reply(response.statusCode(), response.headers().firstValue("Content-Type").orElse(""),
                    response.body());
        } catch (IOException exception) {
            throw new AssertionError("Request to " + request.build().uri() + " failed: " + exception, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted", exception);
        }
    }

    @Override
    public void close() {
        app.stop();
    }
}
