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
    
    public Bullet(mat4 initialTransform, vec3 initialVelocity)
    {
        speed = 0.8f;
        this.velocity = initialVelocity;
        this.transform = new mat4();
        this.transform = initialTransform;
        this.bulletShape = new Mesh("assets/bullet.obj.mesh");
    }
    
    public void update(float dT, Program prog)
    {
        
        mat4 trans = this.transform.mul(math3d.translation(this.velocity.mul((speed*dT))));
        this.transform = trans;
        prog.setUniform("transform", this.transform);
        this.bulletShape.draw(prog);
    }
}
