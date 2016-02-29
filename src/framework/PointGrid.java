/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;
import static JGL.JGL.*; /*GL_ARRAY_BUFFER;
import static JGL.JGL.GL_STATIC_DRAW;
import static JGL.JGL.glBindBuffer;
import static JGL.JGL.glGenBuffers;
import static JGL.JGL.glBufferData;
import static JGL.JGL.glDrawArrays;*/
import java.util.ArrayList;

/**
 *
 * @author Jim Hudson (ported from JS by Skyler Evans)
 */
public class PointGrid {
    
    private int nx;
    private int ny;
    private int np;
    int vbuff;
    byte[] vdata;
    
    
    public PointGrid(int nx, int ny)
    {
        this.nx = nx;
        this.ny = ny;
        this.vbuff  = 0;
    }
    
    public void setup()
    {
        ArrayList<Float> pts = new ArrayList<>();
        this.np = this.nx * this.ny;
        for(int i = 0; i < this.nx; ++i)
        {
            //float x = -1.0f + 2.0f * i / (this.nx-1f);
            float s  = i/(this.nx-1);
            for(int j = 0; j < this.ny; ++j)
            {
                //float y = -1.0f + 2.0f * j / (this.ny-1);
                float t = j/(this.ny-1);
                pts.add(s);
                pts.add(t);
            }
        }
        int[] tmp = new int[1];
        glGenBuffers(1,tmp);
        this.vbuff = tmp[0];
        this.vdata = new byte[pts.size()]; //need to figure out how to assign points into vdata.
        /*
        ByteBuffer bb = ByteBuffer.allocate(pts.size() * 3 * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        
        //convert pts from ArrayList into a float array.
        
        fb.put(fa);
        */
        glBindBuffer( GL_ARRAY_BUFFER, this.vbuff );
        glBufferData( GL_ARRAY_BUFFER, this.vdata.length , this.vdata, GL_STATIC_DRAW );
        
    }
    
    public void draw(Program prog)
    {
        if(this.vbuff == 0)
            this.setup();
        glBindBuffer(GL_ARRAY_BUFFER, this.vbuff);
        //prog.setVertexFormat("a_position",2,GL_FLOAT); //not sure what to do here...
        glDrawArrays(GL_POINTS,0,this.np);
    }
    
}
