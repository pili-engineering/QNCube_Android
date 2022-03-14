package com.niucube.lrcview;

import androidx.annotation.Nullable;

import com.niucube.lrcview.bean.LrcData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 加载歌词
 *
 *
 * @date 2021/7/6
 */
public class LrcLoadUtils {


    @Nullable
    public static LrcData parse( File lrcFile) {

            InputStream instream = null;
            InputStreamReader inputreader = null;
            BufferedReader buffreader = null;
            try {
                instream = new FileInputStream(lrcFile);
                inputreader = new InputStreamReader(instream);
                buffreader = new BufferedReader(inputreader);
                String line = buffreader.readLine();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (instream != null) {
                        instream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (inputreader != null) {
                        inputreader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (buffreader != null) {
                        buffreader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        return LrcLoadDefaultUtils.parseLrc(lrcFile);
    }
}
