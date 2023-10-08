import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tiago Lopes
 * Written for ECE6122-Q, Final Project Submission
 * <p>
 * Server class generates various threads called @Session, each receives a connection from a @Client
 * The Server maintains a sessionList from which the Sessions can access other Sessions for information or broadcasting
 */
public class Server implements Runnable {

    private static final int SERVER_PORT = 8005;

    private final ArrayList<Session> sessionList;

    private ServerSocket server;

    private boolean serverActive;

    private ExecutorService threadPool;

    /**
     * Initializes the Server instance variables from the main method
     */
    public Server() {
        sessionList = new ArrayList<>();
        serverActive = true;
    }

    /**
     * Accepts TCP socket connections, maintains each connection in a thread within threadPool
     */
    @Override
    public void run() {
        try {
            server = new ServerSocket(SERVER_PORT);
            threadPool = Executors.newCachedThreadPool();
            while (serverActive) {
                Socket socket = server.accept();
                Session session = new Session(socket, this);
                sessionList.add(session);
                threadPool.execute(session);
            }
        } catch (Exception e) {
            end();
        }
    }

    /**
     * Sends a message to all threads. Utilized for global messages such as clients entering or exiting the chat
     *
     * @param message
     */
    public void broadcast(String message) {
        for (Session session : sessionList) {
            if (session != null) {
                session.writeMessage(message);
            }
        }
    }

    /**
     * Ends each individual session alongside the server session, triggered by an Exception within run()
     * Otherwise the Server will remain running and willing to accept new Client connections
     */
    public void end() {
        try {
            serverActive = false;
            threadPool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (Session session : sessionList) {
                session.end();
            }
        } catch (IOException e) {
            // TODO: ignore
        }
    }

    public ArrayList<Session> getSessionList() {
        return sessionList;
    }

    public static void main(String[] args) {
        // Initialize and run a Server instance
        Server server = new Server();
        server.run();
    }
}

