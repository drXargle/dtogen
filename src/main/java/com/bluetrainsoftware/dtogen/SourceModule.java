package com.bluetrainsoftware.dtogen;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class SourceModule {
  @Parameter
  private String destinationPackage;

  @Parameter
  private String sourcePackage;

	/**
	 * By default generate the dtos
	 */
	@Parameter
  private boolean generateDto = true;

	/**
	 * By default don't generate the mapper
	 */
	@Parameter
  private boolean generateMapper = false;

	/**
	 * Allow overrides on the dto template
	 */
	@Parameter
  private String dtoTemplate;

	/**
	 * Allow overrides on the mapper template
	 */
  @Parameter
  private String mapperTemplate;

  @Parameter
  private List<String> packageRelativeExclusions;

	public String getDestinationPackage() {
		return destinationPackage;
	}

	public void setDestinationPackage(String destinationPackage) {
		this.destinationPackage = destinationPackage;
	}

	public String getSourcePackage() {
		return sourcePackage;
	}

	public void setSourcePackage(String sourcePackage) {
		this.sourcePackage = sourcePackage;
	}

	public boolean isGenerateDto() {
		return generateDto;
	}

	public void setGenerateDto(boolean generateDto) {
		this.generateDto = generateDto;
	}

	public boolean isGenerateMapper() {
		return generateMapper;
	}

	public void setGenerateMapper(boolean generateMapper) {
		this.generateMapper = generateMapper;
	}

	public String getDtoTemplate() {
		return dtoTemplate;
	}

	public void setDtoTemplate(String dtoTemplate) {
		this.dtoTemplate = dtoTemplate;
	}

	public String getMapperTemplate() {
		return mapperTemplate;
	}

	public void setMapperTemplate(String mapperTemplate) {
		this.mapperTemplate = mapperTemplate;
	}

	public List<String> getPackageRelativeExclusions() {
		return packageRelativeExclusions;
	}

	public void setPackageRelativeExclusions(List<String> packageRelativeExclusions) {
		this.packageRelativeExclusions = packageRelativeExclusions;
	}
}
