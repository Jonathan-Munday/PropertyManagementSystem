package za.ac.mycput.infrastructure.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Local JWKS endpoint for decoder tests. Only the public key is published.
 */
public final class JwksServer implements AutoCloseable {

    private final HttpServer server;
    private volatile String document = "{\"keys\":[]}";

    private JwksServer(HttpServer server) {
        this.server = server;
    }

    public static JwksServer start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        JwksServer jwks = new JwksServer(server);
        server.createContext("/.well-known/jwks.json", exchange -> {
            byte[] body = jwks.document.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Cache-Control", "no-store, max-age=0");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        return jwks;
    }

    public void publish(JWK key) {
        document = new JWKSet(key.toPublicJWK()).toString();
    }

    public String uri() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/.well-known/jwks.json";
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
