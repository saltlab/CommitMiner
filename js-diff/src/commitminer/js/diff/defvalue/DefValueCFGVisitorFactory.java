package commitminer.js.diff.defvalue;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.SourceCodeFileChange;
import commitminer.cfg.ICFGVisitor;
import commitminer.cfg.ICFGVisitorFactory;
import commitminer.js.annotation.AnnotationFactBase;

public class DefValueCFGVisitorFactory implements ICFGVisitorFactory {

	@Override
	public ICFGVisitor newInstance(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts) {
		return new DefValueCFGVisitor(AnnotationFactBase.getInstance(sourceCodeFileChange));
	}

}
