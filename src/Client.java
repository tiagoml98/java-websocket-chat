import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Tiago Lopes
 * Written for ECE6122-Q, Final Project Submission
 * <p>
 * Client class for the application, sends and receives messages from the server and coordinates user interaction
 */
public class Client implements Runnable {

    private static String SERVER_IP = "127.0.0.1";

    private static int SERVER_PORT = 8005;

    private Socket socket;

    private BufferedReader reader;

    private BufferedReader systemReader;

    private PrintWriter writer;

    private boolean clientInactive;

    /**
     * Creates a connection to a Server which then instantiates a Session
     * Performs read functionality via a separate reader thread which continuously listens to the Session
     */
    @Override
    public void run() {
        try {
            // Reads from the System.in input stream
            systemReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter Server IP: ");
            SERVER_IP = systemReader.readLine();
            System.out.println("Enter Server Port: ");
            SERVER_PORT = Integer.parseInt(systemReader.readLine());

            socket = new Socket(SERVER_IP, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            // Reads from the socket input stream
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            MessageReader messageReader = new MessageReader();
            Thread thread = new Thread(messageReader);
            thread.start();
            String message;
            // Outputs the message to the Client itself
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            end();
        }
    }

    /**
     * Terminates the clients connection on request or upon an IOException
     * Closes IO streams
     */
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

    /**
     * The class is nested to allow access to the writer object and class-level attributes
     */
    class MessageReader implements Runnable {

        /**
         * Continuously listens for Server broadcasts
         */
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
        // Creates and runs a client instance
        Client client = new Client();
        client.run();
    }
}

