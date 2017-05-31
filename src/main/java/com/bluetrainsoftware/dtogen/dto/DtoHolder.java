package com.bluetrainsoftware.dtogen.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class DtoHolder {
	public String dtoPackageName;
	public String mapperPackageName;
	public String extend;
	public Set<ImportHolder> imports;
	public Set<DtoProperty> properties;
	public String entityName;
	public String dtoName;
	public Map<Object, Object> additional = new HashMap<>();

	public void addProperty(DtoProperty property) {
		if (properties == null) {
			properties = new HashSet<>();
		}

		properties.add(property);
	}

	public ImportHolder addImport(Class<?> clazz) {
		ImportHolder importHolder = new ImportHolder();

		if (clazz.getPackage() != null && !"java.lang".equals(clazz.getPackage().getName())) {
			importHolder.importName = clazz.getPackage().getName() + "." + clazz.getSimpleName();

			if (imports == null) {
				imports = new HashSet<>();
			}

			Optional<ImportHolder> first = imports.stream().filter(f -> importHolder.importName.equals(f.importName)).findFirst();

			if (first.isPresent()) {
				return first.get();
			} else {
				imports.add(importHolder);
			}
		}

		return importHolder;
	}
}
