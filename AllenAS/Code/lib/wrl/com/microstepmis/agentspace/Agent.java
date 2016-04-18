package wrl.com.microstepmis.agentspace;

import wrl.com.microstepmis.schd.*;
import wrl.java.util.*;
import java.util.Date;

/**
 * Abstraktna trieda pre implementaciu agentov pre systemy zalozene na
 * Agent-Space architekture.
 *
 * @author $Andrej Lucny$
 * @version $Revision: 1.5 $ 
 * $Id: Agent.java,v 1.5 2005/02/01 10:18:21 matog Exp $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */
public abstract class Agent implements Disposable {
	private ProxyMonitor monitor;
	private Proxy proxy;
	protected Space space;
	protected Thread thread;
	protected long timestamp;
	protected boolean exited;

	/**
	 * Creates a new Agent object.
	 */
	public Agent() {
		monitor = new ProxyMonitor();
		proxy = null;
		space = SpaceFactory.getInstance();
		exited = false;
		thread = Thread.currentThread();
	}

	/**
	 * Zistenie threadu, v ktorom bezi mainLoop agenta.
	 *
	 * @return thread, v ktorom bezi mainLoop agenta
	 */
	public Thread getThread() {
		return thread;
	}

	/**
	 * Zistenie space-u, nad ktorym agent momentalne pracuje.
	 *
	 * @return space, nad ktorym agent momentalne pracuje
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * Nastavi pracovny space agenta na dany space (pozor, argument null je
	 * ignorovany  a k ziadnej zmene nedojde).  
	 * 
	 * Upozornenie: kombinacia public
	 * metody setSpace s nesynchronizovanymi  public metodami write, read,
	 * delete je inherentne nebezpecna, pokial  sa dane metody pouzivaju z
	 * roznych threadov. Viem si predstavit imaginarnu situaciu, kedy by malo
	 * vyznam agentovi menit  zvonka space za behu: pri prepinanie agenta medzi
	 * simulovanym prostredim a  realnym prostredim na beziacom systeme. MatoG
	 *
	 * @param space novy space
	 *
	 * @return space, nad ktorym agent predtym pracoval
	 */
	public Space setSpace(Space space) {
		Space oldSpace = this.space;

		
		this.space = space;

		return oldSpace;
	}

	/**
	 * Nastavi pracovny space agenta na space s danym menom a k ziadnej zmene
	 * nedojde).  Upozornenie: kombinacia public metody setSpace s
	 * nesynchronizovanymi  public metodami write, read, delete je inherentne
	 * nebezpecna, pokial  sa dane metody pouzivaju z roznych threadov. Viem si
	 * predstavit imaginarnu situaciu, kedy by malo vyznam agentovi menit
	 * zvonka space za behu: pri prepinanie agenta medzi simulovanym prostredim
	 * a  realnym prostredim na beziacom systeme. MatoG
	 *
	 * @param name meno noveho space-u
	 *
	 * @return space, nad ktorym agent predtym pracoval
	 */
	public Space setSpace(String name) {
		Space oldSpace = this.space;

		this.space = SpaceFactory.getInstance(name);

		return oldSpace;
	}

	/**
	 * Vytvorenie interneho proxy.
	 *
	 * @return vrati interne proxy
	 */
	private Proxy attachProxy() {
		proxy = new Proxy(thread, monitor);

		return proxy;
	}

	/**
	 * Vytvorenie timeru.
	 *
	 * @param delay cas cakania do 1. tiku
	 * @param period perioda
	 *
	 * @return proxy, ktore bude timerom triggerovane
	 */
	public Proxy attachTimer(long delay, long period) {
		Proxy proxy = attachProxy();

		new SchdTimer(proxy, delay, period);

		return proxy;
	}

	/**
	 * Vytvorenie timeru.
	 *
	 * @param date datum/cas 1. tiku
	 * @param period perioda
	 *
	 * @return proxy, ktore bude timerom triggerovane
	 */
	public Proxy attachTimer(Date date, long period) {
		Proxy proxy = attachProxy();

		new SchdTimer(proxy, date, period);

		return proxy;
	}

	/**
	 * Vytvorenie timeru.
	 *
	 * @param period perioda
	 *
	 * @return proxy, ktore bude timerom triggerovane
	 */
	public Proxy attachTimer(long period) {
		return attachTimer(period, period);
	}

	/**
	 * Vytvorenie triggeru daneho typu na bloky.
	 *
	 * @param name meno/maska blokov
	 * @param type typ triggeru
	 *
	 * @return proxy, ktore bude triggerovane
	 */
	public Proxy attachTrigger(String name, int type) {
		Proxy proxy = attachProxy();

		space.attachTrigger(name, proxy, type);

		return proxy;
	}

	/**
	 * Vytvorenie triggeru na bloky. Vytvori sa typ triggeru Trigger.NORMAL.
	 *
	 * @param name meno/maska blokov
	 *
	 * @return proxy, ktore bude triggerovane
	 */
	public Proxy attachTrigger(String name) {
		return attachTrigger(name, Trigger.NORMAL);
	}

	/**
	 * Inicializacia agenta (prazdna). Ocakava sa jej prekrytie, ale nie je
	 * nutne.
	 *
	 * @param args argumenty
	 */
	public void init(String[] args) {
		// to be overriden, nie je abstract aby to nebolo nutne prekryt
	}
	
	/**
	 * Kod, ktory vykonava agent pri zobudeni (prazdna metoda). Ocakava sa aspon
	 * 1 z metod senseSelectAct, alebo senseSelectAct(proxy).
	 */
	public void senseSelectAct() {
		// to be overriden, nie je abstract aby to nebolo nutne prekryt
	}

	/**
	 * Kod, ktory vykonava agent pri zobudeni (prazdna metoda). Ocakava sa aspon
	 * 1 z metod senseSelectAct, alebo senseSelectAct(proxy).
	 *
	 * @param proxy proxy, na ktore sa agent zobudil
	 */
	public void senseSelectAct(Proxy proxy) {
		// to be overriden, nie je abstract aby to nebolo nutne prekryt
	}

	private Proxy receive() {
		if (proxy == null) {
			return null;
		}

		Proxy pr = proxy.receive();

		timestamp = pr.getTimestamp();

		return pr;
	}

	/**
	 * Java/JVM nie je hard realtime, preto nie je garantovana latencia budenia.
	 * Preto je dobre mat moznost zistit, k akemu casu mal byt agent zobudeny
	 * (aktualny cas moze byt o dost vyssi).
	 *
	 * @return casovu znacku prisluchajucu okamihu zobudenia v milisekundach
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Hlavna funkcia agenta. Prekrytie sa ocakava len v specialnych pripadoch.
	 *
	 * @param args argumenty agenta
	 */
	public void mainLoop(String[] args) {
		// to be overriden, v specialnych pripadoch
		init(args);

		for (;;) {
			Proxy proxy = receive();

			if (proxy == null) {
				return;
			}

			senseSelectAct(proxy);
			senseSelectAct();

			if (exited) {
				return;
			}
		}
	}

	/**
	 * Hlavna funkcia agenta, spusti mainLoop. Metoda existuje preto, aby sa
	 * agent dal pustit ako SchdProcess.
	 *
	 * @param args argumenty agenta
	 */
	public void main(String[] args) {
		// tuto metodu spusta scheduler
		mainLoop(args);
	}

	/**
	 * Zavolanim funkcie dispose() sa dava zvonku agentovi najavo, ze ma ukoncit cinnost.
	 * Poznamka: exit() je vlastna agentovi, dispose je vlastna objektom implementujucim
	 * Disposable().
	 * 
	 * Pri najblizsej iteracii cyklu mainLoop agent skonci. Odporucany sposob
	 * ukoncovania cinnosti agenta (stopnutie threadu je neodporucane Sun-om).
	 */
	public void dispose() {
		exit();
	}


	/**
	 * Zavolanim funkcie exit() sa dava agentovi najavo, ze ma ukoncit cinnost.
	 * Pri najblizsej iteracii cyklu mainLoop agent skonci. Odporucany sposob
	 * ukoncovania cinnosti agenta (stopnutie threadu je neodporucane Sun-om).
	 */
	public void exit() {
		exited = true;
	}

	/**
	 * Zaspatie na urcitu dobu. Pozor,
	 *
	 * @param ms cas zaspania v milisekundach
	 */
	public void delay(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Zapis do prostredia (zvoleneho funkciou setSpace(), default
	 * Space.DEFAULT_SPACE_NAME)
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFrom zaciatok platnosti bloku
	 * @param validTo koniec platnosti bloku
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void write(String name, Object value, long validFrom, long validTo, float priority) {
		space.write(name, value, validFrom, validTo, priority);
	}

	/**
	 * Zapis do prostredia (zvoleneho funkciou setSpace(), default
	 * Space.DEFAULT_SPACE_NAME). Blok sa zapise s defaultnou prioritou
	 * (Block.DEFAULT_PRIORITY)
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFrom zaciatok platnosti bloku
	 * @param validTo koniec platnosti bloku
	 */
	public void write(String name, Object value, long validFrom, long validTo) {
		space.write(name, value, validFrom, validTo);
	}

	/**
	 * Zapis do prostredia (zvoleneho funkciou setSpace(), default
	 * Space.DEFAULT_SPACE_NAME).
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFor doba platnosti bloku (odteraz)
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void write(String name, Object value, long validFor, float priority) {
		space.write(name, value, validFor, priority);
	}

	/**
	 * Zapis do prostredia (zvoleneho funkciou setSpace(), default
	 * Space.DEFAULT_SPACE_NAME). Blok sa zapise s defaultnou prioritou
	 * (Block.DEFAULT_PRIORITY)
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFor doba platnosti bloku (odteraz)
	 */
	public void write(String name, Object value, long validFor) {
		space.write(name, value, validFor);
	}

	/**
	 * Zapis do prostredia (zvoleneho funkciou setSpace(), default
	 * Space.DEFAULT_SPACE_NAME).
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void write(String name, Object value, float priority) {
		space.write(name, value, priority);
	}

	/**
	 * Zapis do prostredia (zvoleneho funkciou setSpace(), default
	 * Space.DEFAULT_SPACE_NAME). Blok sa zapise s defaultnou prioritou
	 * (Block.DEFAULT_PRIORITY) a neobmedzenou platnostou.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 */
	public void write(String name, Object value) {
		space.write(name, value);
	}

	/**
	 * Precitanie bloku s danym menom z prostredia.
	 *
	 * @param name meno bloku
	 *
	 * @return hodnota bloku
	 */
	public Object read(String name) {
		return space.read(name);
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
	public Object read(String name, Object def) {
		return space.read(name, def);
	}

	/**
	 * Vycitavanie blokov, ktore sposobili triggernutie proxy
	 *
	 * @param proxy triggernuta proxy
	 *
	 * @return status 1. najdeneho bloku
	 */
	public BlockStatus readFirst(Proxy proxy) {
		return space.readFirst(proxy);
	}

	/**
	 * Vycitavanie blokov, ktore sposobili triggernutie proxy
	 *
	 * @param proxy triggernuta proxy
	 *
	 * @return status najdeneho bloku
	 */
	public BlockStatus readNext(Proxy proxy) {
		return space.readNext(proxy);
	}

	/**
	 * Vycitavanie blokov, ktore sposobili triggernutie internej proxy
	 *
	 * @return status 1. najdeneho bloku
	 */
	public BlockStatus readFirst() {
		return space.readFirst(proxy);
	}

	/**
	 * Vycitavanie blokov, ktore sposobili triggernutie internej proxy
	 *
	 * @return status 1. najdeneho bloku
	 */
	public BlockStatus readNext() {
		return space.readNext(proxy);
	}

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void delete(String name, float priority) {
		space.delete(name, priority);
	}

	/**
	 * Vymazanie bloku z prostredia (vymazanie s prioritou
	 * Block.DEFAULT_PRIORITY).
	 *
	 * @param name meno bloku
	 */
	public void delete(String name) {
		space.delete(name);
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
	public void delete(String name, long validFrom, long validTo, float priority) {
		space.delete(name, validFrom, validTo, priority);
	}

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param validFor doba platnosti vymazania
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void delete(String name, long validFor, float priority) {
		space.delete(name, validFor, priority);
	}
}
