/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import framework.math3d.mat4;
import framework.math3d.math3d;
import framework.math3d.vec3;
import framework.math3d.vec4;

/**
 *
 * @author skyler
 */
public class FlareSprite {
    public ImageTexture sprite;
    public float[] position;
    public float[] scale;
    
    public FlareSprite (String filename, vec4 pos, vec4 sc)
    {
        sprite = new ImageTexture(filename);
        position = new float[4];
        position[0] = pos.x;
        position[1] = pos.y;
        position[2] = pos.z;
        position[3] = pos.w;
        
        scale = new float[4];
        scale[0] = sc.x;
        scale[1] = sc.y;
        scale[2] = sc.z;
        scale[3] = sc.w;
        
    }
    
    public FlareSprite(String filename, vec3 pos, vec3 sc)
    {
        sprite = new ImageTexture(filename);
        position = new float[3];
        position[0] = pos.x;
        position[1] = pos.y;
        position[2] = pos.z;
        
        scale = new float[3];
        scale[0] = sc.x;
        scale[1] = sc.y;
        scale[2] = sc.z;
        
    }
    
    public mat4 GetTransform (vec3 lightPosition, int i)
    {
        mat4 S = math3d.scaling(new vec3 (scale[i], scale[i], scale[i]));
        float t = position[i];
        float px = lightPosition.x + t * (-lightPosition.x);
        float py = lightPosition.y + t * (-lightPosition.y);
        mat4 T = math3d.translation(new vec3(px,py,0.f));
        
        return T;
    }
}
