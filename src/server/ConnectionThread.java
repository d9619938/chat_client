package server;
import service.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.StringJoiner;

public class ConnectionThread implements Runnable {
    Server server;
    ConnectionService connectionService;


    public ConnectionThread(Server server, ConnectionService connectionService) {
        this.server = server;
        this.connectionService = connectionService;
    }

    public void run() {
        while (true) {
            Message message;
            Sender sender;
            File file;
            try {
                message = connectionService.readMessage();
                System.out.println(message);

                if(message instanceof Sender) {
                    sender = (Sender) message;

                    if (!server.getFileMap().containsKey(sender.getFile())) {   // загрузка файла на сервер
                        file = new File(server.getDirectory() + server.getSeparator() + sender.getFileName());
                        Files.write(file.toPath(), sender.getFileByte());
                        server.getFileMap().put(file, sender.getDescription());
                        PrintWriter printWriter = new PrintWriter(new FileWriter(server.mapinfo, true));
                        synchronized (server.mapinfo) {
                            printWriter.println(file.getAbsolutePath() + ";" + sender.getDescription());
                        }
                        printWriter.close();
                        for (ConnectionService connection : server.getConnectionServicesList()) {  // рассылка сообщений о добавлении нового файла
                            try {
                                connection.writheMessage(new Message("На сервер загружен новый файл: "
                                        + sender.getFileName() + " = " + sender.getDescription()));
                            } catch (IOException e) {
                                System.out.println("Ошибка отправки сообщения");
                                System.out.println(e.getMessage());
                            }
                        }
                    } else {
                        connectionService.writheMessage(new Message("Файл с указанным именем уже существует," +
                                " проверьте список файлов на сервере"));
                    }




                } else if(message.getText().equals("/get_files")){    // сообщение клиенту о файлах хранящихся на сервере
                    StringJoiner joiner = new StringJoiner("\n");
                    for (Map.Entry<File, String> x : server.getFileMap().entrySet()) {
                        String line = "- " + x.getKey().getName() + " = " + x.getValue();
                        joiner.add(line);
                    }
                    String str = joiner.toString();

                    connectionService.writheMessage(new Message("список доступных файлов на сервере:\n" + str));

                }


                else if(message.getText().startsWith("/get") && message.getText().endsWith(".txt")){ // оправка файла клиенту
                    String[] strings = message.getText().split(" ");
                    sender = new Sender(strings[0] + " "
                            + server.getDirectory() + server.getSeparator() + strings[1]);
                    file = new File(server.getDirectory() + server.getSeparator() + strings[1]);
                    if(server.getFileMap().containsKey(file)) {
                        sender.setDescription(server.getFileMap().get(file));
                        sender.checkFile();
                        connectionService.writheMessage(sender);
                    } else {
                        connectionService.writheMessage(new Message("Ошибка в запросе, повторите"));
                    }

                }


            else {
                    for (ConnectionService connection : server.getConnectionServicesList()) {  // общая рассылка сообщений
                        try {
                            connection.writheMessage(message);
                        } catch (IOException e) {
                            System.out.println("Ошибка отправки сообщения");
                            System.out.println(e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                server.getConnectionServicesList().remove(connectionService);
                for (ConnectionService connection : server.getConnectionServicesList()) {
                    try {
                        connection.writheMessage(new Message("Один из пользователей покинул чат\n" +
                                "Количество активных соединений - " + server.getConnectionServicesList().size()));
                    } catch (IOException ex) {
                        System.out.println("Ошибка отправки сообщения");
                        System.out.println(ex.getMessage());
                    }
                }
                System.out.println("Соединение с клиентом разорвано");
                System.out.printf("Количество активных соединений - %d\n", server.getConnectionServicesList().size());
                connectionService.close();
                return;
            }
        }
    }

}
