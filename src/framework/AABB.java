/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

/**
 *
 * @author skyler
 */
public class AABB {
    float width;
    float height;
    float x;
    float y;
    
    AABB(float width, float height, float x, float y) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }
    
    public boolean overlapCheck(AABB other) {
        
        return (this.x < other.x + other.width && this.x + this.width > other.x
                && this.y < other.y + other.height && this.height + other.y > other.y);
    }
    
    public void update(float x, float y) {
        //time dilation handled by the moving bodies.
        this.x = x;
        this.y = y;
    }
    
}
