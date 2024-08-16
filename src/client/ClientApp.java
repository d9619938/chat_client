package client;

import java.net.InetSocketAddress;

public class ClientApp {
    public static void main (String[] args) {
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 4001);
        Client client = new Client(remoteAddress);
        client.run();
    }

}
