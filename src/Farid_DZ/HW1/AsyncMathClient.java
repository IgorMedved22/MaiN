package Farid_DZ.HW1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;

public class AsyncMathClient {

    public static void main(String[] args) throws Exception {
        new AsyncMathClient().start();
    }

    public void start() throws Exception {
        try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
            client.connect(new InetSocketAddress("127.0.0.1", 8888)).get();
            System.out.println("[CLIENT] Подключено к серверу.");

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String line;

            while (true) {
                System.out.print("Введите команду (<операция> <число1> <число2> или exit): ");
                line = console.readLine();
                if (line == null || line.equalsIgnoreCase("exit")) break;

                ByteBuffer buffer = ByteBuffer.wrap((line + "\n").getBytes(StandardCharsets.UTF_8));
                client.write(buffer).get();

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                client.read(readBuffer).get();
                readBuffer.flip();
                String response = StandardCharsets.UTF_8.decode(readBuffer).toString().trim();
                System.out.println("[CLIENT] Результат: " + response);
            }
        }
    }
}
