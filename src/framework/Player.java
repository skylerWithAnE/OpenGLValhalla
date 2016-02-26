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
public class Player {
    private Mesh playerShape;
    private mat4 transform;
    public vec3 velocity;
    public vec3 position;
    
    public Player(vec3 initialPosition)
    {
        this.position = new vec3();
        this.transform = math3d.translation(initialPosition);
        this.velocity = new vec3();
        this.playerShape = new Mesh("assets/torus.obj.mesh");
    }
    
    public void Update(float dT, Program prog)
    {
        this.transform = this.transform.mul(math3d.translation(this.velocity.mul(dT)));
        prog.setUniform("transform", this.transform);
        this.playerShape.draw(prog);
        this.position = this.position.add(this.velocity);
        this.velocity = new vec3(0.f,0.f,0.f);
    }
    
    public mat4 getTransform()
    {
        return this.transform;
    }
}