package server;

public class ServerApp {
    public static void main(String[] args) {
        Server server = new Server(4001);
        server.run();
    }
}
