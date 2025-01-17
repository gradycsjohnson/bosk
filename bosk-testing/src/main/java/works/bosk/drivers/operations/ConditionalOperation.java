package works.bosk.drivers.operations;

import works.bosk.Identifier;
import works.bosk.Reference;

/**
 * Doesn't include {@link SubmitInitialization} because that has a different kind of precondition.
 */
public sealed interface ConditionalOperation extends UpdateOperation permits
	SubmitConditionalDeletion,
	SubmitConditionalReplacement
{
	Reference<Identifier> precondition();
	Identifier requiredValue();
	UpdateOperation unconditional();
}
