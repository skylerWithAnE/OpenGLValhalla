
package framework;

import static JGL.JGL.GL_CLAMP_TO_EDGE;
import static JGL.JGL.GL_FLOAT;
import static JGL.JGL.GL_LINEAR;
import static JGL.JGL.GL_LINEAR_MIPMAP_LINEAR;
import static JGL.JGL.GL_NEAREST;
import static JGL.JGL.GL_REPEAT;
import static JGL.JGL.GL_RGBA;
import static JGL.JGL.GL_RGBA32F;
import static JGL.JGL.GL_TEXTURE_2D;
import static JGL.JGL.GL_TEXTURE_CUBE_MAP;
import static JGL.JGL.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static JGL.JGL.GL_TEXTURE_MAG_FILTER;
import static JGL.JGL.GL_TEXTURE_MIN_FILTER;
import static JGL.JGL.GL_TEXTURE_WRAP_S;
import static JGL.JGL.GL_TEXTURE_WRAP_T;
import static JGL.JGL.GL_UNSIGNED_BYTE;
import static JGL.JGL.glGenerateMipmap;
import static JGL.JGL.glTexImage2D;
import static JGL.JGL.glTexParameteri;
import static JGL.JGL.glTexSubImage2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.imageio.ImageIO;


public class SolidCubeTexture extends CubeTexture{
    //+-X, +-Y, +-Z
    public SolidCubeTexture(int fmt, float r, float g, float b, float a){
        super(1);
        bind(0);
        //this.fmt=fmt;
        if(  fmt != GL_UNSIGNED_BYTE && fmt != GL_FLOAT )
            throw new RuntimeException("Bad format");
       
        glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
        
        byte[] data;
        if( fmt == GL_FLOAT ){
            ByteBuffer bb = ByteBuffer.allocate(16);
            FloatBuffer fb = bb.asFloatBuffer();
            float[] f = new float[]{r,g,b,a};
            fb.put(f);
            data = bb.array();
        }
        else{
            data = new byte[]{ (byte)(r*255), (byte)(g*255), (byte)(b*255), (byte)(a*255) };
        }
        
        for(int i=0;i<6;++i){
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,0,
                (fmt == GL_FLOAT) ? GL_RGBA32F : GL_RGBA,
                size,size,0, GL_RGBA, fmt, null);  
            glTexSubImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, 0,0, size,size,
                    GL_RGBA,fmt,data);
        }
    }
}