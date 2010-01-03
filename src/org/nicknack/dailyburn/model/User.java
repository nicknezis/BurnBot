package org.nicknack.dailyburn.model;
/*
<?xml version="1.0" encoding="UTF-8"?>
<user>
  <id type="integer">123</id>
  <time-zone>Mountain Time (US &amp; Canada)</time-zone>
  <username>StephenB</username>
  <uses-metric-weights type="boolean">false</uses-metric-weights>
  <uses-metric-distances type="boolean">false</uses-metric-distances>
  <cal-goals-met-in-past-week type="integer">3</cal-goals-met-in-past-week>
  <days-exercised-in-past-week type="integer">1</days-exercised-in-past-week>
  <picture-url>/images/fu/0003/2674/me_normal.png</picture-url>
  <url>/locker_room/stephenb</url>
  <calories-burned type="integer">0</calories-burned>
  <calories-consumed type="integer">0</calories-consumed>
  <body-weight type="decimal">186.0</body-weight>
  <body-weight-goal type="decimal">175.0</body-weight-goal>
  <pro type="boolean">true</pro>
  <created-at>2007-08-02 18:00:00 -0600</created-at>
</user>
*/
public class User {
	private int id; //  <id type="integer">123</id>
	private String timeZone; //<time-zone>Mountain Time (US &amp; Canada)</time-zone>
	private String username; //<username>StephenB</username>
	private boolean usesMetricWeights; //<uses-metric-weights type="boolean">false</uses-metric-weights>
	private boolean usesMetricDistances; //<uses-metric-distances type="boolean">false</uses-metric-distances>
	private int calGoalsMetInPastWeek; //<cal-goals-met-in-past-week type="integer">3</cal-goals-met-in-past-week>
	private int daysExercisedInPastWeek; //<days-exercised-in-past-week type="integer">1</days-exercised-in-past-week>
	private String pictureUrl; //<picture-url>/images/fu/0003/2674/me_normal.png</picture-url>
	private String url; //<url>/locker_room/stephenb</url>
	private int caloriesBurned; //<calories-burned type="integer">0</calories-burned>
	private int caloriesConsumed; //<calories-consumed type="integer">0</calories-consumed>
	private float bodyWeight; //<body-weight type="decimal">186.0</body-weight>
	private float bodyWeightGoal; //<body-weight-goal type="decimal">175.0</body-weight-goal>
	private boolean pro; //<pro type="boolean">true</pro>
	private String createdAt; //<created-at>2007-08-02 18:00:00 -0600</created-at>
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTimezone() {
		return timeZone;
	}
	public void setTimezone(String timezone) {
		this.timeZone = timezone;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public boolean isUsesMetricWeights() {
		return usesMetricWeights;
	}
	public void setUsesMetricWeights(boolean usesMetricWeights) {
		this.usesMetricWeights = usesMetricWeights;
	}
	public boolean isUsesMetricDistances() {
		return usesMetricDistances;
	}
	public void setUsesMetricDistances(boolean usesMetricDistances) {
		this.usesMetricDistances = usesMetricDistances;
	}
	public int getCalGoalsMetInPastWeek() {
		return calGoalsMetInPastWeek;
	}
	public void setCalGoalsMetInPastWeek(int calGoalsMetInPastWeek) {
		this.calGoalsMetInPastWeek = calGoalsMetInPastWeek;
	}
	public int getDaysExercisedInPastWeek() {
		return daysExercisedInPastWeek;
	}
	public void setDaysExercisedInPastWeek(int daysExercisedInPastWeek) {
		this.daysExercisedInPastWeek = daysExercisedInPastWeek;
	}
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getCaloriesBurned() {
		return caloriesBurned;
	}
	public void setCaloriesBurned(int caloriesBurned) {
		this.caloriesBurned = caloriesBurned;
	}
	public int getCaloriesConsumed() {
		return caloriesConsumed;
	}
	public void setCaloriesConsumed(int caloriesConsumed) {
		this.caloriesConsumed = caloriesConsumed;
	}
	public float getBodyWeight() {
		return bodyWeight;
	}
	public void setBodyWeight(float bodyWeight) {
		this.bodyWeight = bodyWeight;
	}
	public float getBodyWeightGoal() {
		return bodyWeightGoal;
	}
	public void setBodyWeightGoal(float bodyWeightGoal) {
		this.bodyWeightGoal = bodyWeightGoal;
	}
	public boolean isPro() {
		return pro;
	}
	public void setPro(boolean pro) {
		this.pro = pro;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
