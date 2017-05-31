package com.bluetrainsoftware.dtogen;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class DtoGeneratorTest {
	@Test
	public void tryit() throws MojoFailureException, MojoExecutionException {
		if (!new File("src/main/java").exists()) {
			throw new RuntimeException("need to be in project to run test");
		}
		DtoGeneneratorMojo dtoGeneneratorMojo = new DtoGeneneratorMojo();
		SourceModule sourceModule = new SourceModule();
		sourceModule.setDestinationPackage("com.bluetrainsoftware.dtogen.test");
		sourceModule.setSourcePackage("com.bluetrainsoftware.dtogen.sample");
		sourceModule.setGenerateMapper(true);

		dtoGeneneratorMojo.sourceModules = Collections.singletonList(sourceModule);
		dtoGeneneratorMojo.javaOutFolder = new File("./target/generated-sources/dto");

		dtoGeneneratorMojo.execute();
	}
}
