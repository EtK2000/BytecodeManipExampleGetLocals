package com.etk2000.bcm.getlocals;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public abstract class DependencyPatcherTask extends DefaultTask {
	private static final Pattern CLASSES_PATTERN = Pattern.compile("[/\\\\]build[/\\\\]classes[/\\\\]java[/\\\\](main|test)$");
	public Task compileTask;

	@TaskAction
	public void patch() {
		if (compileTask == null)
			throw new IllegalStateException("compileTask needs to be set");

		final File buildDir = compileTask.getOutputs().getFiles().getFiles().stream()
				.filter(file -> CLASSES_PATTERN.matcher(file.getAbsolutePath()).find())
				.findFirst()
				.orElse(null);
		if (buildDir == null) {
			System.out.printf(
					"Didn't find com.etk2000.bcm:GetLocals, not patching %s...\n",
					compileTask.getName()
			);
			return;
		}

		try {
			// patching is all that's needed here because we're modifying inplace
			BytecodeManipExampleGetLocalsProcessor.processDirectory(buildDir);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private void updateDependentTasksClasspath(@Nonnull File originalJar, @Nonnull File patchedJar) {
		// normal compiles
		getProject().getTasks().withType(JavaCompile.class).configureEach(task -> {
			task.setClasspath(
					task.getClasspath()
							.minus(getProject().files(originalJar))
							.plus(getProject().files(patchedJar))
			);
		});
		getProject().getTasks().withType(JavaExec.class).configureEach(task -> {
			task.setClasspath(
					task.getClasspath()
							.minus(getProject().files(originalJar))
							.plus(getProject().files(patchedJar))
			);
		});

		// update tests
		getProject().getTasks().withType(Test.class).configureEach(task -> {
			task.setClasspath(
					task.getClasspath()
							.minus(getProject().files(originalJar))
							.plus(getProject().files(patchedJar))
			);
		});
	}
}