package service.test;

import java.io.Serializable;

public class Files implements Serializable {
    private String fileDescription;
    private String fileText;
    private double fileSizeMb;
    private String fileDirectory;

    public Files(String fileDescription, String fileText) {
        this.fileDescription = fileDescription;
    }

    public void setFileText(String fileText) {
        this.fileText = fileText;
        for (int i = 0; i < 100; i++) {
            this.fileText += fileText;
        }

    }
}
