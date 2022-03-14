package com.qiniudemo.baseapp.been;

import java.io.Serializable;

public class QiniuApp implements Serializable {

    /**
     * id : aliquip aliqua ex
     * title : aliqua non eu commodo cillum
     * url : ipsum esse enim in Duis
     * desc : sit reprehenderit adipisicing
     * icon : aliqua eu sit occaecat
     */
    private String id;
    private String title;
    private String url;
    private String desc;
    private String icon;
    private String type="";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
