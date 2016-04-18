package wrl.com.microstepmis.agentspace;

import wrl.java.util.Collection;
import wrl.java.util.ArrayList;
import wrl.java.util.Set;
import wrl.java.util.TreeSet;
import wrl.java.util.Iterator;
import wrl.com.microstepmis.schd.Proxy;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class Trigger {

	public static final int NORMAL	 = 0;		// trigger na konkretny blok
	public static final int MATCHING = 1;		// trigger na skupinu blokov vyhovujucich maske
	public static final int MULTIPLY = 2;		// trigger na skupinu blokov vyhovujucich maske s moznostou ziskania zmenenych blokov
	public static final int SORTED   = 3;		// trigger na skupinu blokov vyhovujucich maske s moznostou ziskania zmenenych blokov danych v poradi ako sa menili vratanie viacnasobnych hodnot
	public static final int QUIET    = 4;		// trigger na skupinu blokov vyhovujucich maske bez triggerovania proxy

	public Set masks;
	public Proxy proxy;
	public int type;
	public Set blocks;
	private Collection changed;
	private BlockStatus[] retset = new BlockStatus[]{};
	private int retofs = 0;

	public Trigger (String mask, Proxy proxy, int type) {
		this.masks = new TreeSet();
		masks.add(mask);
		this.proxy = proxy;
		this.type = type;
		blocks = new TreeSet(new BlockComparator());
		switch (type) {
			case Trigger.MULTIPLY: 
				changed = new TreeSet(new BlockStatusComparator());
				break;
			case Trigger.SORTED:
				changed = new ArrayList();
				break;
			default:
				changed = null;
		}
	}

	public void add (String mask, Proxy proxy, int type) {
		if (this.proxy == proxy && this.type == type)
			masks.add(mask);
	}

	public void addBlock (Block block) {
		blocks.add(block);
	}

	public boolean call (Block block) {
		switch (type) {
			case Trigger.MULTIPLY:
			case Trigger.SORTED:
				BlockStatus st = block.getStatus();
				if (type == Trigger.MULTIPLY) changed.remove(st);
				changed.add(st);
				break;
		}
		if (isQuiet()) return proxy.isTriggerable();
		else {
			if (block.isEmpty()) return proxy.trigger(block.validTo);
			else return proxy.trigger(block.validFrom);
		}
	}
	
	public boolean matches (String mask, String name) {
		// TO DO
		if (mask.equals(".*")) return true;
		else return mask.equals(name);
	}

	public boolean match (String name) {
		Iterator it = masks.iterator();
		while (it.hasNext()) {
			String mask = (String) it.next();
			if (matches(mask,name)) return true;
		}
		return false;
	}

	public void start () {
		switch (type) {
			case Trigger.NORMAL:
			case Trigger.MATCHING:
			case Trigger.QUIET:
				retset = new BlockStatus[blocks.size()];
				int i = 0;
				Iterator it = blocks.iterator();
				while (it.hasNext()) {
					Block block = (Block) it.next();
					retset[i++] = block.getStatus();
				}
				break;
			case Trigger.MULTIPLY:
				retset = (BlockStatus []) changed.toArray();
				changed = new TreeSet(new BlockStatusComparator());
				break;
			case Trigger.SORTED:
				retset = (BlockStatus []) changed.toArray();
				changed = new ArrayList();
				break;
		}
		retofs = 0;
	}

	public BlockStatus next () {
		if (retset.length == retofs) return null;
		return retset[retofs++];
	}
	
	public boolean hasMask () {
		switch (type) {
			case Trigger.MATCHING:
			case Trigger.MULTIPLY:
			case Trigger.SORTED:
			case Trigger.QUIET:
				return true;
		}
		return false;
	}

	public boolean isQuiet () {
		switch (type) {
			case Trigger.QUIET:
				return true;
		}
		return false;
	}

	public String toString() {
		String ret = "Trigger on ";
		Iterator it = masks.iterator();
		while (it.hasNext()) {
			String mask = (String) it.next();
			ret += mask + " ";
		}
		ret += "used by";
		it = blocks.iterator();
		while (it.hasNext()) {
			Block block = (Block) it.next();
			ret += " " + block.name;
		}
		return ret;
	}

}
