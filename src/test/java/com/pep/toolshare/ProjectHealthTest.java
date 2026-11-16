package com.pep.toolshare;

import com.pep.toolshare.domain.Dates;
import com.pep.toolshare.domain.MemberTier;
import com.pep.toolshare.reports.WaitListBoard;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Starter health: the supplied files are present and the supplied helpers work before any TODO is done. */
class ProjectHealthTest {
    @Test
    void suppliedDataFilesArePresent() throws IOException {
        String seed = Files.readString(Path.of("src/main/resources/db/seed.sql"));
        assertTrue(seed.contains("INSERT INTO loans"), "seed.sql is supplied with the sample loans");
        assertTrue(seed.contains("example.test"), "the sample data uses reserved example.test addresses only");
        List<String> log = Files.readAllLines(Path.of("fixtures/loan-log.txt"));
        assertEquals(28, log.size(), "fixtures/loan-log.txt is the supplied synthetic log");
        assertTrue(Files.isRegularFile(Path.of("review/assistant-proposal.md")));
        assertTrue(Files.isRegularFile(Path.of("WORKLOG.md")));
        assertTrue(Files.isRegularFile(Path.of(".env.example")));
    }

    @Test
    void suppliedHelpersWork() {
        assertEquals(6, Dates.daysBetween(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 15)));
        assertEquals(LocalDate.of(2026, 10, 15), Dates.parseOrNull("2026-10-15"));
        assertNull(Dates.parseOrNull("15/10/2026"));
        WaitListBoard board = new WaitListBoard();
        assertSame(board.forTool("LT-1003-4"), board.forTool("LT-1003-4"));
        assertEquals(3, MemberTier.values().length);
    }

    @Test
    void appPrintsHelpWithoutAnyLearnerCode() {
        java.io.PrintStream original = System.out;
        java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(captured, true, java.nio.charset.StandardCharsets.UTF_8));
        try {
            App.main(new String[] {"help"});
        } finally {
            System.setOut(original);
        }
        String help = captured.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(help.contains("reset") && help.contains("report") && help.contains("serve"), help);
    }

    @Test
    void theServerListensOnThisComputerOnly() throws Exception {
        Javalin app = Javalin.create(config -> config.routes.get("/ping", ctx -> ctx.result("pong")));
        App.listen(app, 0);
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
            HttpResponse<String> reply = client.send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:" + app.port() + "/ping")).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals("pong", reply.body());
            for (InetAddress address : nonLoopbackAddresses()) {
                try (Socket socket = new Socket()) {
                    assertThrows(IOException.class, () -> socket.connect(new InetSocketAddress(address, app.port()), 2000),
                            "the API must not accept connections on " + address.getHostAddress());
                }
            }
        } finally {
            app.stop();
        }
    }

    private static List<InetAddress> nonLoopbackAddresses() throws SocketException {
        List<InetAddress> addresses = new ArrayList<>();
        for (NetworkInterface network : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (network.isUp() && !network.isLoopback()) {
                for (InetAddress address : Collections.list(network.getInetAddresses())) {
                    if (address instanceof Inet4Address) {
                        addresses.add(address);
                    }
                }
            }
        }
        return addresses;
    }
}
