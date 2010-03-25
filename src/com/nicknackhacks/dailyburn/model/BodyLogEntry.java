package com.nicknackhacks.dailyburn.model;
//<?xml version="1.0" encoding="UTF-8"?>
//<body-log-entries type="array">
//  <body-log-entry>
//    <id type="integer">129579</id>
//    <logged-on type="date">2009-07-22</logged-on>
//    <user-id type="integer">1</user-id>
//    <value type="decimal">186.0</value>
//    <unit>lbs</unit>
//    <body-metric-identifier>body_weight</body-metric-identifier>
//  </body-log-entry>
//  <body-log-entry>
//    <id type="integer">129535</id>
//    <logged-on type="date">2009-06-09</logged-on>
//    <user-id type="integer">1</user-id>
//    <value type="decimal">185.0</value>
//    <unit>lbs</unit>
//    <body-metric-identifier>body_weight</body-metric-identifier>
//  </body-log-entry>
//</body-log-entries>
public class BodyLogEntry {
	private int id;
	private String loggedOn;
	private int userId;
	private float value;
	private String unit;
	private String metricIdentifier;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLoggedOn() {
		return loggedOn;
	}
	public void setLoggedOn(String loggedOn) {
		this.loggedOn = loggedOn;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getMetricIdentifier() {
		return metricIdentifier;
	}
	public void setMetricIdentifier(String metricIdentifier) {
		this.metricIdentifier = metricIdentifier;
	}
}
