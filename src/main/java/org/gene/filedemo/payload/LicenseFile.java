package org.gene.filedemo.payload;

public class LicenseFile {
    private String fileName;
    private long fileSize;
    private String fileType;

    public LicenseFile() {
    }

    public LicenseFile(String fileName, long fileSize, String fileType) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LicenseFile{");
        sb.append("fileName='").append(fileName).append('\'');
        sb.append(", fileSize=").append(fileSize);
        sb.append(", fileType='").append(fileType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
