package com.nicknackhacks.dailyburn.model;

import android.database.Cursor;

import com.nicknackhacks.dailyburn.provider.BurnBotContract.UserContract;

public class User {
	private int id;
	private String username;
	private boolean usesMetricWeights;
	private boolean usesMetricDistances;
	private int calGoalsMetInPastWeek;
	private int daysExercisedInPastWeek;
	private String pictureUrl;
	private String url;
	private String sex;
	private int caloriesBurned;
	private int caloriesConsumed;
	private float bodyWeight;
	private float bodyWeightGoal;
	private boolean pro;
	private String createdAt;
	private boolean dynamicDietGoals;

	public User(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex(UserContract.USER_ID)));
		setUsername(cursor.getString(cursor
				.getColumnIndex(UserContract.USER_NAME)));
		setUsesMetricWeights(cursor.getInt(cursor
				.getColumnIndex(UserContract.USER_METRIC_WEIGHTS)) == 0 ? false
				: true);
		setUsesMetricDistances(cursor.getInt(cursor
				.getColumnIndex(UserContract.USER_METRIC_DISTANCE)) == 0 ? false
				: true);
		setCalGoalsMetInPastWeek(cursor.getInt(cursor
				.getColumnIndex(UserContract.USER_CAL_GOALS_MET)));
		setDaysExercisedInPastWeek(cursor.getInt(cursor
				.getColumnIndex(UserContract.USER_DAYS_EXERCISED)));
		setPictureUrl(cursor.getString(cursor
				.getColumnIndex(UserContract.USER_PICTURE_URL)));
		setUrl(cursor.getString(cursor.getColumnIndex(UserContract.USER_URL)));
		setCaloriesBurned(cursor.getInt(cursor
				.getColumnIndex(UserContract.USER_CAL_BURNED)));
		setCaloriesConsumed(cursor.getInt(cursor
				.getColumnIndex(UserContract.USER_CAL_CONSUMED)));
		setBodyWeight(cursor.getFloat(cursor
				.getColumnIndex(UserContract.USER_BODY_WEIGHT)));
		setBodyWeightGoal(cursor.getFloat(cursor
				.getColumnIndex(UserContract.USER_BODY_WEIGHT_GOAL)));
		setPro(cursor.getInt(cursor.getColumnIndex(UserContract.USER_PRO)) == 0 ? false
				: true);
		setCreatedAt(cursor.getString(cursor
				.getColumnIndex(UserContract.USER_CREATED_AT)));
		setSex(cursor.getString(cursor.getColumnIndex(UserContract.USER_SEX)));
		setDynamicDietGoals(cursor.getInt(cursor
				.getColumnIndex(UserContract.USER_DYN_DIET_GOALS)) == 0 ? false
				: true);
	}

	public User() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public boolean isDynamicDietGoals() {
		return dynamicDietGoals;
	}

	public void setDynamicDietGoals(boolean dynamicDietGoals) {
		this.dynamicDietGoals = dynamicDietGoals;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}
}
