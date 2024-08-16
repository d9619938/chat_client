package service;

import java.io.*;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.util.Objects;

public class ConnectionService implements AutoCloseable {
    Socket connectionSocket;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;

    public Socket getConnectionSocket() {
        return connectionSocket;
    }

    public ConnectionService(Socket connectionSocket) throws IOException {
        this.connectionSocket = Objects.requireNonNull(connectionSocket, "connectionSocket не может быть null");
        this.outputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
        this.inputStream = new ObjectInputStream(connectionSocket.getInputStream());
    }

    @Override
    public void close() {
        try {
            inputStream.close();
            outputStream.close();
            connectionSocket.close();
        } catch (IOException e) {
            System.out.println("Ошибка во время закрытия ресурсов");
            System.out.println(e.getMessage());
        }
    }
    public void writheMessage (Message message) throws IOException {
            message.setSentAt(ZonedDateTime.now());
            outputStream.writeObject(message);
            outputStream.flush();
    }
    public Message readMessage () throws IOException {
        try {
            return (Message) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("Ошибка приведения входящего сообщения к типу Message");
            throw new RuntimeException(e);
        }
    }
}
