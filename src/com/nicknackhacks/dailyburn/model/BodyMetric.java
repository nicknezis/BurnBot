package com.nicknackhacks.dailyburn.model;
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

	private int id;
	private String name;
	private boolean pro;
	private String identifier;
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
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
