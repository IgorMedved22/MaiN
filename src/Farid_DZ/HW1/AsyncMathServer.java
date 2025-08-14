package Farid_DZ.HW1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

public class AsyncMathServer {

    public static void main(String[] args) throws Exception {
        new AsyncMathServer().start();
    }

    public void start() throws IOException {
        AsynchronousServerSocketChannel server =
                AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", 8888));

        System.out.println("[SERVER] Сервер запущен на порту 8888...");

        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void att) {
                server.accept(null, this); // принимаем следующее подключение
                handleClient(client);
            }

            @Override
            public void failed(Throwable exc, Void att) {
                exc.printStackTrace();
            }
        });

        // Чтобы программа не завершилась
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }

    private void handleClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer buf) {
                if (bytesRead == -1) {
                    try { client.close(); } catch (IOException ignored) {}
                    System.out.println("[SERVER] Клиент отключился.");
                    return;
                }

                buf.flip();
                String message = StandardCharsets.UTF_8.decode(buf).toString().trim();
                System.out.println("[SERVER] Получено: " + message);

                String response = processRequest(message);

                ByteBuffer outBuffer = ByteBuffer.wrap((response + "\n").getBytes(StandardCharsets.UTF_8));
                client.write(outBuffer, outBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        buf.clear();
                        client.read(buf, buf, this); // снова читаем от клиента
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        exc.printStackTrace();
                        try { client.close(); } catch (IOException ignored) {}
                    }
                });
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                exc.printStackTrace();
                try { client.close(); } catch (IOException ignored) {}
            }
        });
    }

    private String processRequest(String message) {
        try {
            String[] parts = message.split("\\s+");
            if (parts.length != 3) return "Ошибка: формат <операция> <число1> <число2>";

            String op = parts[0].toUpperCase();
            double a = Double.parseDouble(parts[1]);
            double b = Double.parseDouble(parts[2]);

            return switch (op) {
                case "ADD" -> String.valueOf(a + b);
                case "SUB" -> String.valueOf(a - b);
                case "MUL" -> String.valueOf(a * b);
                case "DIV" -> (b == 0) ? "Ошибка: деление на ноль" : String.valueOf(a / b);
                default -> "Неизвестная операция";
            };
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}
