package wrl.com.microstepmis.agentspace;

import wrl.java.util.Map;
import wrl.java.util.TreeMap;
import wrl.java.util.HashMap;
import wrl.java.util.Set;
import wrl.java.util.HashSet;
import wrl.java.util.Iterator;
import wrl.java.util.StringComparator;
import wrl.com.microstepmis.schd.Proxy;

/**
 * Space - prostredie pre agentov.
 *
 * @author $Andrej Lucny$
 * @version $Revision: 1.1 $
 * $Id: LocalSpace.java,v 1.1 2005/01/21 10:21:41 matog Exp $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class LocalSpace implements Space{
	public static final String DEFAULT_SPACE_NAME = "DATA";
	
	protected Thread thread = null;			// thread, v ramci ktoreho bezi funkcia Space.main();
	protected String name;

	// reprezentacia blokov (pristup podla ich mena)
	private final Map blocks;

	// vyprchavanie platnosti (bloky utriedene podla casu)
	private final ValidityQueue validq;

	// triggery pouzivajuce masku s .* (regex)
	// tu si drzime vsetky masky s .* aby sme vedeli pri zriadeni noveho bloku
	// zariadit aby bol triggerovany aj uz existujucimi triggrami
	private final Set matchingTriggers;

	// vsetky triggery
	private final Map triggers;

	/**
	 * Vytvorenie noveho space objektu. Vola sa z getInstance()
	 */
	protected LocalSpace( String name ) {
		thread = null;
		this.name = name;
		blocks = new TreeMap(new StringComparator());
		validq = new ValidityQueue();
		matchingTriggers = new HashSet();
		triggers = new HashMap();
	}

	/**
	 * Ziskanie mena space-u.
	 *
	 * @return meno space-u
	 */
	 
	public String getName() {
		return name;
	}

	/**
	 * Vygenerovanie triggrov pre dany blok. Poodstranovanie triggrov, ktorych vlastnici neziju.
	 *
	 * @param block blok, ktoreho triggre sa generuju
	 */

	private void generateTriggers (Block block) {
		// triggerneme vsetky potrebne proxy
		// ak pritom zistime, ze vlastnik proxy uz nezije, odstranime tieto triggre
		Iterator it = block.fixedTriggers.iterator();
		while (it.hasNext()) {
			Trigger trigger = (Trigger) it.next();
			if (!trigger.call(block))
				detachTrigger(trigger.proxy);
		}
		it = block.matchingTriggers.iterator();
		while (it.hasNext()) {
			Trigger trigger = (Trigger) it.next();
			if (!trigger.call(block))
				detachTrigger(trigger.proxy);
		}
	}

	/**
	 * Zapis bloku do prostredia.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFrom zaciatok platnosti bloku
	 * @param validTo koniec platnosti bloku
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	
	synchronized public void write (String name, Object value, long validFrom, long validTo, float priority) {
		// realizujeme defaulty
		if (validFrom == 0) validFrom = System.currentTimeMillis();
		// overime ci uz taky blok mame vytvoreny
		Block block = (Block) blocks.get(name);
		if (block == null) {
			// blok je novy, zavedieme ho
			block = new Block(name,value,validFrom,validTo,priority);
			blocks.put(name,block);
			// pokial je meno noveho bloku matchovane nejakym uz existujucim matchujucim triggerom, pridame ho
			Iterator it = matchingTriggers.iterator();
			while (it.hasNext()) {
				Trigger trigger = (Trigger) it.next();
				if (trigger.match(block.name)) {
					trigger.addBlock(block);
					block.addTrigger(trigger);
				}
			}
		}
		else {
			// blok uz existuje, zmenime ho ale iba ak na to mame prioritu
			if (priority < block.priority) return;
			block.setValue(value,validFrom,validTo,priority);
		}
		// blok sa zmenil vygenerujeme triggre
		if (block.isActive()) generateTriggers(block);
		// upravime vyprchavanie platnosti
		if (block.isFinite()) validq.add(block);
		else validq.remove(block);
	}

	/**
	 * Zapis bloku do prostredia s prioritou Block.DEFAULT_PRIORITY.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFrom zaciatok platnosti bloku
	 * @param validTo koniec platnosti bloku
	 */

	public void write (String name, Object value, long validFrom, long validTo) {
		write(name,value,validFrom,validTo,Block.DEFAULT_PRIORITY);
	}

	/**
	 * Zapis bloku do prostredia.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFor doba platnosti bloku
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void write (String name, Object value, long validFor, float priority) {
		long now = System.currentTimeMillis();
		write(name,value,now,now+validFor,priority);
	}

	/**
	 * Zapis bloku do prostredia s prioritou Block.DEFAULT_PRIORITY.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFor doba platnosti bloku
	 */
	public void write (String name, Object value, long validFor) {
		write(name,value,validFor,Block.DEFAULT_PRIORITY);
	}

	/**
	 * Zapis bloku do prostredia.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void write (String name, Object value, float priority) {
		long now = System.currentTimeMillis();
		write(name,value,now,Block.FOREVER,priority);
	}

	/**
	 * Zapis bloku do prostredia s prioritou Block.DEFAULT_PRIORITY.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 */
	public void write (String name, Object value) {
		write(name,value,Block.DEFAULT_PRIORITY);
	}
	
	/**
	 * Uplne odstranenie bloku.
	 *
	 * @param block odstranovany blok
	 */
	private void liquidate (Block block) {
		// uplne odstranenie bloku
		// jednak z MATCHING a MULTIPLY triggrov
		// (samotne triggery musime ponechat lebo tie zanikaju az s agentom, ktory ich
		// registruje - aj keby boli NORMAL a teda na jeden blok - stale ich mozu opat zapisat
		Iterator it = block.matchingTriggers.iterator();
		while (it.hasNext()) {
			Trigger trigger = (Trigger) it.next();
			trigger.blocks.remove(block);
		}
		// zrusime blok z reprezentacie blokov
		blocks.remove(block.name);
		// odstranime ho aj z vyprchavania validity, uz ziadnu nema (nemusi tam vobec byt, samozrejme)
		validq.remove(block);
	}

	/**
	 * Akcie pri expirovani bloku (vygenerovanie triggerov, vymazanie bloku a pod.).
	 *
	 * @param block blok (pozor, volaju sa metody bloku a teda interny stav bloku sa meni)
	 *
	 * @return true, ak blok uz nebol platny (a vykonaju sa akcie), false ak este plati (ziadne akcie)
	 */


	private void preliquidate (Block block) {
		// obsah bloku vyhlasime za prazdny
		// (musime tak urobit aj v pripade ze ideme nasledne blok zlikvidovat kvoli MULTIPLY triggrom
		// - samotny bol totiz moze prezit likvidaciu v zozname zmien asociovanych s nejakym MULTIPLY triggerom
		// za ucelom prevzatia tejto zmeny zo strany nejakeho agenta, ktora sa vykona uz po "odstraneni"
		// - v takom pripade musime agentovi vratit prazdny blok, nie poslednu hodnotu)
		block.clear();
		// odhadneme ci je vhodne blok uplne odstranit
		if (block.fixedTriggers.size() == 0) {
			// blok uplne odstranime, lebo tento uz mozno nikdy nebude zapisany
			liquidate(block);
		}
		else {
			// hoci tento blok uz nema hodnotu, vieme, ze potencialne bude opat zapisana
			// preto si ho nechame v reprezentacii
			// odstranime ho len z vyprchavania validity, lebo uz ziadnu nema
			validq.remove(block);
		}
	}

	/**
	 * Akcie pri expirovani bloku (vygenerovanie triggerov, vymazanie bloku a pod.).
	 *
	 * @param block blok (pozor, volaju sa metody bloku a teda interny stav bloku sa meni)
	 *
	 * @return true, ak blok uz nebol platny (a vykonaju sa akcie), false ak este plati (ziadne akcie)
	 */
	
	private boolean validityExpired (Block block) {
		// overime ci blok este plati
		if (block.isValid()) return false;
		// kvoli platnosti vymazania s definovanou prioritou sa moze stat ze ideme rusit prazdny blok
		// v takom pripade negenerujeme trigger
		boolean alreadyEmpty = block.isEmpty();
		// blok prestal platit, zlikvidujeme hodnotu (ale ponechame udaje o platnosti potrebne pre vygenerovanie triggra)
		block.partialClear();
		// vygenerujeme trigger
		if (!alreadyEmpty) generateTriggers(block);
		// odstranime, ciastocne alebo uplne
		preliquidate(block); 
		// tu sa nemusime bat o MULTIPLY triggre - blok prezije v 
		return true;
	}

	/**
	 * Akcie pri vstupeni bloku do platnosti (vygenerovanie triggerov a pod.).
	 *
	 * @param block blok
	 *
	 * @return true, ak blok nebol platny (a teraz je a generuju sa triggre), false, ak uz platil (negeneruju sa triggre)
	 */
	 
	private boolean validityStarted (Block block) {
		// overime ci blok uz plati
		if (!block.isValid()) return false;
		// vygenerujeme trigger
		generateTriggers(block);
		return true;
	}

	/**
	 * Precitanie bloku s danym menom z prostredia.
	 *
	 * @param name meno bloku
	 *
	 * @return hodnota bloku
	 */
	 	
	synchronized public Object read (String name) {
		Block block = (Block) blocks.get(name);
		// existuje ?
		if (block == null) return null;
		// neni prazdny ?
		if (block.isEmpty()) return null;
		// nezacal este platit ?
		if (!block.isActive()) return null;
		// nevyprsala mu uz validita ?
		// (toto je to tu jednak aby sme si boli isti, 
		// jednak aby sa space spraval ako-tak koretkne aj keby nebezal jeho main
		if (validityExpired(block)) return null;
		// vratime hodnotu
		return block.value;
	}

	/**
	 * Precitanie bloku s danym menom z prostredia. Ak taky blok neexistuje,
	 * vrati sa default.
	 *
	 * @param name meno bloku
	 * @param def default hodnota
	 *
	 * @return hodnotu bloku, ak taky v prostredi existuje, default inak
	 */
	public Object read (String name, Object def) {
		Object obj = read(name);
		// ak sa neda precitat vratime agentom zvoleny default
		if (obj == null) return def;
		return obj;
	}

	/**
	 * Vycitavanie blokov, ktore sposobili triggernutie proxy
	 * funguje pre vsetky, ale zmysel ma len pre 
	 * MATCHING triggre - vracia vsetky bloky, ktore zodpovedaju maske
	 * MULTIPLY triggre - vracia vsetky neprevzate zmenene bloky, ktore zodpovedaju maske
	 *
	 * @param proxy triggernuta proxy
	 *
	 * @return status 1. najdeneho bloku
	 */
	synchronized public BlockStatus readFirst (Proxy proxy) {
		if (proxy == null) return null;
		Trigger trigger = (Trigger) triggers.get(proxy);
		if (trigger == null) return null;
		trigger.start();
		return readNext(proxy);
	}

	/**
	 * Vycitavanie blokov, ktore sposobili triggernutie proxy
	 *
	 * @param proxy triggernuta proxy
	 *
	 * @return status najdeneho bloku
	 */
	 
	synchronized public BlockStatus readNext (Proxy proxy) {
		if (proxy == null) return null;
		Trigger trigger = (Trigger) triggers.get(proxy);
		BlockStatus st = trigger.next();
		return st;
	}

	/**
	 * Vytvorenie triggeru daneho typu na bloky.
	 *
	 * @param mask meno/maska blokov
	 * @param proxy triggerovane proxy
	 * @param type typ triggeru
	 *
	 * @return proxy, ktore bude triggerovane
	 */
	 
	synchronized public void attachTrigger (String mask, Proxy proxy, int type) {
		// overime ci uz nie je zavedeny
		Trigger trigger = (Trigger) triggers.get(proxy);
		if (trigger == null) {
			// ak je novy zavedieme
			trigger = new Trigger(mask,proxy,type);
			triggers.put(proxy,trigger);
		}
		else {
			// ak uz taky existuje, pridame mu dalsiu masku (trigerujucu to iste proxy)
			trigger.add(mask,proxy,type);
		}
		if (trigger.hasMask()) {
			// trigger pouzivajuci masku, ktory ale nevie povedat, ktore bloky sa zmenili
			// trigger pouzivajuci masku, ktory si aj pamata zmenene bloky
			// v oboch pripadoch budeme v buducnosti kontrolovat kazdy novy blok ci nespada pod dany trigger
			matchingTriggers.add(trigger);
			// a musime prebehnut vsetky existujuce bloky ci nespadaju pod dany trigger
			Iterator it = blocks.values().iterator();
			while (it.hasNext()) {
				Block block2 = (Block) it.next();
				if (trigger.match(block2.name)) {
					// ak ano, povieme triggeru, ktory blok trigeruje
					trigger.addBlock(block2);
					// aj bloku povieme, ktory trigger ho triggeruje
					block2.addTrigger(trigger);
				}
			}
		}
		else {
			// normalne maska zodpoveda jedinemu bloku
			// zistime ci ho uz mame zavedeny
			Block block1 = (Block) blocks.get(mask);
			if (block1 == null) {
				// ak je to novy blok, vytvorime ho ako prazdny a zavedieme ho
				block1 = new Block(mask);
				blocks.put(mask,block1);
			}
			// povieme triggeru, ktory blok trigeruje
			trigger.addBlock(block1);
			// ako aj bloku, ktory trigger ho triggeruje
			block1.addTrigger(trigger);
		}
	}

	/**
	 * Odstranenie triggeru z prostredia.
	 *
	 * @param proxy proxy triggerovane triggerom
	 */
	synchronized public void detachTrigger (Proxy proxy) {
		Trigger trigger = (Trigger) triggers.get(proxy);
		if (trigger != null) {
			// odstranime ho zo vsetkych blokov, ktore triggeroval
			Iterator it = trigger.blocks.iterator();
			while (it.hasNext()) {
				Block block = (Block) it.next();
				block.removeTrigger(trigger);
				// ak bol tento trigger posledny z pevnych, nasledkom jeho zaniku
				// treba zlikvidovat cely blok ak uz bol ponechany v reprezentacii iba kvoli tomuto triggru
				if (block.fixedTriggers.size() == 0)
					if (block.isEmpty() && !block.isValid())
						liquidate(block);
			}
			// odstranime ho z kontroly novych blokov (pre pripad ze bol MATCHING alebo MULTIPLY
			matchingTriggers.remove(trigger);
			// odstranime ho zo zoznamu triggerov
			triggers.remove(proxy);
		}
	}

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	synchronized public void delete (String name, float priority) {
		// overime ci je taky blok vobec vytvoreny
		Block block = (Block) blocks.get(name);
		if (block == null) return;
		// mame na odmazanie prioritu ?
		if (priority < block.priority) return;
		// blok sa ma odmazat, zlikvidujeme hodnotu (ale nasimulujeme udaje o platnosti potrebne pre vygenerovanie triggra)
		block.partialClear(System.currentTimeMillis());
		// vygenerujeme trigger
		generateTriggers(block);
		// odstranime, ciastocne alebo uplne
		preliquidate(block);
	}

	/**
	 * Vymazanie bloku z prostredia (vymazanie s prioritou
	 * Block.DEFAULT_PRIORITY).
	 *
	 * @param name meno bloku
	 */
	 	
	public void delete (String name) {
		delete(name,Block.DEFAULT_PRIORITY);
	}

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param validFrom zaciatok platnosti vymazania
	 * @param validTo koniec platnosti vymazania
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */

	synchronized public void delete (String name, long validFrom, long validTo, float priority) {
		// ma vyznam len pre validFrom == Block.NOW resp. 0 
		// pre zhodu s ostatnymi prototypmi, kde jediny long urcuje relativnu platnost, ponechavame tento tvar
		// overime ci je taky blok vobec vytvoreny
		Block block = (Block) blocks.get(name);
		if (block == null) {
			// ak je priorita najnizsia, staci neurobit nic
			if (priority == Block.DEFAULT_PRIORITY) return;
			// napriek tomu, ze ideme blok vymazavat, musime ho vytvorit;
			// musime totiz v case zabranit jeho prepisaniu tym co maju nizsiu prioritu
			block = new Block(name,Block.EMPTY,validFrom,validTo,priority);
			blocks.put(name,block);
			// ziadny trigger netreba
		}
		else {
			// mame na odmazanie prioritu ?
			if (priority < block.priority) return;
			// ak je priorita defaultna, nema zmysel blok nechavat
			if (priority == Block.DEFAULT_PRIORITY) {
				delete(name,priority);
				return;
			}
			// blok sa ma odmazat, zlikvidujeme hodnotu (ale nasimulujeme udaje o platnosti potrebne pre vygenerovanie triggra)
			block.partialClear(System.currentTimeMillis()); 
			// vygenerujeme trigger
			generateTriggers(block);
			// a tu nastavime skutocny platnost aby sa stratil az po jej vyprsani
			block.partialClear(validFrom, validTo, priority); 
		}
		// block neodstranime, ale ponechame 
		validq.add(block);
	}

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param validFor doba platnosti vymazania
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void delete (String name, long validFor, float priority) {
		delete(name,Block.NOW,System.currentTimeMillis()+validFor,priority);
	}

	/**
	 * Zistovanie, ci bezi main daneho space-u
	 *
	 * @return vracia nastaveny thread, v ktorom bezi main metoda daneho space-u
	 */

	public synchronized Thread getThread() {
		return thread;
	}

	/**
	 * "Zamykacia funkcia", aby sa main nedal pustit v 2 roznych threadoch.
	 * main nemoze byt synchronized, preto pri vstupe do main skusime objektu
	 * povedat, ze main bezi v nasom threade. Ak je interna 
	 *
	 * @return vracia nastaveny Thread (ak bol null, vrati Thread.currentThread(), inak
	 * vrati nastaveny thread).
	 */
	 
	public synchronized Thread setThread() {
		if( thread == null ) 
			thread = Thread.currentThread();
		return thread;
	}

	/**
	 * "Odomykacia funkcia", v reali nepouzivana, lebo main je vecny
	 *
	 * @return vracia null, ak sa podarilo odomknut, inak vrati thread, ktorym je main zamknuty
	 */
	public synchronized Thread clearThread() {
		if( thread == Thread.currentThread() ) 
			thread = null;
		return thread;
	}

	/**
	 * Vypis space-u do stringu
	 *
	 * @return string s popisom space-u, blokov, triggerov
	 */

	synchronized public String toString () {
		String ret = "Space " + name;
		if( thread != null ) ret = ret + "(main method running):";
		else ret = ret + "(main method not running):";
		ret += blocks.size() + " blocks\n";
		Iterator it = blocks.values().iterator();
		while (it.hasNext()) {
			Block block = (Block) it.next();
			ret += block + "\n";
		}
		ret += triggers.size() + " triggers\n";
		it = triggers.values().iterator();
		while (it.hasNext()) {
			Trigger trigger = (Trigger) it.next();
			ret += trigger + "\n";
		}
		ret += validq;
		return ret;
	}

	/**
	 * main metoda- nemusi sa pustit, ale potom:
	 * - nebude fungovat triggrovanieodstranenia bloku zo space
	 *
	 * @param args argumenty (zatial ignorovane)
	 */
	public void main (String[] args) {
		// nastavime objektu pole thread, ak sa nam to nepodari
		if( setThread() != Thread.currentThread() ) {
			System.out.println( "Cannot start method main, method main already running" );
			return;
		}
		// aktivujeme validity queue
		Proxy proxy = validq.activate();
		for (;;) {
			proxy.receive();
			long now = System.currentTimeMillis();
			for (;;) {
				// vyberieme nastavajucu udalost a odstranime ju z queue
				Block block = validq.pop(now);
				// ak nas ziadna udalost necaka, ideme spat
				if (block == null) break;
				// ak treba, vyhodime dany blok 
				// (udalost moze vzniknut aj vyprsanim aj zacatim platnosti)
				if (!validityExpired(block)) validityStarted(block);
			}
		}
		// toto nikdy nenastane, ale uvadzam, nech sa na to pamata
		// clearThread();
	}
	
}
