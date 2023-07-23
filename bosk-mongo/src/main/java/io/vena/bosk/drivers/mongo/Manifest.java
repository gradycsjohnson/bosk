package io.vena.bosk.drivers.mongo;

import io.vena.bosk.StateTreeNode;
import java.util.Optional;
import lombok.Value;

@Value
public class Manifest implements StateTreeNode {
	Integer version;
	Optional<EmptyNode> sequoia;

	@Value
	public static class EmptyNode implements StateTreeNode {}

	public static Manifest forSequoia() {
		return new Manifest(1, Optional.of(new EmptyNode()));
	}
}