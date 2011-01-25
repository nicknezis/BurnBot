package com.nicknackhacks.dailyburn.model;


/*
 * <?xml version="1.0" encoding="UTF-8"?>
<exercise-sets type="array">
      <exercise-set>
            <created-at type="datetime">2011-01-24T17:20:23-05:00</created-at>
            <id type="integer">16231618</id>
            <reps type="integer" nil="true"></reps>
            <set-order type="integer">1</set-order>
            <set-type>CardioSet</set-type>
            <logged-on type="date">2011-01-24</logged-on>
            <calories-burned type="integer">154</calories-burned>
            <workout-id nil="true"></workout-id>
            <exercise-id type="integer">512056</exercise-id>
            <exercise-name>Treadmill</exercise-name>
            <weight nil="true"></weight>
            <distance type="decimal">0.8</distance>
            <time type="decimal">602.0</time>
            <incline type="decimal">0.1</incline>
            <heart-rate type="decimal">120.0</heart-rate>
            <calorie nil="true"></calorie>
            <weight-unit nil="true"></weight-unit>
            <distance-unit>mi</distance-unit>
            <time-unit>s</time-unit>
            <incline-unit nil="true"></incline-unit>
            <heart-rate-unit nil="true"></heart-rate-unit>
      </exercise-set>
      <exercise-set>
            <created-at type="datetime">2011-01-24T17:18:57-05:00</created-at>
            <id type="integer">16231516</id>
            <reps type="integer">20</reps>
            <set-order type="integer">1</set-order>
            <set-type>WeightSet</set-type>
            <logged-on type="date">2011-01-24</logged-on>
            <calories-burned type="integer">29</calories-burned>
            <workout-id nil="true"></workout-id>
            <exercise-id type="integer">512198</exercise-id>
            <exercise-name>Lat Pulldowns - Wide-Grip</exercise-name>
            <weight type="decimal">85.0</weight>
            <distance nil="true"></distance>
            <time nil="true"></time>
            <incline nil="true"></incline>
            <heart-rate nil="true"></heart-rate>
            <calorie nil="true"></calorie>
            <weight-unit>lbs</weight-unit>
            <distance-unit nil="true"></distance-unit>
            <time-unit nil="true"></time-unit>
            <incline-unit nil="true"></incline-unit>
            <heart-rate-unit nil="true"></heart-rate-unit>
      </exercise-set>
      <exercise-set>
            <created-at type="datetime">2011-01-24T17:18:57-05:00</created-at>
            <id type="integer">16231517</id>
            <reps type="integer">15</reps>
            <set-order type="integer">2</set-order>
            <set-type>WeightSet</set-type>
            <logged-on type="date">2011-01-24</logged-on>
            <calories-burned type="integer">22</calories-burned>
            <workout-id nil="true"></workout-id>
            <exercise-id type="integer">512198</exercise-id>
            <exercise-name>Lat Pulldowns - Wide-Grip</exercise-name>
            <weight type="decimal">90.0</weight>
            <distance nil="true"></distance>
            <time nil="true"></time>
            <incline nil="true"></incline>
            <heart-rate nil="true"></heart-rate>
            <calorie nil="true"></calorie>
            <weight-unit>lbs</weight-unit>
            <distance-unit nil="true"></distance-unit>
            <time-unit nil="true"></time-unit>
            <incline-unit nil="true"></incline-unit>
            <heart-rate-unit nil="true"></heart-rate-unit>
      </exercise-set>
      <exercise-set>
            <created-at type="datetime">2011-01-24T17:18:57-05:00</created-at>
            <id type="integer">16231518</id>
            <reps type="integer">10</reps>
            <set-order type="integer">3</set-order>
            <set-type>WeightSet</set-type>
            <logged-on type="date">2011-01-24</logged-on>
            <calories-burned type="integer">15</calories-burned>
            <workout-id nil="true"></workout-id>
            <exercise-id type="integer">512198</exercise-id>
            <exercise-name>Lat Pulldowns - Wide-Grip</exercise-name>
            <weight type="decimal">95.0</weight>
            <distance nil="true"></distance>
            <time nil="true"></time>
            <incline nil="true"></incline>
            <heart-rate nil="true"></heart-rate>
            <calorie nil="true"></calorie>
            <weight-unit>lbs</weight-unit>
            <distance-unit nil="true"></distance-unit>
            <time-unit nil="true"></time-unit>
            <incline-unit nil="true"></incline-unit>
            <heart-rate-unit nil="true"></heart-rate-unit>
      </exercise-set>
      <exercise-set>
            <created-at type="datetime">2011-01-24T17:17:49-05:00</created-at>
            <id type="integer">16231448</id>
            <reps type="integer">20</reps>
            <set-order type="integer">1</set-order>
            <set-type>WeightSet</set-type>
            <logged-on type="date">2011-01-24</logged-on>
            <calories-burned type="integer">18</calories-burned>
            <workout-id nil="true"></workout-id>
            <exercise-id type="integer">512168</exercise-id>
            <exercise-name>Push-Ups</exercise-name>
            <weight type="decimal">250.0</weight>
            <distance nil="true"></distance>
            <time nil="true"></time>
            <incline nil="true"></incline>
            <heart-rate nil="true"></heart-rate>
            <calorie nil="true"></calorie>
            <weight-unit>lbs</weight-unit>
            <distance-unit nil="true"></distance-unit>
            <time-unit nil="true"></time-unit>
            <incline-unit nil="true"></incline-unit>
            <heart-rate-unit nil="true"></heart-rate-unit>
      </exercise-set>
</exercise-sets>

set_type - Can be either "WeightSet" or "CardioSet"
set_order - The ordering of this set (only useful when multiple WeightSets were created on the same exercise)
exercise_id - The id of the exercise this set was submitted for.
exercise_name - The name of the exercise (included for display purposes).
workout_id - If set, then this set was submitted in the context of the workout with this id.
logged_on - The date against which this set was recorded.
calories_burned - The number of calories burned for this set (could have been user-submitted or estimated by DailyBurn, CardioSets and WeightSets).
reps - the number of reps performed in this set (WeightSets only).
weight & weight_unit - The weight lifted (WeightSets only).
distance & distance_unit - The distance traveled (CardioSets only).
time & time_unit - The time submitted (CardioSets only).
calorie - This is the user-entered number of Cals burned (CardioSets only).
incline & incline_unit - The incline value (CardioSets only).
heart_rate & heart_rate_unit - The avg heart rate value (CardioSets only).
 */

public class ExerciseSet {
	private String createdAt;
	private int id;
	private int reps;
	private int setOrder;
	private SetType setType;
	private String loggedOn;
	private int caloriesBurned;
	private int workoutId;
	private int exerciseId;
	private String exerciseName;
	private float weight;
	private float distance;
	private float time;
	private float incline;
	private float heartRate;
	private int calorie;	//User entered calories burned (CardioSet only)
	private String weightUnit;
	private String distanceUnit;
	private String timeUnit;
	private String inclineUnit;
	private String heartRateUnit;
	
	public enum SetType {
		CardioSet,
		WeightSet
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getReps() {
		return reps;
	}

	public void setReps(int reps) {
		this.reps = reps;
	}

	public int getSetOrder() {
		return setOrder;
	}

	public void setSetOrder(int setOrder) {
		this.setOrder = setOrder;
	}

	public SetType getSetType() {
		return setType;
	}

	public void setSetType(SetType setType) {
		this.setType = setType;
	}

	public String getLoggedOn() {
		return loggedOn;
	}

	public void setLoggedOn(String loggedOn) {
		this.loggedOn = loggedOn;
	}

	public int getCaloriesBurned() {
		return caloriesBurned;
	}

	public void setCaloriesBurned(int caloriesBurned) {
		this.caloriesBurned = caloriesBurned;
	}

	public int getWorkoutId() {
		return workoutId;
	}

	public void setWorkoutId(int workoutId) {
		this.workoutId = workoutId;
	}

	public int getExerciseId() {
		return exerciseId;
	}

	public void setExerciseId(int exerciseId) {
		this.exerciseId = exerciseId;
	}

	public String getExerciseName() {
		return exerciseName;
	}

	public void setExerciseName(String exerciseName) {
		this.exerciseName = exerciseName;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public float getIncline() {
		return incline;
	}

	public void setIncline(float incline) {
		this.incline = incline;
	}

	public float getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(float heartRate) {
		this.heartRate = heartRate;
	}

	public int getCalorie() {
		return calorie;
	}

	public void setCalorie(int calorie) {
		this.calorie = calorie;
	}

	public String getWeightUnit() {
		return weightUnit;
	}

	public void setWeightUnit(String weightUnit) {
		this.weightUnit = weightUnit;
	}

	public String getDistanceUnit() {
		return distanceUnit;
	}

	public void setDistanceUnit(String distanceUnit) {
		this.distanceUnit = distanceUnit;
	}

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	public String getInclineUnit() {
		return inclineUnit;
	}

	public void setInclineUnit(String inclineUnit) {
		this.inclineUnit = inclineUnit;
	}

	public String getHeartRateUnit() {
		return heartRateUnit;
	}

	public void setHeartRateUnit(String heartRateUnit) {
		this.heartRateUnit = heartRateUnit;
	};
	
}
