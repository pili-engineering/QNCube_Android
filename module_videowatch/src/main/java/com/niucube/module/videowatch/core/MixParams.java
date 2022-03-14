package com.niucube.module.videowatch.core;

import java.util.ArrayList;
import java.util.List;


/*

 w: 1280, h: 720, fps: 25, gopSize: 75, bitrate: 1500000,
 

 */
public class MixParams {

    public int w = 1280;
    public int h = 720;
    public int fps = 25;
    public int gopSize = 75;
    public int bitrate = 1500000;
    public List<InputsDTO> inputs =  new ArrayList<InputsDTO>();
    public List<OutputsDTO> outputs =new ArrayList<OutputsDTO>();


    public static class InputsDTO {
        public String url;
        public int x;
        public int y;
        public int w;
        public int h;
        public String filter;
    }

    public static class OutputsDTO {
        public String url ="";
    }
}
