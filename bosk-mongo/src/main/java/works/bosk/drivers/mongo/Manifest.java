package works.bosk.drivers.mongo;

import java.util.Optional;
import works.bosk.StateTreeNode;
import works.bosk.drivers.mongo.MongoDriverSettings.DatabaseFormat;

/**
 * Defines the format of the manifest document, which is stored in the database
 * to describe the database contents.
 */
public record Manifest(
	Integer version,
	Optional<EmptyNode> sequoia,
	Optional<PandoFormat> pando
) implements StateTreeNode {
	public Manifest(Integer version, Optional<EmptyNode> sequoia, Optional<PandoFormat> pando) {
		// Note: this could be a compact constructor, but then it won't work:
		// https://github.com/adoptium/adoptium-support/issues/1025
		if (sequoia.isPresent() == pando.isPresent()) {
			throw new IllegalArgumentException("Exactly one format (sequoia or pando) must be specified in manifest");
		}
		this.version = version;
		this.sequoia = sequoia;
		this.pando = pando;
	}

	public record EmptyNode() implements StateTreeNode { }

	public static Manifest forSequoia() {
		return new Manifest(1, Optional.of(new EmptyNode()), Optional.empty());
	}

	public static Manifest forPando(PandoFormat settings) {
		return new Manifest(1, Optional.empty(), Optional.of(settings));
	}

	public static Manifest forFormat(DatabaseFormat format) {
		// TODO: Use a type switch in newer Java versions
		if (format instanceof PandoFormat p) {
			return forPando(p);
		} else {
			return forSequoia();
		}
	}
}
