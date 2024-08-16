package client;

import service.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;

 /*
Соединение с сервером устанавливается, когда запускается клиентская программа.
Соединение с сервером разрывается, если пользователь решил завершить работу программы или, если сервер перестал отвечать.
Клиентская программа завершает работу по желанию пользователя (ввод exit в консоль) или, если сервер перестал отвечать.
Клиент в отдельном потоке получает данные от пользователя, создает экземпляр Message и отправляет сообщение на сервер.
Клиент в отдельном потоке получает сообщения от сервера и выводит их в консоль.
Пользователь может загрузить на сервер '.txt' файл. Передаются: описание файла (не более N символов) и содержимое файла (не более M Mb). Значения N и M задать самостоятельно.
Пользователь может запросить '.txt' файл, для получения файла пользователь должен ввести его название. Названия и описание доступных файлов присылает сервер.*/

public class Client implements Runnable {

    InetSocketAddress remoteAddress;
    private final String directory;
    private final String separator;

    public Client(InetSocketAddress remoteAddress) {
        this.remoteAddress = Objects.requireNonNull(remoteAddress, "remoteAddress не может быть null");
        directory = "/Users/dmitrijbogdanov/IdeaProjects/my-training/new-local-repository/src/com/local/project" +
                "/exam03/client/file_archive_client";
        separator = File.separator;
    }

    @Override
    public void run() {
        try{
            Socket socket = new Socket(); // сокет для соединения с сервером
            socket.connect(remoteAddress); // передаем в сокет ip и порт сервера, к которому хотим подключиться
            try {
                ConnectionService service = new ConnectionService(socket); // используем ConnectionService для получения
                System.out.println("Соединение с сервером установлено");
                Thread threadWrithe = new Thread(()-> {   // отдельный поток для отправки сообщений на сервер
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("""
                            Введите текст сообщения или команду:
                            - '/send и путь к файлу' для загрузки файла на сервер
                            - '/get_files' для получения информации о файлах хранящихся на сервере
                            - '/get и имя файла' для получения файла
                            - '/exit' для выхода из чата.
                            """);
                    String str;
                    Sender sender;
                    while (true) {
                        str = scanner.nextLine();
                        if ("/exit".equals(str)) {   //выход по команде
                            try {
                                socket.close();
                                System.out.println("Вы покинули чат");
                                System.exit(1);
                            } catch (IOException e) {
                                System.out.println("Ошибка во время закрытия сокета");
                                System.out.println(e.getMessage());
                            }


                        } else if (str.contains("/send")) {// отправка файла
                            try {
                                    sender = new Sender(str);
                                    if(sender.getFile() == null) {
                                        System.out.println("Повторите запрос");
                                    } else {
                                        System.out.println("Введите описание файла");
                                        sender.setDescription(scanner.nextLine());
                                        if (sender.checkFile()) service.writheMessage(sender);
                                    }
                            } catch (IOException e) {
                                System.out.println("Ошибка отправка файла");
                                throw new RuntimeException(e);
                            }


                        }  else {
                            try {
                                service.writheMessage(new Message(str));  // отправка обычного текстового сообщения
                            } catch (IOException e) {
                                System.out.println("Ошибка соединения, необходимо переподключиться");
                                throw new RuntimeException();
                            }
                        }
                    }
                });

                Thread threadRead = new Thread(()-> {
                    Message message;
                    Sender sender;
                    File file;
                    while (true) {
                            try {
                                message = service.readMessage();
                                if (message instanceof Sender) {   // получение и загрузка файла
                                    sender = ((Sender) message);
                                    file = new File(directory + separator + sender.getFileName());
                                    Files.write(file.toPath(), sender.getFileByte());
                                    System.out.println("Файл загружен в директорию клиента");
                                } else {
                                    System.out.println(message);     // получение обычного текстового сообщения
                                }
                            } catch (IOException e) {
                                System.out.println("Ошибка во время получения сообщения");
                                System.out.println("Сервер перестал отвечать, попробуйте переподключиться");
                                System.out.println(e.getMessage());
                                System.exit(2);
                            }
                    }
                });

                threadWrithe.start();
                threadRead.start();

            } catch (IOException e) {
                System.out.println("Сервер перестал отвечать");
                System.out.println(e.getMessage());
                System.exit(2);
            }

        } catch (IOException e) {
            System.out.println("Сервер недоступен, не удалось установить соединение с сервером");
            System.out.println(e.getMessage());
        }
    }
}
