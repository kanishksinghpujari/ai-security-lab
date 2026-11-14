package dev.kanishk.aisecuritylab.security;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClamAvScanner {
    private static final int CHUNK_SIZE = 2048;

    private final String host;
    private final int port;

    public ClamAvScanner(
            @Value("${lab.clamav.host:127.0.0.1}") String host,
            @Value("${lab.clamav.port:3310}") int port) {
        this.host = host;
        this.port = port;
    }

    public ScanResult scan(byte[] content) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2_000);
            socket.setSoTimeout(10_000);

            try (DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                 ByteArrayOutputStream response = new ByteArrayOutputStream()) {
                output.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
                for (int offset = 0; offset < content.length; offset += CHUNK_SIZE) {
                    int length = Math.min(CHUNK_SIZE, content.length - offset);
                    output.writeInt(length);
                    output.write(content, offset, length);
                }
                output.writeInt(0);
                output.flush();

                int next;
                while ((next = socket.getInputStream().read()) != -1 && next != 0) {
                    response.write(next);
                }
                if (next == -1) {
                    throw new IOException("ClamAV closed the connection without a response");
                }
                String reply = response.toString(StandardCharsets.UTF_8).trim();
                if (reply.endsWith("OK")) {
                    return new ScanResult(true, "CLEAN");
                }
                if (reply.contains("FOUND")) {
                    return new ScanResult(false, "INFECTED");
                }
                throw new IOException("Unexpected ClamAV response");
            }
        }
    }

    public record ScanResult(boolean clean, String status) {}
}
