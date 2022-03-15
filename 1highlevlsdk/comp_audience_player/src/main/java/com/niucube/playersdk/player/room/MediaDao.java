package com.niucube.playersdk.player.room;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.niucube.playersdk.player.been.MediaParams;


@Dao
public interface MediaDao {

    @Query("SELECT * FROM mediaparams WHERE path=:path")
    MediaParams getMediaParams(String path);


    @Insert
    void insert(MediaParams mediaParams);
}
