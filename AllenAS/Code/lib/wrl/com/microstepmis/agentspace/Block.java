package wrl.com.microstepmis.agentspace;

import wrl.java.util.Set;
import wrl.java.util.HashSet;
import wrl.java.util.Iterator;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.3 $
 * $Id: Block.java,v 1.3 2005/01/21 10:21:41 matog Exp $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class Block {

	public static final long FOREVER = -1;
	public static final long NOW = 0;
	public static final Object EMPTY = null;
	public static final float DEFAULT_PRIORITY = 0.0f;

	public String name;
	public Object value;
	public long validFrom;
  public long validTo;
  public float priority;

	public Set fixedTriggers;
	public Set matchingTriggers;

	public Block (String name, Object value, long validFrom, long validTo, float priority) {
		this.name = name;
		this.value = value;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.priority = priority;
		fixedTriggers = new HashSet();
		matchingTriggers = new HashSet();
	}

	public Block (String name) {
		this(name,EMPTY,NOW,FOREVER,DEFAULT_PRIORITY);
	}

	public boolean isValid (long tim) {
		if (validTo == FOREVER) return true;
		if (tim < validTo) return true;
		return false;
	}

	public boolean isValid() {
		return isValid(System.currentTimeMillis());
	}

	public boolean isActive (long tim) {
		if (tim < validFrom) return false;
		return true;
	}

	public boolean isActive() {
		return isActive(System.currentTimeMillis());
	}

	public boolean isFinite() {
		return (validTo != FOREVER || validFrom <= System.currentTimeMillis());
	}

	public boolean isEmpty() {
		return (value == EMPTY);
	}

	public void setValue (Object value, long validFrom, long validTo, float priority) {
		this.value = value;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.priority = priority;
	}

	public void partialClear () {
		value = EMPTY;
	}

	public void partialClear (long validTo) {
		value = EMPTY;
		this.validTo = validTo;
	}

	public void partialClear (long validFrom, long validTo, float priority) {
		value = EMPTY;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.priority = priority;
	}
	
	public void clear () {
		setValue(EMPTY,NOW,FOREVER,DEFAULT_PRIORITY);
	}

	public void addTrigger (Trigger trigger) {
		if (trigger.hasMask()) matchingTriggers.add(trigger);
		else fixedTriggers.add(trigger);
	}

	public void removeTrigger (Trigger trigger) {
		if (trigger.hasMask()) matchingTriggers.remove(trigger);
		else fixedTriggers.remove(trigger);
	}

	BlockStatus getStatus () {
		return new BlockStatus(name,value,validFrom,validTo);
	}

	public String toString() {
		String ret = "Block " + name + " triggered by";
		Iterator it = fixedTriggers.iterator();
		while (it.hasNext()) {
			Trigger trigger = (Trigger) it.next();
			ret += " " + trigger.masks;
		}
		it = matchingTriggers.iterator();
		while (it.hasNext()) {
			Trigger trigger = (Trigger) it.next();
			ret += " " + trigger.masks;
		}
		return ret;
	}

	public int compareTo (Object obj) {
		Block block = (Block) obj;
		return name.compareTo(block.name);
	}

}
