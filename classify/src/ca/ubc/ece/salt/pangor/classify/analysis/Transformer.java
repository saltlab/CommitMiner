package ca.ubc.ece.salt.pangor.classify.analysis;

import org.deri.iris.api.basics.ITuple;

import ca.ubc.ece.salt.pangor.analysis.Commit;

public interface Transformer {

	public abstract ClassifierFeatureVector transform(Commit commit,
			ITuple tuple);

}