package com.bluetrainsoftware.dtogen;

import com.bluetrainsoftware.dtogen.dto.DtoHolder;
import com.bluetrainsoftware.dtogen.dto.DtoProperty;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import javax.persistence.Entity;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@Mojo(name = "generate",
	defaultPhase = LifecyclePhase.PROCESS_CLASSES,
	configurator = "include-project-dependencies",
	requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DtoGeneneratorMojo extends AbstractMojo {
	List<DtoHolderEnhancer> enhancers = new ArrayList<>();

	@Component
	MavenProjectHelper projectHelper;

	@Parameter(required = true)
	List<SourceModule> sourceModules;

	@Parameter(defaultValue = "${project}", readonly = true)
	MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/dto/src/test/java")
	File javaOutFolder;

	@Parameter(defaultValue = "${project.directory}")
	File projectDir;

	@Parameter
	String dtoTemplate = "/dtogen/dtogen.mustache";

	@Parameter
	String mapperTemplate = "/dtogen/mapper.mustache";

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		loadEnhancers();

		try {
			for(SourceModule module : sourceModules) {
				Set<Class<?>> classesToExport = new HashSet<>();

				if (module.getSourcePackage() != null) {
					new FastClasspathScanner(module.getSourcePackage())
						.matchClassesWithAnnotation(Entity.class, classesToExport::add).scan();
				}

				if (classesToExport.size() > 0) {
					exportModule(module, classesToExport);
				} else {
					getLog().error(String.format("found no classes or interfaces in package/path `%s`", module.getSourcePackage()));
				}
			}

			if (project != null) {
				project.addCompileSourceRoot(javaOutFolder.getAbsolutePath());
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private void loadEnhancers() {
		ServiceLoader<DtoHolderEnhancer> serviceLoader = ServiceLoader.load(DtoHolderEnhancer.class, Thread.currentThread().getContextClassLoader());
		Iterator<DtoHolderEnhancer> iterator = serviceLoader.iterator();
		while (iterator.hasNext()) {
			enhancers.add(iterator.next());
		}
	}

	private void exportModule(SourceModule module, Set<Class<?>> classesToExport) throws MojoExecutionException {
		Map<Class<?>, DtoHolder> holders = new HashMap<>();

 		classesToExport.forEach(clazz -> {
			DtoHolder dtoHolder = new DtoHolder();

			dtoHolder.dtoPackageName = module.getDestinationPackage() + ".dto";
			dtoHolder.mapperPackageName = module.getDestinationPackage() + ".mapper";
			dtoHolder.dtoName = clazz.getSimpleName() + "Dto";
			dtoHolder.entityName = clazz.getSimpleName();

			dtoHolder.addImport(Set.class).both = false;
			dtoHolder.addImport(List.class).both = false;

			dtoHolder.addImport(clazz);

			holders.put(clazz, dtoHolder);
		});

		for (Class<?> clazz : holders.keySet()) {
			DtoHolder dtoHolder = holders.get(clazz);

			extractFields(clazz, dtoHolder, holders);
		}

		// allow external stuff to enhance
		enhancers.forEach(e -> e.additionalProcessing(module, classesToExport, holders));

		if (module.isGenerateDto()) {
			String finalDtoTemplate = module.getDtoTemplate() == null ? dtoTemplate : module.getDtoTemplate();

			Template dtoTemplate = Mustache.compiler().compile(new InputStreamReader(getTemplate(finalDtoTemplate)));

			getLog().info(String.format("generating %d dtos from %s.", holders.size(), module.getSourcePackage()));
			for (Class<?> clazz : holders.keySet()) {
				DtoHolder dtoHolder = holders.get(clazz);

				dtoPackageFolder(dtoHolder).mkdirs();

				try (FileWriter fw = new FileWriter(dtoFilename(dtoHolder))) {
					getLog().debug(String.format("generating dto: %s.%s", dtoHolder.dtoPackageName, dtoHolder.dtoName));
					dtoTemplate.execute(dtoHolder, fw);
					fw.flush();
				} catch (IOException e) {
					getLog().error("Failed", e);
				}
			}
		}

		if (module.isGenerateMapper()) {
			String finalMapperTemplate = module.getDtoTemplate() == null ? mapperTemplate : module.getMapperTemplate();
			Template mapperTemplate = Mustache.compiler().compile(new InputStreamReader(getTemplate(finalMapperTemplate)));

			getLog().info(String.format("generating %d mappers from %s.", holders.size(), module.getSourcePackage()));

			for (Class<?> clazz : holders.keySet()) {
				DtoHolder dtoHolder = holders.get(clazz);

				mapperPackageFolder(dtoHolder).mkdirs();

				try (FileWriter fw = new FileWriter(mapperFilename(dtoHolder))) {
					getLog().debug(String.format("generating mapper for: %s.%s", dtoHolder.mapperPackageName, dtoHolder.entityName));
					mapperTemplate.execute(dtoHolder, fw);
					fw.flush();
				} catch (IOException e) {
					getLog().error("Failed", e);
				}
			}
		}
	}

	private InputStream getTemplate(String name) throws MojoExecutionException {
		InputStream stream = getClass().getResourceAsStream(name);

		if (stream == null) {
			if (!name.startsWith(File.separator)) {
				name = File.separator + name;
			}
			// try local project directory src/main/resources
			File f = new File(projectDir, "src/main/resources" + name);
			getLog().info("looking in " + f.getAbsolutePath());
			if (f.exists()) {
				try {
					stream = new FileInputStream(f);
				} catch (FileNotFoundException e) { // hard to see how this can happen
					throw new MojoExecutionException("Cannot find file", e);
				}
			}
		}

		if (stream == null) {
			// try local project directory src/test/resources
			File f = new File(projectDir, "src/test/resources" + name);
			if (f.exists()) {
				try {
					stream = new FileInputStream(f);
				} catch (FileNotFoundException e) { // hard to see how this can happen
					throw new MojoExecutionException("Cannot find file", e);
				}
			}
		}

		if (stream == null) {
			throw new MojoExecutionException("Cannot find resource named " + name);
		}

		return stream;
	}

	private File dtoFilename(DtoHolder holder) {
		return new File(dtoPackageFolder(holder), holder.dtoName  + ".java");
	}

	private File mapperFilename(DtoHolder holder) {
		return new File(mapperPackageFolder(holder), holder.entityName + "Mapper.java");
	}

	private File dtoPackageFolder(DtoHolder holder) {
		return new File(javaOutFolder, holder.dtoPackageName.replace('.', File.separatorChar) );
	}

	private File mapperPackageFolder(DtoHolder holder) {
		return new File(javaOutFolder, holder.mapperPackageName.replace('.', File.separatorChar) );
	}

	private void extractFields(Class<?> clazz, DtoHolder dtoHolder, Map<Class<?>, DtoHolder> holders) {

		try {
			Arrays.stream(Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors())
				.filter(pd -> Objects.nonNull(pd.getReadMethod()))
				.forEach(pd -> {
					Field field = null;
					try {
						field = clazz.getDeclaredField(pd.getName());
					} catch (NoSuchFieldException e) {
						getLog().error("no such field " + pd.getName(), e);
						return;
					}

					DtoProperty prop = new DtoProperty();

					prop.propField = field.getName();
					prop.propName = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
					prop.propType = field.getType().getSimpleName();

					prop.isCollection = Collection.class.isAssignableFrom(field.getType());
					prop.isSet = Set.class.isAssignableFrom(field.getType());
					prop.isList = List.class.isAssignableFrom(field.getType());
					prop.isMap = Map.class.isAssignableFrom(field.getType());
					prop.isArray = field.getType().isArray();

					if (field.getGenericType() instanceof ParameterizedType) {
						Class<?> persistentClass = (Class<?>)
							((ParameterizedType)field.getGenericType())
								.getActualTypeArguments()[0];
						prop.mappingEntity = holders.get(persistentClass);
						prop.propType = prop.propType + "<" + persistentClass.getSimpleName() + "Dto>";
						dtoHolder.addImport(persistentClass).both = (holders.get(persistentClass) == null);
					} else if (prop.isArray) {
						prop.mappingEntity = holders.get(field.getType().getComponentType());
						prop.propType = field.getType().getComponentType().getSimpleName() + "Dto[]";
					} else {
						prop.mappingEntity = holders.get(field.getType());
						if (prop.mappingEntity != null) {
							prop.propType += "Dto";
						}
					}

					dtoHolder.addImport(field.getType()).both = (holders.get(field.getType()) == null);

					dtoHolder.addProperty(prop);
				});

		} catch (IntrospectionException e) {
			getLog().error("Failed to get bean info", e);
		}
	}
}
