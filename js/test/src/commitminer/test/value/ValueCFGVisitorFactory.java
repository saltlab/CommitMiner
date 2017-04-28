package commitminer.test.value;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.SourceCodeFileChange;
import commitminer.cfg.ICFGVisitor;
import commitminer.cfg.ICFGVisitorFactory;

public class ValueCFGVisitorFactory implements ICFGVisitorFactory {

	@Override
	public ICFGVisitor newInstance(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts) {
		return new ValueCFGVisitor(sourceCodeFileChange, facts);
	}

}
