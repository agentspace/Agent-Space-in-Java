package wrl.com.microstepmis.agentspace;

import java.util.Date;

import wrl.java.util.SortedSet;
import wrl.java.util.TreeSet;
import wrl.java.util.Iterator;
import wrl.java.util.NoSuchElementException;
import wrl.com.microstepmis.schd.Proxy;
import wrl.com.microstepmis.schd.SchdTimer;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class ValidityQueue {

	// Pre pripad pouzitia Space bez kontoly validity, musi sa tato explicitne zapnut
	private boolean active = false;
	
	private SchdTimer timer;
	
	// vyprchavanie platnosti (bloky utriedene podla casu)
	private final SortedSet validsto;
	
	// zacinanie platnosti
	private final SortedSet validsfrom;
	
	// najblizsia udalost
	private long min = Block.FOREVER;

	public ValidityQueue () {
		validsto = new TreeSet(new ValidityToComparator());
		validsfrom = new TreeSet(new ValidityFromComparator());
	}
	
	public Proxy activate () {
		active = true;
		Proxy proxy = new Proxy();
		timer = new SchdTimer(proxy);
		return proxy;
	}
	
	private void reSchedule () {
		long min = Block.FOREVER;
		try {
			Block block = (Block) validsto.first();
			if (block.validTo != Block.FOREVER)
				if (min == Block.FOREVER || block.validTo < min) 
					min = block.validTo;
		} catch (NoSuchElementException e) {
		}
		try {
			Block block = (Block) validsfrom.first();
			if (block.validFrom != Block.FOREVER)
				if (min == Block.FOREVER || block.validFrom < min) 
					min = block.validFrom;
		} catch (NoSuchElementException e) {
		}
		if (min == Block.FOREVER) {
			if (this.min != Block.FOREVER) {
				timer.cancel();
				this.min = Block.FOREVER;
			}
		}
		else {
			if (this.min == Block.FOREVER || min != this.min) {
				timer.adjust(new Date(min));
				this.min = min;
			}
		}
	}
	
	public void add (Block block) {
		if (!active) return;
		if (block.validTo != Block.FOREVER) validsto.add(block);
		else validsto.remove(block);
		if (block.validFrom > System.currentTimeMillis()) validsfrom.add(block);
		else validsfrom.remove(block);
		reSchedule();
//		System.out.println("add "+this);
	}
	
	public void remove (Block block) {	
		if (!active) return;
		validsto.remove(block);
		validsfrom.remove(block);
		reSchedule();
//		System.out.println("remove "+this);
	}
	
	public Block pop (long tim) {
		if (!active) return null;
		for (;;) {
			Block firstto = null;
			try {
				firstto = (Block) validsto.first();
			} catch (NoSuchElementException e) {
			}
			Block firstfrom = null;
			try {
				firstfrom = (Block) validsfrom.first();
			} catch (NoSuchElementException e) {
			}
			// ak narazime na blocky s neohranicenou platnsotu, likvidujeme ich
			// (toto by sa nikdy nemalo vyskytnut, je tu len ako zabezpeka vratenia korektnej hodnoty)
			if (firstto != null)
				if (firstto.validTo == Block.FOREVER)
					validsto.remove(firstto);
			if (firstfrom != null)
				if (firstfrom.validFrom == Block.FOREVER)
					validsfrom.remove(firstfrom);
			// ak nemame skorsi ako dany cas, je to ako keby sme nemali nic
			if (firstto != null)
				if (firstto.validTo > tim)
					firstto = null;
			if (firstfrom != null)
				if (firstfrom.validFrom > tim) {
					firstfrom = null;
				}
			// vratime naskorsiu udalost			
			if (firstto == null) {
				if (firstfrom == null) return null;
				else {
					validsfrom.remove(firstfrom);
					reSchedule();
//					System.out.println("pop "+this);
					return(firstfrom);
				}
			}
			else {
				if (firstfrom == null) {
					validsto.remove(firstto);
					reSchedule();
//					System.out.println("pop "+this);
					return(firstto);
				}
				else {
					// mame oba
					if (firstto.validTo < firstfrom.validFrom) {
						validsto.remove(firstto);
						reSchedule();
//						System.out.println("pop "+this);
						return(firstto);
					}
					else {
						validsfrom.remove(firstfrom);
						reSchedule();
//						System.out.println("pop "+this);
						return(firstfrom);
					}
				}
			}
		}
	}
	
	synchronized public String toString () {
		String ret = "";
		if (active) {
			ret += validsto.size() + " validity expiration entries\n";
			int ord = 0;
			Iterator it = validsto.iterator();
			while (it.hasNext()) {
				Block block = (Block) it.next();
				ret += (++ord) + ". " + block.name + " " + new Date(block.validTo) + "\n";
			}
			ret += validsfrom.size() + " validity waiting entries\n";
			ord = 0;
			it = validsfrom.iterator();
			while (it.hasNext()) {
				Block block = (Block) it.next();
				ret += (++ord) + ". " + block.name + " " + new Date(block.validFrom) + "\n";
			}
		}
		return ret;
	}

}
