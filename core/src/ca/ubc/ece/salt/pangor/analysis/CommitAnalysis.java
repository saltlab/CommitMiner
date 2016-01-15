package ca.ubc.ece.salt.pangor.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

/**
 * Gathers facts about one commit from various domains and runs queries in
 * datalog against those facts to synthesize alerts.
 */
public class CommitAnalysis {

	/**
	 * The data set manages the alerts by storing and loading alerts to and
	 * from the disk, performing pre-processing tasks and calculating metrics.
	 */
	private DataSet dataSet;

	/**
	 * The list of analyses that we will run on this commit. Each
	 * {@code DomainAnalysis} gathers facts for one domain.
	 */
	private List<DomainAnalysis> domainAnalyses;

	/**
	 * @param dataSet The data set that will generate and store the alerts.
	 * @param domainAnalyses The domains to extract facts from.
	 */
	public CommitAnalysis(DataSet dataSet, List<DomainAnalysis> domainAnalyses) {
		this.dataSet = dataSet;
		this.domainAnalyses = domainAnalyses;
	}

	/**
	 * Analyzes the commit and creates alerts.
	 *
	 * The commit is analyzed by each domain analysis. A database of facts is
	 * stored and each domain analysis adds to the database.
	 * @param commit The commit we are analyzing.
	 * @throws Exception
	 */
	public void analyze(Commit commit) throws Exception {

		/* Initialize the fact base that will be filled by the domain analyses. */
		Map<IPredicate, IRelation> facts = new HashMap<IPredicate, IRelation>();

		/* Run each domain analysis on the commit. */
		for(DomainAnalysis domainAnalysis : domainAnalyses) {
			domainAnalysis.analyze(commit, facts);
		}

		/* Synthesize the alerts from the analysis facts. */
		this.synthesizeAlerts(commit, facts);

	}

	/**
	 * Registers alerts by applying rules to the facts found during the analysis.
	 *
	 * This method is effective for patterns that are contained within one
	 * file. For analyses that need knowledge about multiple files, this method
	 * should be overridden.
	 *
	 * @param commit The details for the commit.
	 * @param facts The facts derived from the source code file analyses.
	 * @throws Exception
	 */
	protected void synthesizeAlerts(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {

		/* Query the knowledge base and create alerts. */
		this.dataSet.addCommitAnalysisResults(commit, facts);

	}

}
