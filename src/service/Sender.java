package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class Sender extends Message {
    private final int maxDescriptionLength = 20;
    private final int maxFileSizeMb = 1;
    private String path;
    private String fileName;
    private File file;
    private String description;
    private byte[] fileByte;

    public Sender(String text)/* throws IOException*/ {
        super(text);
        if (text == null) throw new IllegalArgumentException("Ошибка создания объекта Sender, текст не может быть null");
        parseMessage(getText());
    }
    private void parseMessage (String string) {
        String[] strings = string.split(" ");
        this.path = strings[1];
        if (!path.endsWith(".txt")) {
            System.out.println("Ошибка создания объекта Sender, текст не содержит ссылки на файл");
        } else {
            this.file = new File(path);
            fileName = file.getName();
        }
    }

    public boolean checkFile () throws IOException {
        Objects.requireNonNull(file, "file не может быть null");
        if (file.isFile() && file.length() < maxFileSizeMb * (1024 * 1024) && description.length() < maxDescriptionLength){
            System.out.println("Файл прошел проверку");
            fileByte = Files.readAllBytes(file.toPath());
            return true;
        } else {
            System.out.println("Файл не прошел проверку");
            System.out.println("по данному пути располагается файл? - " + file.isFile());
            System.out.println("размер файла превышает " + maxFileSizeMb + "Мб? - " + (file.length() > maxFileSizeMb * (1024 * 1024)));
            System.out.println("описание файла превышает допустимый размер? - " + (description.length() > maxDescriptionLength));
            System.out.println("Повторите попытку учитывая результаты проверки");
        }
        return false;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }

    public byte[] getFileByte() {
        return fileByte;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String string) {
        this.description = string;
    }
}



