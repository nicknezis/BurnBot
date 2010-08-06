package com.nicknackhacks.dailyburn.model;

//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800): <diet-goal>
//D/BurnBot ( 9035):   <created-on type="date">2010-04-19</created-on>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <dynamic type="boolean">true</dynamic>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <id type="integer">2956609</id>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <lower-bound type="integer">48</lower-bound>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <upper-bound type="integer">83</upper-bound>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <user-id type="integer">176766</user-id>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <unit>grams</unit>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <goal-type>TotalFatDietGoal</goal-type>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <adjusted-lower-bound type="integer">48</adjusted-lower-bound>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <adjusted-upper-bound type="integer">83</adjusted-upper-bound>
//D/BurnBot ( 9035):   <diet-plan-percent type="float">0.3</diet-plan-percent>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800): </diet-goal>
//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800): </diet-goals>


public class DietGoal {
	private String createdOn;
	private boolean dynamic;
	private int id;
	private int lowerBound;
	private int upperBound;
	private int userId;
	private String unit;
	private String goalType;
	private int adjustedLowerBound;
	private int adjustedUpperBound;
	private float dietPlanPercent;

	public String getCreatedOn() {
		return createdOn;
	}
	
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	
	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

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

	public String getGoalType() {
		return goalType;
	}

	public void setGoalType(String goalType) {
		this.goalType = goalType;
	}

	public int getAdjustedLowerBound() {
		return adjustedLowerBound;
	}

	public void setAdjustedLowerBound(int adjustedLowerBound) {
		this.adjustedLowerBound = adjustedLowerBound;
	}

	public int getAdjustedUpperBound() {
		return adjustedUpperBound;
	}

	public void setAdjustedUpperBound(int adjustedUpperBound) {
		this.adjustedUpperBound = adjustedUpperBound;
	}

	public float getDietPlanPercent() {
		return dietPlanPercent;
	}

	public void setDietPlanPercent(float dietPlanPercent) {
		this.dietPlanPercent = dietPlanPercent;
	}
}
