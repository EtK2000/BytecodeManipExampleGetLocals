package com.etk2000.bcm.getlocals.bytecode;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;

public class ReplaceGetLocalsCallsClassVisitor extends ClassVisitor {
	public ReplaceGetLocalsCallsClassVisitor(@Nonnull ClassWriter cw) {
		super(Opcodes.ASM9, cw);
	}

	@Override
	public MethodVisitor visitMethod(int access, @Nonnull String name, @Nonnull String descriptor, String signature, String[] exceptions) {
		return new ReplaceGetLocalsCallsMethodNode(
				access,
				super.visitMethod(access, name, descriptor, signature, exceptions)
		);
	}
}