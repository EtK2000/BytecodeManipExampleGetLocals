package com.etk2000.bcm.getlocals;

import com.etk2000.bcm.getlocals.bytecode.ReplaceGetLocalsCallsClassVisitor;
import com.etk2000.bcm.getlocals.bytecode.SuperNameVisitor;
import com.etk2000.bcm.getlocals.util.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class BytecodeManipExampleGetLocalsProcessor {
	private static void processClassFile(@Nonnull File classFile) throws IOException {
		final ClassReader cr;
		try (FileInputStream fis = new FileInputStream(classFile)) {
			cr = new ClassReader(fis.readAllBytes());
		}

		final SuperNameVisitor scanner = new SuperNameVisitor();
		cr.accept(scanner, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ReplaceGetLocalsCallsClassVisitor(cw), 0);

		try (FileOutputStream fos = new FileOutputStream(classFile)) {
			fos.write(cw.toByteArray());
		}
	}

	public static void processDirectory(@Nonnull File directory) throws IOException {
		FileUtils.processClassFilesInTree(directory, BytecodeManipExampleGetLocalsProcessor::processClassFile);
	}

	private BytecodeManipExampleGetLocalsProcessor() {
	}
}