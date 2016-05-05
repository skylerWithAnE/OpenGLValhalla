/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import java.util.ArrayList;
import java.util.TreeMap;
import org.w3c.dom.Entity;  //huh?

/**
 *
 * @author skyler
 */
public class CollisionGrid {
    float xmin, xmax, ymin, ymax, S;
     TreeMap<Integer,TreeMap<Integer,ArrayList<Entity>>> G = new TreeMap<>();
     public CollisionGrid(float xmin, float xmax, float ymin, float ymax, float S) {
         this.xmin = xmin;
         this.xmax = xmax;
         this.ymin = ymin;
         this.ymax = ymax;
         this.S = S;    //size of each cell.
     }
     int[] put(float x, float y, Entity obj, int[] oldcell) {
         int i = (int)((x-this.xmin)/this.S);
         int j = (int)((x-this.xmin)/this.S);
         if(oldcell!=null && i == oldcell[0] && j == oldcell[1])
             return oldcell;    //no change.
         if(oldcell!=null) {
             //remove obj from G[oldcell[0], oldcell[1]]
         }
         if (!G.containsKey(i)) 
             this.G.put(i, new TreeMap<>());
         if(!this.G.get(i).containsKey(j))
             this.G.get(i).put(j, new ArrayList<>());
             //this.G.get(i).put(j,new TreeMap<>()); //bad.
         this.G.get(i).get(j).add(obj);
         return new int[] {i,j};
     }
     
     ArrayList<int[]> getProximate(int[] cell) {
         ArrayList<int[]> L = new ArrayList<>();
         for(int i = cell[0] -1; i<= cell[0]+1; ++i) {
             for(int j = cell[1]-1; j <= cell[1]+1; j++) {
                 if(this.G.containsKey(i) && this.G.get(i).containsKey(j))
                     L.add(new int [] {i,j});
             }
         }
         return L;
     }
}
