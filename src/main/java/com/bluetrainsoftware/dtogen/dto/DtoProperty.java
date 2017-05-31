package com.bluetrainsoftware.dtogen.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class DtoProperty {
	public String propName;
	public String propField;
	public String propType;
	public DtoHolder mappingEntity;
	public boolean isCollection;
	public boolean isList;
	public boolean isSet;
	public boolean isMap; // not supported
	public boolean isArray;
	public Map<Object, Object> additional = new HashMap<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DtoProperty that = (DtoProperty) o;

		return propName != null ? propName.equals(that.propName) : that.propName == null;
	}

	@Override
	public int hashCode() {
		return propName != null ? propName.hashCode() : 0;
	}
}
