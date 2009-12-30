package org.nicknack.dailyburn.domain;

/*
  <?xml version="1.0" encoding="UTF-8"?>
    <food-log-entries type="array">
      <food-log-entry>
        <created-at type="datetime">2009-08-04T14:58:09-06:00</created-at>
        <food-id type="integer">40219</food-id>
        <id type="integer">655900</id>
        <logged-on type="date">2009-08-04</logged-on>
        <servings-eaten type="float">1.0</servings-eaten>
        <user-id type="integer">1</user-id>
        <calories-eaten type="float">180.0</calories-eaten>
        <total-fat-eaten type="float">2.0</total-fat-eaten>
        <total-carbs-eaten type="float">27.0</total-carbs-eaten>
        <protein-eaten type="float">14.0</protein-eaten>
        <food-name>Zone bar: Apple Cinnamon</food-name>
        <food-picture-url>/images/fu/0002/2677/frtzonebrorgcran_normal.jpg</food-picture-url>
        <fiber-eaten type="float">3.0</fiber-eaten>
        <sodium-eaten type="float">140.0</sodium-eaten>
        <cholesterol-eaten type="float">0.0</cholesterol-eaten>
        <potassium-eaten type="float">160.0</potassium-eaten>
      </food-log-entry>
    </food-log-entries>
 */
public class FoodLogEntry {
	//TODO: Check if this can be 'datetime' with xstream
	private String createdAt;
	private int foodId;
	private int id;
	//TODO: Check if this can be 'date' with xstream
	private String loggedOn;
	private float servingsEaten;
	private int userId;
	private float caloriesEaten;
	private float totalFatEaten;
	private float totalCarbsEaten;
	private float proteinEaten;
	private String foodName;
	private String foodPictureUrl;
	private float fiberEaten;
	private float sodiumEaten;
	private float cholesterolEaten;
	private float potassiumEaten;
	
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public int getFoodId() {
		return foodId;
	}
	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}
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
	public float getServingsEaten() {
		return servingsEaten;
	}
	public void setServingsEaten(float servingsEaten) {
		this.servingsEaten = servingsEaten;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public float getCaloriesEaten() {
		return caloriesEaten;
	}
	public void setCaloriesEaten(float caloriesEaten) {
		this.caloriesEaten = caloriesEaten;
	}
	public float getTotalFatEaten() {
		return totalFatEaten;
	}
	public void setTotalFatEaten(float totalFatEaten) {
		this.totalFatEaten = totalFatEaten;
	}
	public float getTotalCarbsEaten() {
		return totalCarbsEaten;
	}
	public void setTotalCarbsEaten(float totalCarbsEaten) {
		this.totalCarbsEaten = totalCarbsEaten;
	}
	public float getProteinEaten() {
		return proteinEaten;
	}
	public void setProteinEaten(float proteinEaten) {
		this.proteinEaten = proteinEaten;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public String getFoodPictureUrl() {
		return foodPictureUrl;
	}
	public void setFoodPictureUrl(String foodPictureUrl) {
		this.foodPictureUrl = foodPictureUrl;
	}
	public float getFiberEaten() {
		return fiberEaten;
	}
	public void setFiberEaten(float fiberEaten) {
		this.fiberEaten = fiberEaten;
	}
	public float getSodiumEaten() {
		return sodiumEaten;
	}
	public void setSodiumEaten(float sodiumEaten) {
		this.sodiumEaten = sodiumEaten;
	}
	public float getCholesterolEaten() {
		return cholesterolEaten;
	}
	public void setCholesterolEaten(float cholesterolEaten) {
		this.cholesterolEaten = cholesterolEaten;
	}
	public float getPotassiumEaten() {
		return potassiumEaten;
	}
	public void setPotassiumEaten(float potassiumEaten) {
		this.potassiumEaten = potassiumEaten;
	}
}
