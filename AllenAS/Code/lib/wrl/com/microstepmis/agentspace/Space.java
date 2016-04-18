package wrl.com.microstepmis.agentspace;

import wrl.com.microstepmis.schd.Proxy;

/**
 * Space - prostredie pre agentov.
 *
 * @author $Andrej Lucny$
 * @version $Revision: 1.4 $
 * $Id: Space.java,v 1.4 2005/01/21 10:21:41 matog Exp $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public interface Space {
	public static final String DEFAULT_SPACE_NAME = "DATA";
	

	/**
	 * Ziskanie mena space-u.
	 *
	 * @return meno space-u
	 */
	 
	public String getName();

	/**
	 * Vygenerovanie triggrov pre dany blok. Poodstranovanie triggrov, ktorych vlastnici neziju.
	 *
	 * @param block blok, ktoreho triggre sa generuju
	 */

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
	
	public void write (String name, Object value, long validFrom, long validTo, float priority);

	/**
	 * Zapis bloku do prostredia s prioritou Block.DEFAULT_PRIORITY.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFrom zaciatok platnosti bloku
	 * @param validTo koniec platnosti bloku
	 */

	public void write (String name, Object value, long validFrom, long validTo);

	/**
	 * Zapis bloku do prostredia.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFor doba platnosti bloku
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void write (String name, Object value, long validFor, float priority);

	/**
	 * Zapis bloku do prostredia s prioritou Block.DEFAULT_PRIORITY.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param validFor doba platnosti bloku
	 */
	public void write (String name, Object value, long validFor);

	/**
	 * Zapis bloku do prostredia.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 * @param priority priorita zapisu bloku (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void write (String name, Object value, float priority);

	/**
	 * Zapis bloku do prostredia s prioritou Block.DEFAULT_PRIORITY.
	 *
	 * @param name meno bloku
	 * @param value hodnota zapisovaneho bloku
	 */
	public void write (String name, Object value);

	/**
	 * Precitanie bloku s danym menom z prostredia.
	 *
	 * @param name meno bloku
	 *
	 * @return hodnota bloku
	 */
	 	
	public Object read (String name);

	/**
	 * Precitanie bloku s danym menom z prostredia. Ak taky blok neexistuje,
	 * vrati sa default.
	 *
	 * @param name meno bloku
	 * @param def default hodnota
	 *
	 * @return hodnotu bloku, ak taky v prostredi existuje, default inak
	 */
	public Object read (String name, Object def);

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
	public BlockStatus readFirst (Proxy proxy);

	/**
	 * Vycitavanie blokov, ktore sposobili triggernutie proxy
	 *
	 * @param proxy triggernuta proxy
	 *
	 * @return status najdeneho bloku
	 */
	 
	public BlockStatus readNext (Proxy proxy);

	/**
	 * Vytvorenie triggeru daneho typu na bloky.
	 *
	 * @param mask meno/maska blokov
	 * @param proxy triggerovane proxy
	 * @param type typ triggeru
	 *
	 * @return proxy, ktore bude triggerovane
	 */
	 
	void attachTrigger (String mask, Proxy proxy, int type);

	/**
	 * Odstranenie triggeru z prostredia.
	 *
	 * @param proxy proxy triggerovane triggerom
	 */
	public void detachTrigger (Proxy proxy);

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void delete (String name, float priority);

	/**
	 * Vymazanie bloku z prostredia (vymazanie s prioritou
	 * Block.DEFAULT_PRIORITY).
	 *
	 * @param name meno bloku
	 */
	 	
	public void delete (String name);

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param validFrom zaciatok platnosti vymazania
	 * @param validTo koniec platnosti vymazania
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */

	public void delete (String name, long validFrom, long validTo, float priority);

	/**
	 * Vymazanie bloku z prostredia.
	 *
	 * @param name meno bloku
	 * @param validFor doba platnosti vymazania
	 * @param priority priorita vymazavania (pouziva sa napr. pri modelovani
	 * 				subsumpcnej architektury)
	 */
	public void delete (String name, long validFor, float priority);

	/**
	 * Zistovanie, ci bezi main daneho space-u
	 *
	 * @return vracia nastaveny thread, v ktorom bezi main metoda daneho space-u
	 */

	public Thread getThread();

	/**
	 * "Zamykacia funkcia", aby sa main nedal pustit v 2 roznych threadoch.
	 * main nemoze byt synchronized, preto pri vstupe do main skusime objektu
	 * povedat, ze main bezi v nasom threade. Ak je interna 
	 *
	 * @return vracia nastaveny Thread (ak bol null, vrati Thread.currentThread(), inak
	 * vrati nastaveny thread).
	 */
	 
	public Thread setThread();

	/**
	 * "Odomykacia funkcia", v reali nepouzivana, lebo main je vecny
	 *
	 * @return vracia null, ak sa podarilo odomknut, inak vrati thread, ktorym je main zamknuty
	 */
	public Thread clearThread();

	/**
	 * Vypis space-u do stringu
	 *
	 * @return string s popisom space-u, blokov, triggerov
	 */

	public String toString ();
}
