package commitminer.classify;

import org.deri.iris.api.basics.ITuple;

import commitminer.analysis.Commit;

public interface Transformer {

	public abstract ClassifierFeatureVector transform(Commit commit,
			ITuple tuple);

}