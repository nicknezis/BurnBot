package com.nicknackhacks.dailyburn.model;



import com.nicknackhacks.dailyburn.R;

//<?xml version="1.0" encoding="UTF-8"?>
//<body-metrics type="array">
//<body-metric>
//  <id type="integer">8</id>
//  <name>Body Weight</name>
//  <pro type="boolean" nil="true"></pro>
//  <body-metric-identifier>body_weight</body-metric-identifier>
//  <unit>lbs|kg</unit>
//</body-metric>
//...
//<body-metric>
//  <id type="integer">646</id>
//  <name>Energy Level</name>
//  <pro type="boolean">true</pro>
//  <body-metric-identifier>energy_level</body-metric-identifier>
//  <unit>(1-10)</unit>
//</body-metric>
//</body-metrics>
public class BodyMetric {
	
	/**
	 * 
	 */
	public BodyMetric() {
	}

	/**
	 * @param id
	 * @param name
	 * @param pro
	 * @param metricIdentifier
	 * @param unit
	 */
	public BodyMetric(int id, String name, boolean pro,
			String metricIdentifier, String unit) {
		this.id = id;
		this.name = name;
		this.pro = pro;
		this.metricIdentifier = metricIdentifier;
		this.unit = unit;
	}

	private int id;
	private String name;
	public boolean pro;
	private String metricIdentifier;
	private String unit;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPro() {
		return pro;
	}

	public void setPro(boolean pro) {
		this.pro = pro;
	}

	public String getMetricIdentifier() {
		return metricIdentifier;
	}

	public void setMetricIdentifier(String identifier) {
		this.metricIdentifier = identifier;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public String toString() {
		if(isPro()) {
			return name + " (Pro)";
		}
		
		return name;
	}
}
