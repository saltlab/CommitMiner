package commitminer.js.diff.controlcall;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.cfg.ICFGVisitor;
import commitminer.cfg.ICFGVisitorFactory;

public class ControlCallCFGVisitorFactory implements ICFGVisitorFactory {

	@Override
	public ICFGVisitor newInstance(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts) {
		return new ControlCallCFGVisitor(AnnotationFactBase.getInstance(sourceCodeFileChange));
	}

}
