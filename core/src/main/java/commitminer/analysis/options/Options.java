package commitminer.analysis.options;

/**
 * Contains analysis options.
 */
public class Options {
	
	private static Options instance;
	
	private DiffMethod diffMethod;
	private ChangeImpact changeImpact;
	
	private Options() {
		this.diffMethod = DiffMethod.GUMTREE;
		this.changeImpact = ChangeImpact.MULTIDIFF;
	}

	private Options(DiffMethod diffMethod, ChangeImpact changeImpact) {
		this.diffMethod = diffMethod;
		this.changeImpact = changeImpact;
	}
	
	public DiffMethod getDiffMethod() {
		return this.diffMethod;
	}

	public void setDiffMethod(Options.DiffMethod diffMethod) {
		this.diffMethod = diffMethod;
	}
	
	public ChangeImpact getChangeImpact() {
		return this.changeImpact;
	}
	
	/**
	 * @return the singleton {@code Options}.
	 */
	public static Options getInstance() {
		if(instance == null) instance = new Options();
		return instance;
	}

	/**
	 * @return the singleton {@code Options}.
	 */
	public static Options createInstance(DiffMethod diffMethod, ChangeImpact changeImpact) {
		if(instance == null) instance = new Options(diffMethod, changeImpact);
		return instance;
	}
	
	/**
	 * The structural change analysis strategy.
	 */
	public enum DiffMethod {
		/** Use Gumtree AST differencing. **/
		GUMTREE,
		/** Use Meyers line differencing (Unix diff). **/
		MEYERS
	}
	
	/**
	 * The change impact strategy.
	 */
	public enum ChangeImpact {
		/** Run the regular MultiDiff analysis (no data or control dependencies). **/
		MULTIDIFF,
		/** Also compute data and control dependencies. **/
		DEPENDENCIES
	}

}
