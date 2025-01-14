package works.bosk.drivers.operations;

import java.util.Collection;
import works.bosk.BoskDriver;
import works.bosk.MapValue;
import works.bosk.Reference;

public record SubmitInitialization<T>(
	Reference<T> target,
	T newValue,
	MapValue<String> diagnosticAttributes
) implements ReplacementOperation<T> {

	@Override
	public SubmitInitialization<T> withFilteredAttributes(Collection<String> allowedNames) {
		return new SubmitInitialization<>(target, newValue, MapValue.fromFunction(allowedNames, diagnosticAttributes::get));
	}

	@Override
	public void submitTo(BoskDriver driver) {
		driver.submitInitialization(target, newValue);
	}

}
