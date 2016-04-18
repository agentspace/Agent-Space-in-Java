package wrl.com.microstepmis.schd;

import java.lang.reflect.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */
public class SchdProcess implements Runnable {
	private Thread thread = null;
	private ThreadGroup grp = null;
	private Object instance = null;
	private Class newClass;
	private String[] args;
	private String taskName; // pod akym menom bol pusteny (nemusi byt unikatne)
	private String keyName; // pod akym menom bezi (unikatne)

	/**
	 * Creates a new SchdProcess object.
	 *
	 * @param processName DOCUMENT ME!
	 * @param className DOCUMENT ME!
	 * @param args DOCUMENT ME!
	 */
	public SchdProcess(String processName, Class newClass, String[] args) {

		taskName = processName;
		keyName = processName + "." + this.toString();

		// thread group s unikatnym menom jednoznacne vytvorenym pre dany proces
		grp = new ThreadGroup(keyName);
		this.newClass = newClass;
		this.args = args;

		// nastartujeme novy thread v novej thread group
		thread = new Thread(grp, this);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	/**
	 * DOCUMENT ME!
	 */
	public void run() {

		try {
			// vytiahnime z class-y metodu main( String[] args );
			Method mainMethod = newClass.getMethod("main", new Class[]{ String[].class });

			// metodu zavolame,
			// ako objekt udame NULL ked ide o staticku metodu
			// inak vytvorime objekt danej triedy
			// ako argumenty dame predpripravene jednoprvkove pole onsahujuce pole argumentov
			if (Modifier.isStatic(mainMethod.getModifiers())) {
				// pre staticku metodu nepotrebujeme instanciu
				instance = null;
			}
			else {
				newClass.getConstructor(new Class[]{});
				// pustame konstruktor dynamickeho objektu
				instance = newClass.newInstance();
			}
			// vytvorime pole argumentov pre metodu main
			// main ma jeden argument a to referenciu na pole stringov
			// pustime main
			mainMethod.invoke(instance, new Object[]{args});

			// vylistujeme si 2 aktivne thready v grupe (1 je tento thread, 2. moze byt iny)
			Thread[] gt = new Thread[2];
			for (; grp.enumerate(gt) > 1;) {
				// v thread grupe su este aspon 2 aktivne thready (t.j. este aspon 1 okrem daneho threadu)
				try {
					// pockame na skoncenie 1. aktivneho threadu
					if (gt[0] != Thread.currentThread()) {
						gt[0].join();
					} else {
						gt[1].join();
					}
				} catch (InterruptedException e) {
				}
			}
			// vsetky thready danej grupy dobehli okrem aktualneho threadu
		} catch (Exception e) {
			System.out.println("cannot launch "+newClass.toString()+" - "+e.toString());
		} finally {
			// odregistrujeme proces zo schedulera
			deregister();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the name, under which the task was launched
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the name, under which the task is stored in Scheduler map of processes
	 */
	public String getKeyName() {
		return keyName;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the name, under which the task is stored in Scheduler map of processes
	 */
	public ThreadGroup getThreadGroup() {
		return grp;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the name, under which the task is stored in Scheduler map of processes
	 */
	public Object getObject() {
		return instance;
	}

	public void deregister() {
	}

}
