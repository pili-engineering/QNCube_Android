package com.niucube.playersdk.player.been;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MediaParams {

    @PrimaryKey(autoGenerate = false)//主键是否自动增长，默认为false
    @NonNull
    private String path;
    private int width;
    private int height;
    private long video_length;

    public MediaParams() {
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getVideo_length() {
        return video_length;
    }

    public void setVideo_length(long video_length) {
        this.video_length = video_length;
    }


}
