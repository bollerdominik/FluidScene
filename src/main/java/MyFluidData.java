import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import processing.core.PApplet;

/**
 * Created by dubsta on 09.03.2017.
 */
public class MyFluidData implements DwFluid2D.FluidData {

    PApplet parent;
    float px, py;
    public MyFluidData(PApplet parent) {
        this.parent = parent;
        //px = 0;
        //py = 0;
    }

    // update() is called during the fluid-simulation update step.
    public void update(DwFluid2D fluid) {

        float vx, vy, radius, vscale, r, g, b, intensity, temperature;

        // add impulse: density + temperature
        intensity = 1.0f;
        //I am setting the values in the other class to make it easier (for the moment) to understand
        //px = vase.getCurrentLocation().x;
        //py = vase.getCurrentLocation().y;

        radius = 20;
        r = (float)30/255;
        g = (float)144/255;
        b = (float)255/255;
        fluid.addDensity(px, py, radius, r, g, b, intensity);
        fluid.addVelocity(px, -5, radius, 0, -5);

        //if((fluid.simulation_step) % 200 == 0){
        //  temperature = 50f;
        //  fluid.addTemperature(px, py, radius, temperature);
        //}
        temperature = -50;
        fluid.addTemperature(px, py, radius, temperature);
        //fluid.addVelocity(px, py, radius, 0, -50);


        boolean mouse_input = parent.mousePressed;

        // add impulse: density + velocity
        if (mouse_input && parent.mouseButton == parent.LEFT) {
            radius = 15;
            vscale = 15;
            px = parent.mouseX;
            py = parent.height - parent.mouseY;
            vx = (parent.mouseX - parent.pmouseX) * +vscale;
            vy = (parent.mouseY - parent.pmouseY) * -vscale;

            fluid.addDensity(px, py, radius, 0.25f, 0.0f, 0.1f, 1.0f);
            fluid.addVelocity(px, py, radius, vx, vy);
        }

    }
}