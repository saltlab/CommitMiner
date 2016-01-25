package ca.ubc.ece.salt.pangor.learn;

import ca.ubc.ece.salt.pangor.api.KeywordUse;

/**
 * Stores meta data for a cluster. Used to summarize clustering results.
 */
public class Cluster {

	public KeywordUse keyword;
	public int cluster;
	public int instances;

	public Cluster(KeywordUse keyword, int cluster, int instances) {
		this.keyword = keyword;
		this.cluster = cluster;
		this.instances = instances;
	}

	@Override
	public String toString() {
		return keyword.toString() + ": C = " + cluster + ", I = " + instances;
	}

}