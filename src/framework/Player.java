/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;
import framework.math3d.mat4;
import framework.math3d.vec3;
import framework.math3d.vec4;
import framework.math3d.math3d;
/**
 *
 * @author skyler
 */
public class Player {
    private Mesh playerShape;
    private mat4 transform;
    public AABB boundingBox;
    public vec3 velocity;
    public vec3 position;
    public vec3 bulletOffset;
    
    public Player(vec3 initialPosition)
    {
        this.position = new vec3();
        this.transform = math3d.translation(initialPosition);
        this.velocity = new vec3();
        this.playerShape = new Mesh("assets/player.obj.mesh");
        this.bulletOffset = new vec3(-0.27,1.04,0);
        this.boundingBox = new AABB(1f,1f, initialPosition.x, initialPosition.y);
    }
    
    public void adjustOffset(float dirX, float dirY)
    {
        
        this.bulletOffset = this.bulletOffset.add(new vec3(dirX,dirY,0));
        System.out.println("in: "+dirX+", "+dirY+" out: "+bulletOffset);
    }
    
    public mat4 getBulletSpawnLocation()
    {
        
        mat4 outTrans = this.transform.mul(math3d.translation(this.bulletOffset));
        return outTrans;
        //return this.transform;
    }
    
    public void Update(float dT, Program prog)
    {
        if(dT > 0) {
            this.transform = this.transform.mul(math3d.translation(this.velocity.mul(dT)));
            this.position = this.position.add(this.velocity);
            this.boundingBox.update(this.transform.get(3,0)+bulletOffset.x, this.transform.get(3,1)+bulletOffset.y);
        }
        Draw(prog);
        if(dT > 0)
            this.velocity = new vec3(0.f,0.f,0.f);
    }
    
    private void Draw(Program prog)
    {
        //prog.setUniform("transform", this.transform);
        prog.setUniform("worldMatrix", transform.mul(mat4.identity()));
        prog.setUniform("diffusemtl", new vec4(1f,0f,0f,1f));
        this.playerShape.draw(prog);
    }
    
    public mat4 getTransform()
    {
        return this.transform;
    }
}
