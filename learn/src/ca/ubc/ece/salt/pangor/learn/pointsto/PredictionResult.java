package ca.ubc.ece.salt.pangor.learn.pointsto;


import ca.ubc.ece.salt.pangor.learn.api.AbstractAPI;

/**
 * Data structure to store an API and a likelihood calculated by a prediction
 */
public class PredictionResult {
	/** The API predicted */
	public AbstractAPI api;

	/** The likelihood calculated by the prediction */
	public double likelihood;

	public PredictionResult(AbstractAPI api, double likelihood) {
		this.api = api;
		this.likelihood = likelihood;
	}
}