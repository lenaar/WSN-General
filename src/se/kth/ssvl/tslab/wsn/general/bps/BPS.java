package se.kth.ssvl.tslab.wsn.general.bps;

public class BPS {
	
	/* Member variables */
	private static BPS instance;
	
	private BPSService service; 
	
	
	/* Initialization and constructor */
	
	/**
	 * This method will return an singleton instance of BPS, 
	 * which is used as a main entry point to the library. Note: Must be called after init() method
	 * @return The BPS singleton
	 * @throws BPSException Throws and exception if BPS has not been initialized using init() method
	 */
	public BPS getInstance() throws BPSException {
		if (instance == null) {
			if (service == null) {
				throw new BPSException("BPS was not initialized with a BPSService before being used");
			}
			instance = new BPS(service);
		}
		
		return instance;
	}
	
	
	/**
	 * The init method will take in a BPSService which needs to
	 *  implement methods for getting different BPS classes (e.g BPSLogger).
	 * The library will use the different classes as callbacks, since the 
	 * 	implementation must be implemented outside the BPS library.  
	 * @param service The BPSService implementation.
	 */
	public void init(BPSService service) {
		this.service = service;
	}
	
	/**
	 * Constructor for BPS, which is private since BPS is a singleton.
	 * @param service The BPSService implementation that the BPS library will use for device-specific methods.
	 */
	private BPS(BPSService service) {
		this.service = service;
	}
	
	/* Methods */
	
	
}
