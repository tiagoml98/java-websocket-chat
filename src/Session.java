import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Tiago Lopes
 * Written for ECE6122-Q, Final Project Submission
 * <p>
 * An individual Session thread
 * Performs read/write functionality between its Client and the Server
 */
class Session implements Runnable {
    private Socket socket;
    private Server server;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public Session(Socket socket, Server server) {
        this.socket = socket;
        this.server = server; // The server allows Session access to sessionList for listing usernames
    }

    /**
     * Performs read/write functionality with a Client and interacts with the Server on its behalf
     */
    @Override
    public void run() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer.println("Enter username: ");
            username = reader.readLine();

            // The message is sent to all clients and printed to the Server
            System.out.println(username + " is connected.");
            server.broadcast(username + " has entered the chat.");

            writer.println("Available Commands: LIST USERS, EXIT");

            // A message is reed and parsed for chat commands
            String message;
            while ((message = reader.readLine()) != null) {
                // The sessionList is obtained from this.server and parsed for usernames
                if (message.startsWith("LIST USERS")) {
                    for (Session session : server.getSessionList()) {
                        StringBuilder userList = new StringBuilder();
                        if (!session.username.equals(username)) {
                            userList.append(session.username);
                            userList.append("\n");
                        }
                        writer.println(userList.toString());
                    }
                }
                // The server is informed of the user's exit and the thread is closed
                else if (message.startsWith("EXIT")) {
                    System.out.println(message);
                    server.broadcast(username + " has left the chat.");
                    end();
                }
                // An ordinary message is sent to all clients
                else {
                    server.broadcast(username + " : " + message);
                }
            }
        } catch (IOException e) {
            end();
        }
    }

    /**
     * A message is sent to a Client by the Session
     */
    public void writeMessage(String message) {
        writer.println(message);
    }

    /**
     * Closes the input/output streams and the socket
     */
    public void end() {
        try {
            writer.close();
            reader.close();
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore
        }

    }
}