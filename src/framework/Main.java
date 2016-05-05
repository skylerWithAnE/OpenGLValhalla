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
import static framework.math3d.math3d.mul;
import static framework.math3d.math3d.sub;
import static framework.math3d.math3d.translation;
import static framework.math3d.math3d.mul;
import static framework.math3d.math3d.sub;
import static framework.math3d.math3d.translation;
import static framework.math3d.math3d.mul;
import static framework.math3d.math3d.sub;
import static framework.math3d.math3d.translation;


public class Main{
    
    static int floorsize = 1;
    static LinkedList<Bullet> activeBullets = new LinkedList<Bullet>();
    static Mesh[] room;
    static Mesh sun;
    static mat4[] roomTransforms = new mat4[floorsize];
    static mat4 trans = translation(new vec3(.0f,0.0f,.0f));
    static Player pc;
    static Program pcprog;
    
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
                    System.out.println("GL message: "+message);
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
        Camera cam, suncam;
        Program prog, progBlur, prog3, progGBlur, progUSQ, progSun, progPG, progFlare;
        float prev;
        
        //blur variables
        UnitSquare usqBlur = new UnitSquare();
        float blur_s = -1.0f;
        float blur_t = 0.5f;
        float blur_dir = 0.0f;
        
        Mesh column;
        UnitSquare usq;
        Framebuffer fbo1, fbo2, fbo3, sunfbo, avgillumfbo, depthMapfbo;
        Texture2D dummytex = new SolidTexture(GL_UNSIGNED_BYTE,0,0,0,0);
        column = new Mesh("assets/usq.obj.mesh");
        usq = new UnitSquare();

        fbo1 = new Framebuffer(512,512);
        fbo2 = new Framebuffer(512,512);
        fbo3 = new Framebuffer(512,512);
        
        //FBOs used for HDR/Glow/Lensflare
        sunfbo = new Framebuffer(16,16);
        avgillumfbo = new Framebuffer(1,1);
        depthMapfbo = new Framebuffer(512,512);
        
        //Basic Programs
//        prog = new Program("src/shaders/vs.txt","src/shaders/gs.txt","src/shaders/fs.txt");
        prog = new Program("src/shaders/vs.txt","src/shaders/fs.txt");
        pcprog = new Program("src/shaders/pcvs.txt","src/shaders/fs.txt");
        
        //Radial Blur Programs
        progBlur = new Program("src/shaders/vsBlur.txt","src/shaders/fsBlur.txt");
        prog3 = new Program("src/shaders/vsBlur.txt","src/shaders/fs2.txt");
        
        //HDR/Glow Lens Flare Programs
        progGBlur = new Program("src/shaders/gblur11vs.txt","src/shaders/gblur11fs.txt");
        progUSQ = new Program("src/shaders/usqvs.txt","src/shaders/usqfs.txt");
        progSun = new Program("src/shaders/sunvs.txt","src/shaders/sunfs.txt");
        progPG = new Program("src/shaders/pgvs.txt","src/shaders/pgfs.txt");
        progFlare = new Program("src/shaders/flarevs.txt","src/shaders/flarefs.txt");
        
        //Flare sprites
        FlareSprite[] flareSprites = new FlareSprite[3];
        flareSprites[0] = new FlareSprite("assets/flare1.png", new vec4(-0.6f, 0.25f, 1.1f, 1.7f), new vec4(0.1f, 0.05f, 0.15f, 0.3f));
        flareSprites[1] = new FlareSprite("assets/flare2.png", new vec3(-0.1f, 0.3f, 0.5f), new vec3(0.14f, 0.2f, 0.1f));
        flareSprites[2] = new FlareSprite("assets/flare3.png", new vec4(-0.4f, 0.4f, 0.7f, 1.2f), new vec4(0.06f, 0.04f, 0.08f, 0.16f));
        ImageTexture glowTex = new ImageTexture("assets/glow.png");
                
        PointGrid pgrid = new PointGrid(16,16);
        
        //Sun Camera (for HDR/Glow, Lensflare)
        suncam = new Camera();
       
        cam = new Camera();
        cam.lookAt( new vec3(0,0,5), new vec3(0,0,0), new vec3(0,1,0) );

        prev = (float)(System.nanoTime()*1E-9);
        trans = trans.mul(math3d.axisRotation(1.0f, 0.0f, 0.0f, 0f));

        SDL_Event ev=new SDL_Event();
        boolean canShoot = true;
        boolean blurDraw = false;
        float fireRate = 0.2f;
        float shootDelay = 0.0f;
        
        pc = new Player(new vec3(0f,0f,0f));
        float[] matTmp = new float[16];
        for(int l = 0; l < 16; l++)
            matTmp[l] = 1.f;
        room = new Mesh[floorsize];
        
        int row = 0;
        int col = 0;
        for(int i = 0; i < floorsize; i++)
        {
            row = i % 8==0 ? row+1 : row;
            col = i % 8==0 ? 0     : col+1;
            room[i] = new Mesh("assets/ground.obj.mesh");
            roomTransforms[i] = translation(new vec3((row-1)*25.0f, col*25.0f, 0.f));
        }
        
        cam.walk(-3f);
        sun = new Mesh("assets/bullet.obj.mesh");
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
                pc.velocity = new vec3(pc.velocity.x,0.5f,0.f);
            }
            if( keys.contains(SDLK_s))
            {
                cam.strafe(new vec3(0.f,-1.5f*elapsed,0.f));
                pc.velocity = new vec3(pc.velocity.x,-0.5f,0.f);
            }
            if( keys.contains(SDLK_a))
            {
                cam.strafe(new vec3(-1.5f*elapsed,0.f,0.f));
                pc.velocity = new vec3(-0.5f,pc.velocity.y,0.f);
            }
            if( keys.contains(SDLK_d))
            {
                cam.strafe(new vec3(1.5f*elapsed,0.f,0.f));
                pc.velocity = new vec3(0.5f,pc.velocity.y,0.f);
            }
            if( keys.contains(SDLK_i))
                cam.walk(1.5f*elapsed);
            if( keys.contains(SDLK_k))
                cam.walk(-1.5f*elapsed);
            
            if(canShoot)
            {
                if( keys.contains(SDLK_UP))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getBulletSpawnLocation(), new vec3(0.f,1.f,0.f)));

                }
                if( keys.contains(SDLK_DOWN))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getBulletSpawnLocation(), new vec3(0.f,-1.f,0.f)));
                }
                if(keys.contains(SDLK_LEFT))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getBulletSpawnLocation(), new vec3(-1.f,0.f,0.f)));
                }
                if(keys.contains(SDLK_RIGHT))
                {
                    canShoot = false;
                    activeBullets.add(new Bullet(pc.getBulletSpawnLocation(), new vec3(1.f,0.f,0.f)));
                }
            }
            
            if(keys.contains(SDLK_o))
                pc.adjustOffset(-1*elapsed,0);
            if(keys.contains(SDLK_p))
                pc.adjustOffset(1*elapsed,0);
            
            if(keys.contains(SDLK_SPACE))
            {
                if(!blurDraw)
                {
                    blurDraw = true;
                    blur_dir = 1f;
                }
                
                if(blur_s > 0.9f)
                    blur_dir = -1f;
                if(blur_s < 0f)
                    blur_dir = 1f;
                blur_s += elapsed * blur_dir;
            }
            else
            {
                blur_s = 0f;
                //blur_t = 0f;
                blurDraw = false;
            }
 
            trans = trans.mul(math3d.axisRotation(1.0f, 0.0f, 0.0f, 0.0f));
            
            if(blurDraw)
            {
                //need to break this down into functions.

                fbo1.bind(); //bind this fbo, draw unblurred
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                drawScene(prog, cam, elapsed);
                fbo1.unbind();

                //draw a blurred copy of the scene to fbo3
                progBlur.use();
                progBlur.setUniform("blur_center",new vec2(blur_s,blur_t));
                progBlur.setUniform("do_normalize", 1f);
                progBlur.setUniform("stepsize",0.01f);
                progBlur.setUniform("numsteps",6.0f);
                fbo3.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                progBlur.setUniform("basetexture",fbo1.texture);
                usqBlur.draw(progBlur);
                fbo3.unbind();

                //not using fbo2 because not "experimental hacks"

                //finally, draw to screen
                prog3.use();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                prog3.setUniform("tex1",fbo3.texture);
                prog3.setUniform("knob", 0.0f);
                usqBlur.draw(prog3);
                prog3.setUniform("tex1",dummytex); //why?
            }
            
            else
            {
                //using logluv, disable blending
                glDisable(GL_BLEND);
                fbo2.texture.unbind();
                fbo2.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                drawSceneFBO(prog, cam, new vec3(10,10,10));
                fbo2.unbind();
                progGBlur.use();
                
                for(int i = 0; i < 5; ++i) //RuntimeException when this loop is enabled.
                {
                    //horizontal gauss blur; read from fbo1, write to fbo2.
                    fbo3.texture.unbind();
                    fbo3.bind();
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    
                    progGBlur.setUniform("tex", fbo2.texture);
                    progGBlur.setUniform("deltas",new vec2(1,0));
                    usq.draw(progGBlur);
                    fbo3.unbind();
                    fbo2.texture.unbind();
                    
                    fbo2.bind();
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    progGBlur.setUniform("deltas",new vec2(0,1));
                    progGBlur.setUniform("tex", fbo3.texture);
                    usq.draw(progGBlur);
                    fbo2.unbind();
                    
                    //while(Framebuffer.active_fbo != null) {}
                }
                
                fbo3.texture.unbind();
                fbo3.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                drawSceneFBO(prog, cam, new vec3(10,10,10));
                fbo3.unbind();
                
                //now combine the two together.
                glEnable(GL_BLEND);
                glBlendFunc(GL_ONE, GL_ONE);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                progUSQ.use();
                progUSQ.setUniform("basetexture", fbo2.texture);
                progUSQ.setUniform("worldMatrix", mat4.identity());
                usq.draw(progUSQ);
                progUSQ.setUniform("basetexture", fbo3.texture);
                usq.draw(progUSQ);
                //reset blendFunc
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                
                //HDR stuff
                sunfbo.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                suncam.lookAt   (new vec3(cam.eye.x, cam.eye.y, cam.eye.z),
                                new vec3(50,50,50), 
                                new vec3(cam.U.x, cam.U.y, cam.U.z));
                glColorMask(false,false,false,false);
                drawScene(progSun, suncam, elapsed);    //need depth buffer
                glColorMask(true,true,true,true);
                progSun.setUniform("worldMatrix", translation(new vec3(50,50,50)));
                sun.draw(progSun);
                sunfbo.unbind();
                
                //determine how much of the "sun" is visible. (probably will need to
                //translate the sun for proper results.
                glBlendFunc(GL_ONE, GL_ONE);
                avgillumfbo.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                progPG.use();
                progPG.setUniform("tex", sunfbo.texture);
                pgrid.draw(progPG);
                avgillumfbo.unbind();
                
                Camera overcam = new Camera();
                overcam.lookAt( new vec3(0f,10f,0f), 
                                new vec3 (0f,0f,0f), new vec3 (0f,0f,1f));
                overcam = cam;
                
                //now draw the normal scene.
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                drawScene(prog, overcam, elapsed);
                vec4 lPos = new vec4(50f,50f,50f,0f);
                lPos.z = 1.0f;
                lPos = math3d.mul(lPos, cam.viewMatrix);
                lPos.x /= lPos.z;
                lPos.y /= lPos.z;
                
                //as light approaches the edge of the screen,
                //reduce alpha fade to zero, along with sprites.
                float alphaScale = 0.f;
                float qx = Math.abs(lPos.x);
                float qy = Math.abs(lPos.y);
                float q  = qx > qy ? qx : qy;
                //when q reaches 0.9, start fade to zero.
                //end fade when q reaches 1.2.
                float fstart = 0.9f;
                float fend = 1.2f;
                alphaScale = 1.0f - (q - fstart) / (fend - fstart);
                
                //No clamp method in Java?
                if(alphaScale < 0.0f)
                    alphaScale = 0.0f;
                if(alphaScale > 1.0f)
                    alphaScale = 1.0f;
                    
                glBlendFunc(GL_SRC_ALPHA, GL_ONE);
                progFlare.use();
                progFlare.setUniform("avgillumtexture",avgillumfbo.texture);
                progFlare.setUniform("alphascale",alphaScale);
                //draw glow around light source.
                //if(1)?
                progFlare.setUniform("worldMatrix", math3d.translation(new vec3 (lPos.x, lPos.y, 0.f)));
                progFlare.setUniform("basetexture", glowTex);
                usq.draw(progFlare);
                
                mat4[][] flareTransforms = new mat4[flareSprites.length][];
                for(FlareSprite fs : flareSprites)
                {
                    progFlare.setUniform("basetexture", fs.sprite);
                    for(int i = 0; i < fs.position.length; i++)
                    {
                        mat4 flareTrans = fs.GetTransform(new vec3(lPos.x, lPos.y, lPos.z), i);
                        progFlare.setUniform("worldMatrix", flareTrans);
                        usq.draw(progFlare);
                    }
                    
                }
                avgillumfbo.texture.unbind();
                
            }
            //shadowmapping stuff (kills framerate, doesn't work--doing something wrong here)
            
//            int[] depthMap = new int[1];
//            glGenTextures(1, depthMap);
//            glBindTexture(GL_TEXTURE_2D, depthMap[0]);
//            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 1024, 1024, 
//                            0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
//
//            depthMapfbo.bind();
//            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap[0], 0);
//            glDrawBuffer(GL_NONE);
//            glReadBuffer(GL_NONE);
//            depthMapfbo.unbind();
            

            SDL_GL_SwapWindow(win);


        }//endwhile
    }//end main
    public static void drawScene(Program prog, Camera cam, float elapsed)
    {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        prog.use();
        prog.setUniform("lightPos",new vec3(30,30,30) );
        prog.setUniform("lightColor", new vec3(1,1,1));
        prog.setUniform("transform", trans);
        prog.setUniform("worldMatrix",mat4.identity());
        cam.draw(prog);
        prog.setUniform("transform", translation(new vec3(0f,30f,30f)));
        sun.draw(prog);
        
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
        
    }
    
        public static void drawSceneFBO(Program prog, Camera cam, vec3 lightColor)
    {
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        prog.use();
        prog.setUniform("lightPos",new vec3(30,30,30) );
        prog.setUniform("lightColor", lightColor);
        prog.setUniform("transform", trans);
        prog.setUniform("worldMatrix",mat4.identity());
        cam.draw(prog);
        sun.draw(prog);
        
        for(int j = 0; j < floorsize; j++)
        {
            prog.setUniform("transform", roomTransforms[j]);
            room[j].draw(prog);
            
        }
        for(int i = 0; i < activeBullets.size(); i++)
        {
            activeBullets.get(i).update(0, prog);
        }
        pc.Update(0, prog);
        
    }
}
