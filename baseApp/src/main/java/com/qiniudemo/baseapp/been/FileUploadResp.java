package com.qiniudemo.baseapp.been;

import java.io.Serializable;

public class FileUploadResp implements Serializable {

    /**
     * id : 63aa75042f2a486c98b95559
     * fileName : compress_1672115459854.jpg
     * fileUrl : https://demo-qnrtc-files.qnsdk.com/image-file-compress_1672115459854.jpg
     * status : 1
     */
    private String id;
    private String fileName;
    private String fileUrl;
    private int status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
