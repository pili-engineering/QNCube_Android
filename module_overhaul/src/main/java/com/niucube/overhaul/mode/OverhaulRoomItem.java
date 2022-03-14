package com.niucube.overhaul.mode;

import java.util.List;

public class OverhaulRoomItem {
    public String roomId;
    public String title;
    public String image;
    public int status;
    public List<Options> options;

    public static class Options{
       public String role;
       public String title;
    }
}
