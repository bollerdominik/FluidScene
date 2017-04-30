import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PGraphics2D;

/**
 * Created by dubsta on 23.02.2017.
 */


public class FluidScene extends PApplet {



    // render targets
    PGraphics2D pg_fluid;
    //texture-buffer, for adding obstacles
    PGraphics2D pg_obstacles;



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
        size(400, 400, P3D);
        smooth(2);
    }

    @Override
    public void setup() {

        font = createFont("Verdana",30);
        TextX = width; // initialize text offscreen
        pg_obstacles = (PGraphics2D) createGraphics(400,400,P2D);

    }

    @Override
    public void draw() {
        // display vase
        background(51);
            pg_obstacles.beginDraw();
            pg_obstacles.clear();
            pg_obstacles.colorMode(HSB, 100);
            pg_obstacles.textFont(font);
            pg_obstacles.text(headlines[index],TextX, height - 40);

            TextX = TextX - 2;

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



        //Hide obstacle
        image(pg_obstacles, 0, 0);


    }



}

