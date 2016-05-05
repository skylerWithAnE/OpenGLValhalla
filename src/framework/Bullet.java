/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import framework.math3d.mat4;
import framework.math3d.vec3;
import framework.math3d.math3d;

/**
 *
 * @author skyler
 */
public class Bullet {
    private Mesh bulletShape;
    public mat4 transform;
    public vec3 velocity;
    public float speed;
    public float lifetime;
    
    public Bullet(mat4 initialTransform, vec3 initialVelocity)
    {
        lifetime = 1.0f;
        speed = 0.8f;
        this.velocity = initialVelocity;
        this.transform = new mat4();
        this.transform = initialTransform;
        this.bulletShape = new Mesh("assets/bullet.obj.mesh");
        bulletShape.emit_texture = new ImageTexture("assets/emissivemap.png");
    }
    
    public void update(float dT, Program prog)
    {
        lifetime-=dT;
        if(lifetime <= 0) {
//            this.bulletShape.
        }
        mat4 trans = this.transform.mul(math3d.translation(this.velocity.mul((speed*dT))));
        this.transform = trans;
        prog.setUniform("transform", this.transform);
        prog.setUniform("emissionscale", 1f);
        this.bulletShape.draw(prog);
        //remove stored uniform so that only the bullet is glowing green--not everything.
        prog.setUniform("emissionscale", 0f);
    }
}
