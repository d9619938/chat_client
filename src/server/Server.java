package server;

import service.ConnectionService;
import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
Сервер не разрывает соединение с клиентом по своей инициативе.
Соединение с клиентом разрывается, если клиент перестал отвечать.
Сервер принимает сообщения от клиентов и рассылает их по всем активным соединениям.
Каждое соединение клиент-сервер должно обрабатываться в отдельном потоке.
Активные соединения хранить в потокобезопасной коллекции или мапе.
Если клиент присылает файл и он удовлетворяет всем требованиям, сервер сохраняет его и рассылает остальным клиентам информацию о том, что загружен новый файл. Сервер рассылает название файла + описание содержимого.
Если клиент запрашивает файл, сервер сначала присылает список имен файлов, потом содержимое выбранного из списка файла.*/

public class Server implements Runnable{

    private final int port;
    private ExecutorService threadPool;
    private CopyOnWriteArrayList<ConnectionService> connectionServicesList;
    private ConcurrentHashMap<File, String> fileMap;
    private final String directory;
    private final String separator;
    File mapinfo;

    public Server(int port) {
        this.port = port;
        threadPool = Executors.newFixedThreadPool(10);
        directory = "/Users/dmitrijbogdanov/IdeaProjects/my-training/new-local-repository/src/com/local/project" +
                "/exam03/server/file-archive-server";
        separator = File.separator;
    }

    public CopyOnWriteArrayList<ConnectionService> getConnectionServicesList() {
        return connectionServicesList;
    }

    public ConcurrentHashMap<File, String> getFileMap() {
        return fileMap;
    }

    public String getDirectory() {
        return directory;
    }

    public String getSeparator() {
        return separator;
    }

    @Override
    public void run() {
     try(ServerSocket serverSocket = new ServerSocket(port)) {
         connectionServicesList = new CopyOnWriteArrayList<>();
         fileMap = new ConcurrentHashMap<>();
         setUpFileMap();  // загрузка данных о сохраненных файлах на сервере
         System.out.println("Сервер запущен");
         System.out.printf("Количество активных соединений - %d\n", getConnectionServicesList().size());
         if (!fileMap.isEmpty()) {
             System.out.println("Список файлов хранящихся на сервере:");
             fileMap.entrySet().stream()
                     .forEach(x -> System.out.println("- " + x.getKey().getName() + " = " + x.getValue()));
         }
         while (true) {
             try {
                 ConnectionService connectionService = new ConnectionService(serverSocket.accept());
                 connectionServicesList.add(connectionService);
                 System.out.println("Новое подключение");
                 System.out.printf("Количество активных соединений - %d\n", getConnectionServicesList().size());
                 threadPool.execute(new ConnectionThread(this, connectionService));
             } catch (IOException e){
                 System.out.println("Ошибка подключения");
                 System.out.println(e.getMessage());
             }
         }

     } catch (IOException e) {
         System.out.println("Ошибка создания serverSocket, возможно порт уже занят");
         System.out.println(e.getMessage());
     }
    }
    private void setUpFileMap() {
        mapinfo = new File("/Users/dmitrijbogdanov/IdeaProjects/my-training/new-local-repository/src" +
                "/com/local/project/exam03/server/mapinfo.txt");
        if(!isFileEmpty(mapinfo)){
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(mapinfo))){
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] strings = line.split(";");
                    fileMap.put(new File(strings[0]), strings[1]);
                }
            } catch (IOException e){
                System.out.println("Ошибка чтения из файла mapinfo.txt");
                System.out.println(e.getMessage());
            }
        }

    }
    private boolean isFileEmpty(File file) {
        return file.length() == 0;
    }
}
