package com.niucube.playersdk.player.room;



import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.niucube.playersdk.player.been.MediaParams;


@Database(entities = {MediaParams.class},version = 1,exportSchema = false)
public abstract class MediaDataBase  extends RoomDatabase {

    private static final String DB_NAME = "MediaDataBase.db";
    private static volatile MediaDataBase instance;

    public static synchronized MediaDataBase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static MediaDataBase create(final Context context) {
        return Room.databaseBuilder(
                context,
                MediaDataBase  .class,
                DB_NAME)
                .allowMainThreadQueries()
                .build();
    }

    public abstract MediaDao getMediaDao();

}
