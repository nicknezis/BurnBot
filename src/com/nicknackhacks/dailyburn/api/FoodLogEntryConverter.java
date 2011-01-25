package com.nicknackhacks.dailyburn.api;


import com.nicknackhacks.dailyburn.model.FoodLogEntry;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FoodLogEntryConverter implements Converter {

	public boolean canConvert(Class clazz) {
		return clazz.equals(FoodLogEntry.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		//TODO: Implement this method if needed
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		FoodLogEntry entry = new FoodLogEntry();
		String nodeName = null;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				nodeName = reader.getNodeName();
				if ("created-at".equals(nodeName)) {
					entry.setCreatedAt(reader.getValue());
				} else if ("food-id".equals(nodeName)) {
					entry.setFoodId(Integer.parseInt(reader.getValue()));
				} else if ("id".equals(nodeName)) {
					entry.setId(Integer.parseInt(reader.getValue()));
				} else if ("meal-name-id".equals(nodeName)) {
					entry.setMealId(Integer.parseInt(reader.getValue()));
				} else if ("logged-on".equals(nodeName)) {
					entry.setLoggedOn(reader.getValue());
				} else if ("servings-eaten".equals(nodeName)) {
					entry.setServingsEaten(Float.parseFloat(reader.getValue()));
				} else if ("user-id".equals(nodeName)) {
					entry.setUserId(Integer.parseInt(reader.getValue()));
				} else if ("calories-eaten".equals(nodeName)) {
					entry.setCaloriesEaten(Float.parseFloat(reader.getValue()));
				} else if ("total-fat-eaten".equals(nodeName)) {
					entry.setTotalFatEaten(Float.parseFloat(reader.getValue()));
				} else if ("total-carbs-eaten".equals(nodeName)) {
					entry.setTotalCarbsEaten(Float.parseFloat(reader.getValue()));
				} else if ("protein-eaten".equals(nodeName)) {
					entry.setProteinEaten(Float.parseFloat(reader.getValue()));
				} else if ("food-name".equals(nodeName)) {
					entry.setFoodName(reader.getValue());
				} else if ("food-picture-url".equals(nodeName)) {
					entry.setFoodPictureUrl(reader.getValue());
				} else if ("fiber-eaten".equals(nodeName)) {
					entry.setFiberEaten(Float.parseFloat(reader.getValue()));
				} else if ("sodium-eaten".equals(nodeName)) {
					entry.setSodiumEaten(Float.parseFloat(reader.getValue()));
				} else if ("cholesterol-eaten".equals(nodeName)) {
					entry.setCholesterolEaten(Float.parseFloat(reader.getValue()));
				} else if ("potassium-eaten".equals(nodeName)) {
					entry.setPotassiumEaten(Float.parseFloat(reader.getValue()));
				}
				reader.moveUp();
			}
		}
		return entry;
	}
}