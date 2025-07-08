package com.etk2000.bcm.getlocals.bytecode;

import org.objectweb.asm.Label;

import javax.annotation.Nonnull;

record LocalVariableNode(
		@Nonnull String name,
		@Nonnull String desc,
		@Nonnull String signature,
		Label start,
		Label end,
		int index
) {
}