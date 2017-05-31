package com.bluetrainsoftware.dtogen;

import com.bluetrainsoftware.dtogen.dto.DtoHolder;

import java.util.Map;
import java.util.Set;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public interface DtoHolderEnhancer {
	void additionalProcessing(SourceModule module, Set<Class<?>> classesToExport, Map<Class<?>, DtoHolder> holders);
}
