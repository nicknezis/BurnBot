package com.nicknackhacks.dailyburn.model;

/*<?xml version="1.0" encoding="UTF-8"?>
 <diet-goals type="array">
 <diet-goal>
 <id type="integer">220085</id>
 <lower-bound type="integer">1947</lower-bound>
 <upper-bound type="integer">2197</upper-bound>
 <user-id type="integer">1</user-id>
 <unit>calories</unit>
 <goal-type>CalorieDietGoal</goal-type>
 </diet-goal>
 ...
 <diet-goal>
 <id type="integer">146867</id>
 <lower-bound type="integer">0</lower-bound>
 <upper-bound type="integer">2500</upper-bound>
 <user-id type="integer">1</user-id>
 <unit>milligrams</unit>
 <goal-type>SodiumDietGoal</goal-type>
 </diet-goal>
 </diet-goals>*/

public class DietGoal {
	private int id;
	private int lowerBound;
	private int upperBound;
	private int userId;
	private String unit;
	private GoalType goalType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public GoalType getGoalType() {
		return goalType;
	}

	public void setGoalType(GoalType goalType) {
		this.goalType = goalType;
	}
}
