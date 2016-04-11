package ca.ubc.ece.salt.pangor.classify.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DataSet;

/**
 * The {@code DataSet} manages the alerts that were generated during the
 * analysis.
 */
public class ClassifierDataSet extends DataSet {

	/**
	 * The path to the file where the data set will be cached. This allows us
	 * to limit our memory use and cache results for the future by storing the
	 * keyword extraction results on the disk.
	 */
	private String dataSetPath;

	/**
	 * Used to produce a data set of the analysis results.
	 * @param dataSetPath The file path to store the data set.
	 * @param supplementaryPath The directory path to store the supplementary
	 * 		  files.
	 * @throws Exception Throws an exception when the {@code dataSetPath}
	 * 					 cannot be read.
	 */
	private ClassifierDataSet(String dataSetPath, List<IRule> rules,
							  List<IQuery> queries) {
		super(rules, queries);
		this.dataSetPath = dataSetPath;
	}

	/**
	 * Adds a feature vector to the data set. If a data set file exist
	 * ({@code dataSetPath}), serializes the feature vector and writes it to
	 * the file. Otherwise, the feature vector is stored in memory in
	 * {@code LearningDataSet}.
	 * @param commit The commit that is being analyzed.
	 * @param knowledgeBase The fact database to query.
	 */
	@Override
	protected void registerAlerts(Commit commit, IKnowledgeBase knowledgeBase)
			throws Exception {

		Map<String, ClassifierFeatureVector> featureVectors = new HashMap<String, ClassifierFeatureVector>();

		for(IQuery query : this.queries) {

			IRelation results = knowledgeBase.execute(query);

			/* Iterate through the tuples that are members of the relation and add
			 * them as alerts. */
			for(int i = 0; i < results.size(); i++) {

				ITuple tuple = results.get(i);

				/* Lookup or create the LearningFeatureVector. */
				String key = commit.projectID + "_" + commit.repairedCommitID 	// Identifies the commit
							 + "_" + tuple.get(0) + "_" + tuple.get(1); 		// Identifies the class/method
				ClassifierFeatureVector featureVector = featureVectors.get(key);

				/* Add the feature vector if it is not yet in the map. */
				if(featureVector == null) {
					featureVector = new ClassifierFeatureVector(commit,
							tuple.get(0).toString(), 	// Class
							tuple.get(1).toString(), 	// Method
							tuple.get(2).toString(), 	// Type
							tuple.get(3).toString(), 	// Subtype
							tuple.get(4).toString());	// Description
					featureVectors.put(key, featureVector);
				}

			}

		}

	}

}