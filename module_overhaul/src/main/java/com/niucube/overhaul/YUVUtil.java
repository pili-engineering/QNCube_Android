package com.niucube.overhaul;

public class YUVUtil {
    static int depth = 4;

    public static void NV21ToRGBA(byte[] input, int width, int height, byte[] output, boolean isRGB)
    {
        int nvOff = width * height ;
        int  i, j, yIndex = 0;
        int y, u, v;
        int r, g, b, nvIndex = 0;
        for(i = 0; i < height; i++){
            for(j = 0; j < width; j ++,++yIndex){
                nvIndex = (i / 2)  * width + j - j % 2;
                y = input[yIndex] & 0xff;
                u = input[nvOff + nvIndex ] & 0xff;
                v = input[nvOff + nvIndex + 1] & 0xff;

                // yuv to rgb
                r = y + ((351 * (v-128))>>8);  //r
                g = y - ((179 * (v-128) + 86 * (u-128))>>8); //g
                b = y + ((443 * (u-128))>>8); //b

                r = ((r>255) ?255 :(r<0)?0:r);
                g = ((g>255) ?255 :(g<0)?0:g);
                b = ((b>255) ?255 :(b<0)?0:b);
                if(isRGB){
                    output[yIndex*depth + 0] = (byte) b;
                    output[yIndex*depth + 1] = (byte) g;
                    output[yIndex*depth + 2] = (byte) r;
                }else{
                    output[yIndex*depth + 0] = (byte) r;
                    output[yIndex*depth + 1] = (byte) g;
                    output[yIndex*depth + 2] = (byte) b;
                }
            }
        }
    }


}
