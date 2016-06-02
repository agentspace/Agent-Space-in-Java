
import java.util.*;
/**
*Class Agent extends class Thread, implements interface Triggerable.
*/
public abstract class Agent extends Thread implements Triggerable {

    Object monitor;
    Timer timer;
    Space space;

    public Agent() {
        monitor = new Object();
        space = Space.getInstance();
        timer = null;
    }

    public void setTimer(int period) {   // to be called from init()
        if (timer == null) {
            timer = new Timer(true);
        }
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        trigger();
                    }
                }, period, period
        );
    }

    public void setTrigger(String name) {   // to be called from init()
        space.attachTrigger(name, this);
    }

    abstract void init(); // to be overriden

    abstract void sense_select_act();  // to be overriden

    private int receive() {
        int ret = 0;
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                ret = -1;
            }
        }
        return (ret);
    }

    public void trigger() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public void main() {
        init();
        for (;;) {
            receive();
            sense_select_act();
        }
    }

    public void run() {
        main();
    }

    public Object read(String name, Object def) {
        return space.read(name, def);
    }

    public void write(String name, Object value) {
        space.write(name, value);
    }

}
