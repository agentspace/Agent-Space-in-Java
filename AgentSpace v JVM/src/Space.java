import java.util.*;
/**
*Class Space represents a singleton instance of our agent-space.
*Singleton pattern is a design pattern that restricts the instantiation of a 
*class to one object.
*In our case we use eager initialization meaning that the static initializer is 
*run when the class is initialized, because the program always needs an instance 
*of Space.
*/
public class Space {

    Map h = Collections.synchronizedMap(new TreeMap());

    private static Space singleton = new Space();

    // variables for representation of bloks and triggers
    HashMap<String, ArrayList<Object>> t;

    /**
     * @return Reference to our one instance of Space.
     */
    public static Space getInstance() {
        return singleton;
    }

    /**
     * Constructor of class Space. Initializes a new HashMap used to store
     * information which blocks trigger which agents. Keys are of type String
     * and represent the masks of blocks and values are of type ArrayList
     * representing one or more triggerable agents.
     */
    protected Space() {
        // initialization of the variables
        t = new HashMap<String, ArrayList<Object>>();
    }

    /**
     * Method used for writing to Space. Also triggers all agents attached to
     * the block which is being written into.
     *
     * @param name Mask of the block.
     * @param value Object which is written to the given block of our Space.
     */
    public void write(String name, Object value) {
        String key = name;
        Object item = value;
        h.put(key, item);
        if (t.containsKey(key)) {
            for (Object a : t.get(key)) {
                ((Triggerable) a).trigger();
            }
        }
        //System.out.println(h.get(key));
    }

    /**
     * Synchronized method used for reading from Space.
     *
     * @param name Mask of the block.
     * @return Information stored in a given block.
     */
    synchronized public Object read(String name) {
        String key = name;
        return h.get(key);
    }

    /**
     * Method used for reading from Space.
     *
     * @param name Result of a call of synchronized read. If is null, it means
     * that block with given mask doesn't exist and returns default value.
     * @param def Default value.
     * @return Either a default value or information stored in a given block.
     */
    public Object read(String name, Object def) {
        Object obj = read(name);
        if (obj == null) {
            return def;
        }
        return obj;
    }

    /**
     * Method used for attaching triggerable agents to blocks. Attaches an agent
     * to a block, so that it triggers when a given block is written into.
     *
     * @param mask Mask of the block.
     * @param agent Agent being attached to a block.
     */
    synchronized public void attachTrigger(String mask, Triggerable agent) {
        if (!t.containsKey(mask)) {
            ArrayList<Object> tmp = new ArrayList<Object>();
            tmp.add(agent);
            t.put(mask, tmp);
        } else {
            t.get(mask).add(agent);
        }

    }

}

