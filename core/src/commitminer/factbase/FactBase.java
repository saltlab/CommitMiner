package commitminer.factbase;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.Version;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.annotation.Annotation;
import commitminer.annotation.DependencyIdentifier;

public abstract class FactBase {
	
	/* The fact database. */
	protected Map<IPredicate, IRelation> facts;
	
	/* The source file pair. */
	protected SourceCodeFileChange sourceCodeFileChange;
	
	protected FactBase(Map<IPredicate, IRelation> facts, SourceCodeFileChange sourceCodeFileChange) {
		this.facts = facts;
		this.sourceCodeFileChange = sourceCodeFileChange;
	}
	
}
