package com.nicknackhacks.dailyburn.model;

//<diet-goals type="array">
//<diet-goal>
//<dynamic type="boolean">true</dynamic>
//<id type="integer">2753477</id>
//<lower-bound type="integer">1956</lower-bound>
//<upper-bound type="integer">2206</upper-bound>
//<user-id type="integer">176766</user-id>
//<unit>calories</unit>
//<goal-type>CalorieDietGoal</goal-type>
//<adjusted-lower-bound type="integer">1956</adjusted-lower-bound>
//<adjusted-upper-bound type="integer">2206</adjusted-upper-bound>
//</diet-goal>

public class DietGoal {
	private boolean dynamic;
	private int id;
	private int lowerBound;
	private int upperBound;
	private int userId;
	private String unit;
	private GoalType goalType;
	private int adjustedLowerBound;
	private int adjustedUpperBound;

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

	public GoalType getGoalType() {
		return goalType;
	}

	public void setGoalType(GoalType goalType) {
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
}
