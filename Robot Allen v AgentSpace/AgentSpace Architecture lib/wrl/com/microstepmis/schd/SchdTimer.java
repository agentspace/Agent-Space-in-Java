package wrl.com.microstepmis.schd;

import java.util.Date;

import wrl.java.util.Timer;
import wrl.java.util.TimerTask;

/**
 * Rozsireny timer pouzivany pri praci s Proxy.
 *
 * @author $Auth: Andrej Lucny$
 *         $Id: SchdTimer.java,v 1.2 2005/02/01 14:34:57 matog Exp $
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */
public class SchdTimer implements Disposable {
	final Timer timer = new Timer();
	final Proxy proxy;
	TimerTask task = null;

	/**
	 * Vytvori novy SchdTimer objekt (periodicky).
	 *
	 * @param proxy proxy, ktoru timer triggeruje
	 * @param delay cas do 1. triggeru v milisekundach
	 * @param period perioda triggerovania v milisekundach
	 */
	public SchdTimer(Proxy proxy, long delay, long period) {
		this(proxy);
		set(delay, period);
	}

	/**
	 * Vytvori novy SchdTimer objekt (periodicky).
	 *
	 * @param proxy proxy, ktoru timer triggeruje
	 * @param date datum 1. triggeru
	 * @param period perioda triggerovania v milisekundach
	 */
	public SchdTimer(Proxy proxy, Date date, long period) {
		this(proxy);
		set(date, period);
	}

	/**
	 * Vytvori novy SchdTimer objekt (jednorazovy).
	 *
	 * @param proxy proxy, ktoru timer triggeruje
	 * @param delay cas do 1. triggeru v milisekundach
	 */
	public SchdTimer(Proxy proxy, long delay) {
		this(proxy, delay, 0);
	}

	/**
	 * Vytvori novy SchdTimer objekt (bez nastavenia casovania).
	 *
	 * @param proxy proxy, ktoru timer triggeruje
	 */
	public SchdTimer(Proxy proxy) {
		this.proxy = proxy;

	}

	private TimerTask createTask(final boolean periodic) {
		return new TimerTask() {
				public void run() {
					proxy.trigger(scheduledExecutionTime());

					//				if (!periodic) cancel();
				}
			};
	}

	/**
	 * Zmena casovania timeru. Stare nastavenie casovania je zabudnute.
	 *
	 * @param delay cas do 1. triggeru v milisekundach
	 * @param period perioda triggerovania v milisekundach
	 */
	public void adjust(long delay, long period) {
		cancel();
		set(delay, period);
	}

	/**
	 * Zmena casovania timeru. Stare nastavenie casovania je zabudnute.
	 *
	 * @param date datum 1. triggeru
	 * @param period perioda triggerovania v milisekundach
	 */
	public void adjust(Date date, long period) {
		cancel();
		set(date, period);
	}

	/**
	 * Zmena casovania timeru. Stare nastavenie casovania je zabudnute.
	 * Timer bude spusteny len 1 krat.
	 *
	 * @param delay cas do jedineho spustenia.
	 */
	public void adjust(long delay) {
		adjust(delay, 0);
	}

	/**
	 * Zmena casovania timeru. Stare nastavenie casovania je zabudnute.
	 * Timer bude spusteny len 1 krat.
	 *
	 * @param date datum jedineho spustenia.
	 */
	public void adjust(Date date) {
		adjust(date, 0);
	}

	/**
	 * Zrusenie casovaneho tasku. Vola (TimerTask) task.cancel(). Nevola timer.cancel().
	 */
	public void cancel() {
		if (task != null) {
			task.cancel();
		}

		//		timer.purge();
	}

	/**
	 * Funkcia z rozhrania Disposable.
	 * Zrusi timer (task.cancel(), timer.cancel();
	 */
	public void dispose() {
		if (task != null) {
			task.cancel(); // task.cancel nestacil - ostavali visiet timery
		}

		timer.cancel(); // musi sa zavolat aj timer.cancel (zaujimave spravanie) MatoG 4.1.2005

		//		timer.purge();
	}

	/**
	 * Ziskanie casovej znacky prisluchajucej tiku timeru.
	 * Java nie je hard-realtime, cize z kodu zavolaneho timerom nemame sancu zistit,
	 * ktoremu tiku timeru volanie prislucha (oneskorenie volania od pozadovaneho okamihu
	 * Java negarantuje a moze byt aj velmi velke).
	 *
	 * @return casovu znacku v milisekundach od 1.1.1970 GMT
	 */
	public long getTimestamp() {
		return task.scheduledExecutionTime();
	}

	private void set(long delay, long period) {
		if (period == 0) {
			task = createTask(false);
			timer.schedule(task, delay);
		} else {
			task = createTask(true);
			timer.scheduleAtFixedRate(task, delay, period);
		}
	}

	private void set(Date date, long period) {
		if (period == 0) {
			task = createTask(false);
			timer.schedule(task, date);
		} else {
			task = createTask(true);
			timer.scheduleAtFixedRate(task, date, period);
		}
	}
}
