package com.nicknackhacks.dailyburn.model;

//D/BurnBot ( 3458): <meal-names type="array">
//D/BurnBot ( 3458):   <meal-name>
//D/BurnBot ( 3458):     <id type="integer">1</id>
//D/BurnBot ( 3458):     <name>Breakfast</name>
//D/BurnBot ( 3458):   </meal-name>
//D/BurnBot ( 3458):   <meal-name>
//D/BurnBot ( 3458):     <id type="integer">2</id>
//D/BurnBot ( 3458):     <name>Morning Snack</name>
//D/BurnBot ( 3458):   </meal-name>
//D/BurnBot ( 3458):   <meal-name>
//D/BurnBot ( 3458):     <id type="integer">3</id>
//D/BurnBot ( 3458):     <name>Lunch</name>
//D/BurnBot ( 3458):   </meal-name>
//D/BurnBot ( 3458):   <meal-name>
//D/BurnBot ( 3458):     <id type="integer">4</id>
//D/BurnBot ( 3458):     <name>Afternoon Snack</name>
//D/BurnBot ( 3458):   </meal-name>
//D/BurnBot ( 3458):   <meal-name>
//D/BurnBot ( 3458):     <id type="integer">5</id>
//D/BurnBot ( 3458):     <name>Dinner</name>
//D/BurnBot ( 3458):   </meal-name>
//D/BurnBot ( 3458):   <meal-name>
//D/BurnBot ( 3458):     <id type="integer">6</id>
//D/BurnBot ( 3458):     <name>Evening Snack</name>
//D/BurnBot ( 3458):   </meal-name>
//D/BurnBot ( 3458): </meal-names>

public class MealName {

	private int id;
	private String name;

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
	
	@Override
	public String toString() {
		return name;
	}
}
