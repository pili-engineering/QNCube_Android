package com.niucube.overhaul.fbo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;


import com.niucube.overhaul.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class TextureRenderer {

    private static final String TAG = "TextureRenderer";

    private final FloatBuffer vertexBuffer, vertexBuffer2, mTexVertexBuffer;

    private final ShortBuffer mVertexIndexBuffer;

    private int mProgram;

    private int textureId, textureId2;

    private int uTextureUnitLocation = 0;

    public RendererCallback rendererCallback = null;
    /**
     * 顶点坐标
     * (x,y,z)
     */
    private float[] POSITION_VERTEX = new float[]{
            0f, 0f, 0f,     //顶点坐标V0

            1f, -1f, 0f,     //顶点坐标V4
            -1f, -1f, 0f,   //顶点坐标V3
            -1f, 1f, 0f,    //顶点坐标V2
            1f, 1f, 0f,     //顶点坐标V1
    };

    /**
     * 顶点坐标
     * (x,y,z)
     */
    float ratioX = 0.9f;
    float ratioY = 0.5f;

    private float[] POSITION_VERTEX2 = new float[]{
            0f, 0f, 0f,     //顶点坐标V0


            1f * ratioX, -1f * ratioY, 0f,     //顶点坐标V4
            -1f * ratioX, -1f * ratioY, 0f,   //顶点坐标V3
            -1f * ratioX, 1f * ratioY, 0f,    //顶点坐标V2
            1f * ratioX, 1f * ratioY, 0f,     //顶点坐标V1
    };

    /**
     * 纹理坐标
     * (s,t)
     */
    private static final float[] TEX_VERTEX = {
            0.5f, 0.5f, //纹理坐标V0
            1f, 0f,     //纹理坐标V1
            0f, 0f,     //纹理坐标V2
            0f, 1.0f,   //纹理坐标V3
            1f, 1.0f,    //纹理坐标V4

    };

    /**
     * 索引
     */
    private static final short[] VERTEX_INDEX = {
            0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
            0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
            0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
            0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
    };


    private int uMatrixLocation;

    private float[] mMatrix = new float[16];

    public TextureRenderer() {
        //分配内存空间,每个浮点型占4字节空间
        vertexBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //传入指定的坐标数据
        vertexBuffer.put(POSITION_VERTEX);
        vertexBuffer.position(0);

        vertexBuffer2 = ByteBuffer.allocateDirect(POSITION_VERTEX2.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //传入指定的坐标数据
        vertexBuffer2.put(POSITION_VERTEX2);
        vertexBuffer2.position(0);


        mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX);
        mTexVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);
    }


    int w = 0;
    int h = 0;


    public void onSurfaceCreated() {


        //设置背景颜色
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        //编译
        final int vertexShaderId = ShaderUtils.compileVertexShader(ResReadUtils.readResource(R.raw.vertex_texture_shader));
        final int fragmentShaderId = ShaderUtils.compileFragmentShader(ResReadUtils.readResource(R.raw.fragment_texture_shader));
        //链接程序片段
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);

        //   uMatrixLocation = GLES30.glGetUniformLocation(mProgram, "u_Matrix");

        uTextureUnitLocation = GLES30.glGetUniformLocation(mProgram, "uTextureUnit");
        //加载纹理
        // textureId = TextureUtils.loadTexture(AppCore.getInstance().getContext(), R.drawable.as);
        //textureId2 = TextureUtils.loadTexture(AppCore.getInstance().getContext(), R.drawable.squirtle);
    }


    public void onSurfaceChanged(int width, int height) {
        Log.d("onSurfaceChanged", "onSurfaceChanged " + width + "  " + height);
        OpenGLTools.INSTANCE.createFBOTexture(width, height);
        w = width;
        h = height;
        OpenGLTools.INSTANCE.createFrameBuffer();
        GLES30.glViewport(0, 0, width, height);

//        final float aspectRatio = width > height ?
//                (float) width / (float) height :
//                (float) height / (float) width;
//        if (width > height) {
//            //横屏
//            Matrix.orthoM(mMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
//        } else {
//            //竖屏
//            Matrix.orthoM(mMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
//        }

    }
   // 绘制电路板识别成功标志
    public int onDrawFrame(int textureIdb, int textureIdf,int rotation) {
        textureId = textureIdb;
        textureId2 = textureIdf;
        Log.d("onDrawFrame", "onDrawFrame");
        OpenGLTools.INSTANCE.bindFBO();
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        //使用程序片段
        GLES30.glUseProgram(mProgram);

        //    GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);

        drawPic1();
        drawPic2(rotation);

        Log.d("drawPic1", "drawPic1");
        onReadPixel(0, 0, w, h);
        Log.d("onSurfaceChanged", "onReadPixel " + w + "  " + h);
        OpenGLTools.INSTANCE.unbindFBO();
        return OpenGLTools.INSTANCE.getTextures()[0];
        //OpenGLTools.INSTANCE.deleteFBO();
    }

    private void drawPic1() {

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glUniform1i(uTextureUnitLocation, 0);
        // 绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length, GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);

        //
    }

    private void drawPic2(int rotation) {

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer2);

        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId2);
        GLES30.glUniform1i(uTextureUnitLocation, 0);
        // 绘制
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA);
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES20.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.length, GLES30.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
    }


    private void onReadPixel(int x, int y, int width, int height) {
        if (rendererCallback == null) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
        GLES20.glReadPixels(x,
                y,
                width,
                height,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, buffer);
        rendererCallback.onRendererDone(buffer, width, height);
    }
}
