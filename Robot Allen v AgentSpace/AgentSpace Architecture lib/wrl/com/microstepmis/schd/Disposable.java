package wrl.com.microstepmis.schd;

/**
 *	Objektom implementujucim rozhranie Disposable sa da povedat, aby skoncili a po sebe
 * 	poupratovali.
 *
 */ 

public interface Disposable {
	/**
	 *	Po zavolani tejto metody by mal objekt po sebe poupratovat.
	 */
  public void dispose();
}

