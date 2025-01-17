package works.bosk;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import works.bosk.annotations.Self;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static works.bosk.ReferenceUtils.theOnlyConstructorFor;

// TODO: This should aim for full coverage of SerializationPlugin
class SerializationPluginTest {

	@Test
	void inheritedFieldAttribute_works() {
		Constructor<Child> childConstructor = theOnlyConstructorFor(Child.class);
		Parameter selfParameter = childConstructor.getParameters()[0];
		assertTrue(SerializationPlugin.isSelfReference(Child.class, selfParameter));
	}

	@RequiredArgsConstructor
	@Getter
	static class Parent {
		@Self final Parent self;
	}

	@Getter
	static class Child extends Parent {
		public Child(Parent self) {
			super(self);
		}
	}
}