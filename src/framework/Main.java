package framework;

import framework.math3d.vec3;
import framework.math3d.mat4;
import java.util.Set;
import java.util.TreeSet;
import java.util.LinkedList;
import static JGL.JGL.*;
import static JSDL.JSDL.*;
import framework.math3d.math3d;
import static framework.math3d.math3d.mul;
import static framework.math3d.math3d.sub;
import static framework.math3d.math3d.translation;
import framework.math3d.vec2;
import framework.math3d.vec4;


public class Main{
    
    
    public static void main(String[] args){
        
        SDL_Init(SDL_INIT_VIDEO);
        long win = SDL_CreateWindow("ETGG 2802",40,60, 512,512, SDL_WINDOW_OPENGL );
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK,SDL_GL_CONTEXT_PROFILE_CORE);
        SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE,24);
        SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE,8);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION,3);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION,2);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_FLAGS, SDL_GL_CONTEXT_DEBUG_FLAG);
        SDL_GL_CreateContext(win);
        
        glDebugMessageControl(GL_DONT_CARE,GL_DONT_CARE,GL_DONT_CARE, 0,null, true );
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDebugMessageCallback(
                (int source, int type, int id, int severity, String message, Object obj ) -> {
                    //System.out.println("GL message: "+message);
                    //if( severity == GL_DEBUG_SEVERITY_HIGH )
                    //    System.exit(1);
                },
                null);

        int[] tmp = new int[1];
        glGenVertexArrays(1,tmp);
        int vao = tmp[0];
        glBindVertexArray(vao);

        glClearColor(0.2f,0.4f,0.6f,1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Set<Integer> keys = new TreeSet<>();
        Camera cam;
        Program prog, prog2;
        float prev;
        Mesh column;
        UnitSquare usq;
        Framebuffer fbo1;
        Framebuffer fbo2;
        Texture2D dummytex = new SolidTexture(GL_UNSIGNED_BYTE,0,0,0,0);
        column = new Mesh("assets/usq.obj.mesh");
        usq = new UnitSquare();

        fbo1 = new Framebuffer(512,512);
        fbo2 = new Framebuffer(512,512);

        prog = new Program("vs.txt","gs.txt","fs.txt");
       
        cam = new Camera();
        cam.lookAt( new vec3(0,0,5), new vec3(0,0,0), new vec3(0,1,0) );

        prev = (float)(System.nanoTime()*1E-9);
        vec3 vect = new vec3(.0f,0.0f,.0f);
        mat4 trans = translation(vect);
        trans = trans.mul(math3d.axisRotation(1.0f, 0.0f, 0.0f, 0f));

        SDL_Event ev=new SDL_Event();
        boolean canShoot = true;
        boolean drawColumn = false;
        float fireRate = 0.2f;
        float shootDelay = 0.0f;
        LinkedList<Bullet> activeBullets = new LinkedList<Bullet>();
        Player pc = new Player(new vec3(0f,0f,0f));
        float[] matTmp = new float[16];
        for(int l = 0; l < 16; l++)
            matTmp[l] = 1.f;
        mat4 roomTransform = translation(new vec3());
        System.out.println(roomTransform.toString());
        int floorsize = 256;
        Mesh[] room = new Mesh[floorsize];
        mat4[] roomTransforms = new mat4[floorsize];
        int row = 0;
        int col = 0;
        for(int i = 0; i < floorsize; i++)
        {
            row = i % 8==0 ? row+1 : row;
            col = i % 8==0 ? 0     : col+1;
            System.out.println("x: " + String.valueOf((row-1)*2.0f) + " y: " + String.valueOf(2.0f*(col)));
            room[i] = new Mesh("assets/usq.obj.mesh");
            roomTransforms[i] = translation(new vec3((row-1)*2.0f, col*2.0f, 0.f));
        }
        while(true){
            while(true){
                int rv = SDL_PollEvent(ev);
                if( rv == 0 )
                    break;
                if( ev.type == SDL_QUIT )
                    System.exit(0);
                if( ev.type == SDL_MOUSEMOTION ){
                }
                if( ev.type == SDL_KEYDOWN ){
                    keys.add(ev.key.keysym.sym);
                }
                if( ev.type == SDL_KEYUP ){
                    keys.remove(ev.key.keysym.sym);
                }
            }

            float now = (float)(System.nanoTime()*1E-9);
            float elapsed = now-prev;
            if(!canShoot)
            {
                shootDelay += elapsed;
                if(shootDelay > fireRate)
                {
                    canShoot = true;
                    shootDelay = 0;
                }
            }

            prev=now;

            if( keys.contains(SDLK_w ))
            {
                cam.strafe(new vec3(0.f,1.5f*elapsed,0.f));
                pc.velocity = new vec3(pc.velocity.x,1.5f,0.f);
            }
            if( keys.contains(SDLK_s))
            {
                cam.strafe(new vec3(0.f,-1.5f*elapsed,0.f));
                pc.velocity = new vec3(pc.velocity.x,-1.5f,0.f);
            }
            if( keys.contains(SDLK_a))
            {
                cam.strafe(new vec3(-1.5f*elapsed,0.f,0.f));
                pc.velocity = new vec3(-1.5f,pc.velocity.y,0.f);
            }
            if( keys.contains(SDLK_d))
            {
                cam.strafe(new vec3(1.5f*elapsed,0.f,0.f));
                pc.velocity = new vec3(1.5f,pc.velocity.y,0.f);
            }
            if( keys.contains(SDLK_i))
                cam.walk(1.5f*elapsed);
            if( keys.contains(SDLK_k))
                cam.walk(-1.5f*elapsed);
            //if( keys.contains(SDLK_r))
                //cam.tilt(0.4f*elapsed);
            //if( keys.contains(SDLK_t))
                //cam.tilt(-0.4f*elapsed);
            if(canShoot)
            {
                if( keys.contains(SDLK_UP))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getTransform(), new vec3(0.f,1.f,0.f)));

                }
                if( keys.contains(SDLK_DOWN))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getTransform(), new vec3(0.f,-1.f,0.f)));
                }
                if(keys.contains(SDLK_LEFT))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getTransform(), new vec3(-1.f,0.f,0.f)));
                }
                if(keys.contains(SDLK_RIGHT))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getTransform(), new vec3(1.f,0.f,0.f)));
                }
            }
            if(keys.contains(SDLK_SPACE))
            {
                drawColumn = !drawColumn;
            }
            
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            trans = trans.mul(math3d.axisRotation(1.0f, 0.0f, 0.0f, 0.0f));
            
            //the fbo stuff is for later...
            //fbo1.bind();
            prog.use();
            prog.setUniform("lightPos",new vec3(50,50,50) );
            prog.setUniform("transform", roomTransform);
            
            prog.setUniform("transform", trans);
            prog.setUniform("worldMatrix",mat4.identity());
            //column.draw(prog);
            for(int j = 0; j < floorsize; j++)
            {
                prog.setUniform("transform", roomTransforms[j]);
                room[j].draw(prog);
            }
            for(int i = 0; i < activeBullets.size(); i++)
            {
                activeBullets.get(i).update(elapsed, prog);
            }
            pc.Update(elapsed, prog);
            cam.draw(prog);
            

            
            //fbo1.unbind();

            //this is also for later...
/*
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            blurprog.use();
            blurprog.setUniform("diffuse_texture",fbo1.texture);
            usq.draw(blurprog);
            blurprog.setUniform("diffuse_texture",dummytex);
*/

            SDL_GL_SwapWindow(win);


        }//endwhile
    }//end main
}
