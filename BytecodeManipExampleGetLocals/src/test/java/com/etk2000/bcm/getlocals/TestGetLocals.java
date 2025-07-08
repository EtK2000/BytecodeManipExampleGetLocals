package com.etk2000.bcm.getlocals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestGetLocals {
	@Test
	void testInCatch() {
		// no locals
		assertEquals(Map.of(), GetLocals.call());

		// only a caught exception
		try {
			throw new Exception("some exception");
		}
		catch (Exception e) {
			assertEquals(
					Map.of("e", e),
					GetLocals.call()
			);
		}
	}

	@Test
	void testWithLocals() {
		// no locals
		assertEquals(Map.of(), GetLocals.call());

		// 1 local
		final int someInt = 1;
		assertEquals(
				Map.of("someInt", someInt),
				GetLocals.call()
		);

		// 2 locals
		final String someString = "hello world!";
		assertEquals(
				Map.of(
						"someInt", someInt,
						"someString", someString
				),
				GetLocals.call()
		);
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3})
	void testWithParameter(int param) {
		// parameter only
		assertEquals(
				Map.of("param", param),
				GetLocals.call()
		);

		// parameter + local
		final int someInt = 1;
		assertEquals(
				Map.of(
						"param", param,
						"someInt", someInt
				),
				GetLocals.call()
		);
	}
}