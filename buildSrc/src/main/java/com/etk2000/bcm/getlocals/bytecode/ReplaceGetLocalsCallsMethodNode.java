package com.etk2000.bcm.getlocals.bytecode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;

public class ReplaceGetLocalsCallsMethodNode extends MethodNode {
	@Nonnull
	private final MethodVisitor visitor;

	public ReplaceGetLocalsCallsMethodNode(int access, @Nonnull MethodVisitor visitor) {
		super(Opcodes.ASM9, access, null, null, null, null);
		this.visitor = visitor;
	}

	@Override
	public void visitEnd() {
		for (AbstractInsnNode insn : this.instructions) {

			// Look for calls to com/etk2000/bcm/getlocals/GetLocals::call
			if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
				final MethodInsnNode methodInsn = (MethodInsnNode) insn;
				if (methodInsn.owner.equals("com/etk2000/bcm/getlocals/GetLocals") && methodInsn.name.equals("call")) {
					final InsnList inject = new InsnList();

					// Map map = new HashMap<>();
					inject.add(new TypeInsnNode(Opcodes.NEW, "java/util/HashMap"));
					inject.add(new InsnNode(Opcodes.DUP));
					inject.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false));
					final int mapIndex = this.maxLocals++;
					inject.add(new VarInsnNode(Opcodes.ASTORE, mapIndex));

					// Add all locals into map
					final int injectionPoint = this.instructions.indexOf(insn);
					for (LocalVariableNode local : this.localVariables) {

						// Skip `this`
						if (local.index == 0 && !Modifier.isStatic(this.access))
							continue;

						// Skip uninitialized variables
						if (this.instructions.indexOf(local.start) >= injectionPoint)
							continue;

						// Stack = [ map, local_name, local_value ]
						inject.add(new VarInsnNode(Opcodes.ALOAD, mapIndex));
						inject.add(new LdcInsnNode(local.name));
						inject.add(loadVariableInsn(local.desc, local.index));
						inject.add(box(local.desc)); // box if needed

						// Call map.put(local_name, local_value) and discard the returned value
						inject.add(new MethodInsnNode(
								Opcodes.INVOKEINTERFACE,
								"java/util/Map",
								"put",
								"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
								true
						));
						inject.add(new InsnNode(Opcodes.POP));
					}

					// Inject setup before the call
					this.instructions.insertBefore(methodInsn, inject);

					// Replace the original call with a load of the local
					this.instructions.set(methodInsn, new VarInsnNode(Opcodes.ALOAD, mapIndex));
				}
			}
		}

		// Now run the MethodVisitor
		accept(this.visitor);
	}

	@Nonnull
	private AbstractInsnNode loadVariableInsn(@Nonnull String desc, int index) {
		return switch (desc) {
			case "B", "C", "I", "S", "Z" -> new VarInsnNode(Opcodes.ILOAD, index);
			case "D" -> new VarInsnNode(Opcodes.DLOAD, index);
			case "F" -> new VarInsnNode(Opcodes.FLOAD, index);
			case "J" -> new VarInsnNode(Opcodes.LLOAD, index);
			default -> new VarInsnNode(Opcodes.ALOAD, index);
		};
	}

	@Nonnull
	private AbstractInsnNode box(@Nonnull String desc) {
		return switch (desc) {
			case "B" -> callValueOf("java/lang/Byte", desc);
			case "C" -> callValueOf("java/lang/Character", desc);
			case "D" -> callValueOf("java/lang/Double", desc);
			case "F" -> callValueOf("java/lang/Float", desc);
			case "I" -> callValueOf("java/lang/Integer", desc);
			case "J" -> callValueOf("java/lang/Long", desc);
			case "S" -> callValueOf("java/lang/Short", desc);
			case "Z" -> callValueOf("java/lang/Boolean", desc);
			default -> new InsnNode(Opcodes.NOP); // reference type = no boxing
		};
	}

	@Nonnull
	private MethodInsnNode callValueOf(@Nonnull String owner, @Nonnull String desc) {
		return new MethodInsnNode(
				Opcodes.INVOKESTATIC,
				owner,
				"valueOf",
				String.format("(%s)L%s;", desc, owner),
				false
		);
	}
}