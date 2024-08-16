package service.test;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class FileSenderTest {

    Socket connectionSocket;
    FileInputStream fis;
    FileOutputStream fos;
    OutputStream os;
    BufferedInputStream bis;
    BufferedOutputStream bos;
    File file;
    final int maxFileSizeMb = 1;

    public FileSenderTest(Socket connectionSocket) throws IOException {
        this.connectionSocket = Objects.requireNonNull(connectionSocket, "connectionSocket не может быть null");
    }

    public void sendFile (String directory, String fileName) throws IOException {
        this.file = new File(directory + fileName);
//        if (!getFileSizeCheck(file)) throw new IllegalArgumentException("Размер файла превышает 1Мб");
//        byte[] byteArray = new byte[1024];
//        fis = new FileInputStream(file);
//        bis = new BufferedInputStream(fis);
//
//        bos = new BufferedOutputStream(connectionSocket.getOutputStream());
//        int inByte;
//        while ((inByte = bis.read(byteArray)) != -1) {
//            bos.write(byteArray, 0, inByte);
//        }
        byte [] mybytearray  = new byte [(int)file.length()];
        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        os = connectionSocket.getOutputStream();
        os.write(mybytearray,0,mybytearray.length);

        os.flush();
        bos.close();
        bis.close();
    }

    public File getFile (String directory, String fileName) throws IOException {
        if (directory != null && fileName != null) {
//            File f = new File(directory + fileName);
//            fos = new FileOutputStream(f.getName());
//            bos = new BufferedOutputStream(fos);
//
//            bis = new BufferedInputStream(connectionSocket.getInputStream());
//            byte[] byteArray = new byte[1024];
//            int readByte;
//            while ((readByte = bis.read(byteArray)) != -1){
//                bos.write(byteArray, 0, readByte);
//            }
            byte [] mybytearray  = new byte [maxFileSizeMb * 1024 * 1024];
            InputStream is = connectionSocket.getInputStream();
            fos = new FileOutputStream(directory + fileName);
            bos = new BufferedOutputStream(fos);
            int bytesRead = is.read(mybytearray,0,mybytearray.length);
            int current = bytesRead;

            do {
                bytesRead =
                        is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos.write(mybytearray, 0 , current);

            bos.flush();
            bos.close();
            bis.close();
            return new File(directory + fileName);
        }
        return null;
    }

    public boolean getFileSizeCheck(File file) {
        this.file = Objects.requireNonNull(file, "file не может быть null");
        return file.length()/(1024*1024) < maxFileSizeMb;
    }
}