package org.eclipse.viatra.solver.data.model.internal;

import java.util.Objects;

import org.eclipse.viatra.solver.data.map.ContinousHashProvider;
import org.eclipse.viatra.solver.data.model.Tuple;
import org.eclipse.viatra.solver.data.model.representation.Relation;

public class SimilarRelationEquivalenceClass {
	final ContinousHashProvider<Tuple> hashProvider;
	final Object defaultValue;
	final int arity;
	public SimilarRelationEquivalenceClass(Relation<?> representation) {
		this.hashProvider = representation.getHashProvider();
		this.defaultValue = representation.getDefaultValue();
		this.arity = representation.getArity();
	}
	@Override
	public int hashCode() {
		return Objects.hash(arity, defaultValue, hashProvider);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SimilarRelationEquivalenceClass))
			return false;
		SimilarRelationEquivalenceClass other = (SimilarRelationEquivalenceClass) obj;
		return arity == other.arity && Objects.equals(defaultValue, other.defaultValue)
				&& Objects.equals(hashProvider, other.hashProvider);
	}
	
}