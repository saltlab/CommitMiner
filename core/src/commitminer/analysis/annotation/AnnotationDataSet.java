package commitminer.analysis.annotation;

import org.deri.iris.api.IKnowledgeBase;

import commitminer.analysis.Commit;
import commitminer.analysis.DataSet;

public class AnnotationDataSet extends DataSet {
	
	private AnnotationFactBase factBase;

	public AnnotationDataSet(AnnotationFactBase factBase) {
		super(null, null);
		this.factBase = factBase;
	}

	@Override
	protected void registerAlerts(Commit commit, IKnowledgeBase knowledgeBase)
			throws Exception {
		/* Nothing to do. The AnnotationFactBase can be used as-is, without
		 * running any queries (which is why we do not use datalog here). */
	}
	
	public AnnotationFactBase getAnnotationFactBase() {
		return factBase;
	}
	
	public void printDataSet() {
		for(Annotation annotation : factBase.getAnnotations()) {
			System.out.println(annotation.toString());
		}
	}

}
