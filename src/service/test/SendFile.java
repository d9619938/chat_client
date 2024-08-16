package service.test;


import java.io.File;

public class
SendFile {
   File file;

    public SendFile(String string) {
        file = new File(string);
        if (file.exists()) {
            System.out.println((getFileSizeMegaBytes(file)) < 1);
            System.out.println((getFileSizeBytes(file)) == 11);
        } else System.out.println("Файла нет");
    }




    // возвращает размер файла в мегабайтах
    // длину файла делим на 1 мегабайт (1024 * 1024 байт) и узнаем количество мегабайт
    private static double getFileSizeMegaBytes(File file) {
        return (double) file.length()/(1024*1024);
    }

    // возвращает размер файла в килобайтах
    // длину файла делим на 1 килобайт (1024 байт) и узнаем количество килобайт
    private static String getFileSizeKiloBytes(File file) {
        return (double) file.length()/1024 + " kb";
    }

    // просто вызываем метод length() и получаем размер файла в байтах
    private long getFileSizeBytes(File file) {
        return (long)file.length();
    }

}
