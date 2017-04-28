package commitminer.analysis.flow.abstractdomain;


/**
 * Scratchpad memory. Unlike JSAI, we define a fixed set of values that we
 * store.
 */
public class Scratchpad {

	/** The scratch memory. **/
	private BValue[] scratchMem;

	public Scratchpad() {
		this.scratchMem = new BValue[Scratch.values().length];
	}

	private Scratchpad(BValue[] scratchMem) {
		this.scratchMem = scratchMem;
	}

	public Scratchpad(Scratch loc, BValue retVal) {
		this.scratchMem = new BValue[Scratch.values().length];
		this.scratchMem[loc.val] = retVal;
	}

	@Override
	public Scratchpad clone() {
		return new Scratchpad(scratchMem);
	}

	/**
	 * @return The value at the location is scratchpad memory.
	 */
	public BValue apply(Scratch loc) {
		return this.scratchMem[loc.val];
	}

	/**
	 * Performs a strong update.
	 * @param loc The location in scratchpad memory.
	 * @param value The value to update.
	 * @return The updated scratchpad.
	 */
	public Scratchpad strongUpdate(Scratch loc, BValue retVal) {
		BValue[] scratchMem = new BValue[this.scratchMem.length];
		for(int i = 0; i < scratchMem.length; i++) scratchMem[i] = this.scratchMem[i];
		scratchMem[loc.val] = retVal;
		return new Scratchpad(scratchMem);
	}

	/**
	 * Compute the union of this and another Scratchpad.
	 * @param pad The Scratchpad to union.
	 * @return The union of the scratchpads.
	 */
	public Scratchpad join(Scratchpad pad) {

		BValue[] scratchMem = new BValue[Scratch.values().length];

		for(int i = 0; i < this.scratchMem.length; i++) {
			if(this.scratchMem[i] == null) scratchMem[i] = pad.scratchMem[i];
			else if(pad.scratchMem[i] == null) scratchMem[i] = this.scratchMem[i];
			else scratchMem[i] = this.scratchMem[i].join(pad.scratchMem[i]);
		}

		return new Scratchpad(scratchMem);

	}

	/**
	 * The types of values we can store in scratch memory and their locations.
	 */
	public enum Scratch {
		RETVAL(0); 					// A return valu

		private int val;

		private Scratch(int val) {
			this.val = val;
		}
	}

}