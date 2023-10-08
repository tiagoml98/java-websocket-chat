import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This is a copy of Client.java, used for demonstration
 */
public class Client2 implements Runnable {

    private static String SERVER_IP = "127.0.0.1";

    private static int SERVER_PORT = 8005;

    private Socket socket;

    private BufferedReader reader;

    private BufferedReader systemReader;

    private PrintWriter writer;

    private boolean clientInactive;

    @Override
    public void run() {
        try {
            systemReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter Server IP: ");
            SERVER_IP = systemReader.readLine();
            System.out.println("Enter Server Port: ");
            SERVER_PORT = Integer.parseInt(systemReader.readLine());

            socket = new Socket(SERVER_IP, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            MessageReader messageReader = new MessageReader();
            Thread thread = new Thread(messageReader);
            thread.start();
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            end();
        }
    }

    public void end() {
        clientInactive = true;
        try {
            reader.close();
            writer.close();
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class MessageReader implements Runnable {

        @Override
        public void run() {
            try {
                while (!clientInactive) {
                    String message = systemReader.readLine();
                    if (message.equals("EXIT")) {
                        writer.println(message);
                        systemReader.close();
                        end();
                    } else {
                        writer.println(message);
                    }
                }
            } catch (IOException e) {
                end();
            }
        }
    }

    public static void main(String args[]) {
        Client2 client2 = new Client2();
        client2.run();
    }
}

