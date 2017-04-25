/**
 * Created by dubsta on 23.02.2017.
 */
import controlP5.Accordion;
import controlP5.ControlP5;
import controlP5.Group;
import controlP5.RadioButton;
import controlP5.Toggle;
import processing.core.*;
import processing.opengl.PGraphics2D;

import processing.opengl.PGraphics3D;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;

public class FluidScene extends PApplet {


    VaseShape vase;

    private ControlP5 cp5;
    int viewport_w = 600;
    int viewport_h = 600;
    int fluidgrid_scale = 1;

    int gui_w = 200;
    int gui_x = 20;
    int gui_y = 20;

    DwFluid2D fluid;
    ObstaclePainter obstacle_painter;

    // render targets
    PGraphics2D pg_fluid;
    //texture-buffer, for adding obstacles
    PGraphics2D pg_obstacles;

    // some state variables for the GUI/display
    int     BACKGROUND_COLOR           = 255;
    boolean UPDATE_FLUID               = true;
    boolean DISPLAY_FLUID_TEXTURES     = true;
    boolean DISPLAY_FLUID_VECTORS      = false;
    int DISPLAY_fluid_texture_mode = 0;
    boolean moveVase = true;

    String[] headlines = {"Ceramic produced and decorated by craftsmen at the Madoura atelier in Vallaouris, France.",
            "Picasso started making ceramics in 1947 at the Madoura workshop, following a rather unconventional process",
            "As he didn’t know how to throw a pot on the wheel, he relied on a potter to create the desired shapes and then he hand-shaped, decorated and fired the objects himself",
            "As a favour to the owners of the workshop, Picasso allowed them to make authentic copies and sell them"};
    int index = 0;
    int TextX = 100;
    int TextY = 560;
    PFont font;

    public static void main(String args[]) {
        PApplet.main("FluidScene");
    }

    @Override
    public void settings() {
        size(viewport_w, viewport_h, P3D);
        smooth(2);
    }

    @Override
    public void setup() {

        font = createFont("Verdana",30);
        TextX = width; // initialize text offscreen

        // main library context
        DwPixelFlow context = new DwPixelFlow(this);
        context.print();
        context.printGL();

        // fluid simulation
        fluid = new DwFluid2D(context, viewport_w, viewport_h, fluidgrid_scale);

        // set some simulation parameters
        fluid.param.dissipation_density     = 0.999f;
        fluid.param.dissipation_velocity    = 0.99f;
        fluid.param.dissipation_temperature = 0.80f;
        fluid.param.vorticity               = 0.10f;

        // interface for adding data to the fluid simulation
        createGUI();
        MyFluidData cb_fluid_data = new MyFluidData(this,cp5);
        fluid.addCallback_FluiData(cb_fluid_data);

        // pgraphics for fluid
        pg_fluid = (PGraphics2D) createGraphics(viewport_w, viewport_h, P2D);
        pg_fluid.smooth(4);
        pg_fluid.beginDraw();
        pg_fluid.background(BACKGROUND_COLOR);
        pg_fluid.endDraw();

        pg_obstacles = (PGraphics2D) createGraphics(viewport_w, viewport_h, P2D);
        //pg_obstacles.smooth(0);
            //pushMatrix();
        pg_obstacles.beginDraw();
        pg_obstacles.clear();
        pg_obstacles.fill(255,0,0);
        pg_obstacles.translate((width/2),(height/2));
        pg_obstacles.rotate(radians(frameCount));
        PShape bot =  pg_obstacles.loadShape("src\\main\\resources\\drawing.svg");
        pg_obstacles.shape(bot, -80, -120, 250, 237);
        pg_obstacles.endDraw();



        obstacle_painter = new ObstaclePainter(pg_obstacles,this);
        vase = new VaseShape(loadShape("src\\main\\resources\\blue_bird.obj"),this,cb_fluid_data);
    }

    @Override
    public void draw() {
        // display vase
        vase.animation();
        if(moveVase) vase.movement++;


        // update simulation
        if(UPDATE_FLUID){

            pg_obstacles.beginDraw();
            pg_obstacles.clear();
            pg_obstacles.colorMode(HSB, 100);
            pg_obstacles.textFont(font);
            pg_obstacles.text(headlines[index],TextX, height - 40);
            pg_obstacles.fill(255,0,0);
            pg_obstacles.translate((width/2),(height/2));
            pg_obstacles.rotate(radians(vase.movement));
            PShape bot =  pg_obstacles.loadShape("src\\main\\resources\\drawing.svg");
            pg_obstacles.shape(bot, -75, -70, 250, 137);
            TextX = TextX - 3;

            // If x is less than the negative width, then it is off the screen
            // textWidth() is used to calculate the width of the current String.
            float W = pg_obstacles.textWidth(headlines[index]);
            if (TextX < -W)
            {
                TextX = width;
                // index is incremented when the current String has left the screen in order to display a new String.
                index = (index + 1) % headlines.length;
            }

            pg_obstacles.endDraw();

            fluid.update();
            fluid.addObstacles(pg_obstacles);

        }

        // clear render target
        pg_fluid.beginDraw();
        pg_fluid.background(BACKGROUND_COLOR);
        pg_fluid.endDraw();


        // render fluid stuff
        if(DISPLAY_FLUID_TEXTURES){
            // render: density (0), temperature (1), pressure (2), velocity (3)
            fluid.renderFluidTextures(pg_fluid, DISPLAY_fluid_texture_mode);
        }

        if(DISPLAY_FLUID_VECTORS){
            // render: velocity vector field
            fluid.renderFluidVectors(pg_fluid, 10);
        }


        // display
        pg_obstacles.translate(frameCount,0);
        image(pg_fluid    , 0, 0);
        //Hide obstacle
        image(pg_obstacles, 0, 0);

        obstacle_painter.displayBrush(this.g);

        // info
        String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [frame %d]   [fps %6.2f]", fluid.fluid_w, fluid.fluid_h, fluid.simulation_step, frameRate);
        surface.setTitle(txt_fps);
    }

    public void mousePressed(){
    }

    public void fluid_resizeUp(){
        fluid.resize(width, height, fluidgrid_scale = max(1, --fluidgrid_scale));
    }
    public void fluid_resizeDown(){
        fluid.resize(width, height, ++fluidgrid_scale);
    }
    public void fluid_reset(){
        fluid.reset();
    }
    public void fluid_togglePause(){
        UPDATE_FLUID = !UPDATE_FLUID;
    }
    public void fluid_displayMode(int val){
        DISPLAY_fluid_texture_mode = val;
        DISPLAY_FLUID_TEXTURES = DISPLAY_fluid_texture_mode != -1;
    }
    public void fluid_displayVelocityVectors(int val){
        DISPLAY_FLUID_VECTORS = val != -1;
    }

    public void keyReleased(){
        if(key == 'p') fluid_togglePause(); // pause / unpause simulation
        if(key == '+') fluid_resizeUp();    // increase fluid-grid resolution
        if(key == '-') fluid_resizeDown();  // decrease fluid-grid resolution
        if(key == 'r') fluid_reset();       // restart simulation

        if(key == '1') DISPLAY_fluid_texture_mode = 0; // density
        if(key == '2') DISPLAY_fluid_texture_mode = 1; // temperature
        if(key == '3') DISPLAY_fluid_texture_mode = 2; // pressure
        if(key == '4') DISPLAY_fluid_texture_mode = 3; // velocity

        if(key == 'q') DISPLAY_FLUID_TEXTURES = !DISPLAY_FLUID_TEXTURES;
        if(key == 'w') DISPLAY_FLUID_VECTORS  = !DISPLAY_FLUID_VECTORS;
        if (key == '.') moveVase = !moveVase;
    }

    public void createGUI(){
        cp5 = new ControlP5(this);

        int sx, sy, px, py, oy;

        sx = 100; sy = 14; oy = (int)(sy*1.5f);


        ////////////////////////////////////////////////////////////////////////////
        // GUI - FLUID
        ////////////////////////////////////////////////////////////////////////////
        Group group_fluid = cp5.addGroup("fluid");
        {
            group_fluid.setHeight(20).setSize(gui_w, 300)
                    .setBackgroundColor(color(16, 180)).setColorBackground(color(16, 180));
            group_fluid.getCaptionLabel().align(CENTER, CENTER);

            px = 10; py = 15;

            cp5.addButton("reset").setGroup(group_fluid).plugTo(this, "fluid_reset"     ).setSize(80, 18).setPosition(px    , py);
            cp5.addButton("+"    ).setGroup(group_fluid).plugTo(this, "fluid_resizeUp"  ).setSize(39, 18).setPosition(px+=82, py);
            cp5.addButton("-"    ).setGroup(group_fluid).plugTo(this, "fluid_resizeDown").setSize(39, 18).setPosition(px+=41, py);

            px = 10;

            cp5.addSlider("velocity").setGroup(group_fluid).setSize(sx, sy).setPosition(px, py+=(int)(oy*1.5f))
                    .setRange(0, 1).setValue(fluid.param.dissipation_velocity).plugTo(fluid.param, "dissipation_velocity");

            cp5.addSlider("density").setGroup(group_fluid).setSize(sx, sy).setPosition(px, py+=oy)
                    .setRange(0, 1).setValue(fluid.param.dissipation_density).plugTo(fluid.param, "dissipation_density");

            cp5.addSlider("temperature").setGroup(group_fluid).setSize(sx, sy).setPosition(px, py+=oy)
                    .setRange(0, 1).setValue(fluid.param.dissipation_temperature).plugTo(fluid.param, "dissipation_temperature");

            cp5.addSlider("vorticity").setGroup(group_fluid).setSize(sx, sy).setPosition(px, py+=oy)
                    .setRange(0, 1).setValue(fluid.param.vorticity).plugTo(fluid.param, "vorticity");

            cp5.addSlider("iterations").setGroup(group_fluid).setSize(sx, sy).setPosition(px, py+=oy)
                    .setRange(0, 80).setValue(fluid.param.num_jacobi_projection).plugTo(fluid.param, "num_jacobi_projection");

            cp5.addSlider("timestep").setGroup(group_fluid).setSize(sx, sy).setPosition(px, py+=oy)
                    .setRange(0, 1).setValue(fluid.param.timestep).plugTo(fluid.param, "timestep");

            cp5.addSlider("gridscale").setGroup(group_fluid).setSize(sx, sy).setPosition(px, py+=oy)
                    .setRange(0, 50).setValue(fluid.param.gridscale).plugTo(fluid.param, "gridscale");

            RadioButton rb_setFluid_DisplayMode = cp5.addRadio("fluid_displayMode").setGroup(group_fluid).setSize(80,18).setPosition(px, py+=(int)(oy*1.5f))
                    .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(2)
                    .addItem("Density"    ,0)
                    .addItem("Temperature",1)
                    .addItem("Pressure"   ,2)
                    .addItem("Velocity"   ,3)
                    .activate(DISPLAY_fluid_texture_mode);
            for(Toggle toggle : rb_setFluid_DisplayMode.getItems()) toggle.getCaptionLabel().alignX(CENTER);

            cp5.addRadio("fluid_displayVelocityVectors").setGroup(group_fluid).setSize(18,18).setPosition(px, py+=(int)(oy*2.5f))
                    .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(1)
                    .addItem("Velocity Vectors", 0)
                    .activate(DISPLAY_FLUID_VECTORS ? 0 : 2);
        }


        ////////////////////////////////////////////////////////////////////////////
        // GUI - DISPLAY
        ////////////////////////////////////////////////////////////////////////////
        Group group_display = cp5.addGroup("display");
        {
            group_display.setHeight(20).setSize(gui_w, 50)
                    .setBackgroundColor(color(16, 180)).setColorBackground(color(16, 180));
            group_display.getCaptionLabel().align(CENTER, CENTER);

            px = 10; py = 15;

            cp5.addSlider("BACKGROUND").setGroup(group_display).setSize(sx,sy).setPosition(px, py)
                    .setRange(0, 255).setValue(BACKGROUND_COLOR).plugTo(this, "BACKGROUND_COLOR");
        }


        ////////////////////////////////////////////////////////////////////////////
        // GUI - ACCORDION
        ////////////////////////////////////////////////////////////////////////////
        cp5.addAccordion("acc").setPosition(gui_x, gui_y).setWidth(gui_w).setSize(gui_w, height)
                .setCollapseMode(Accordion.MULTI)
                .addItem(group_fluid)
                .addItem(group_display)
                .open(4);
    }
}

