package org.nicknack.dailyburn.api;

import org.nicknack.dailyburn.model.FoodLogEntry;

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
		//FoodLogEntry entries = new FoodLogEntries();
		FoodLogEntry entry = new FoodLogEntry();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				if ("created-at".equals(reader.getNodeName())) {
					entry.setCreatedAt(reader.getValue());
				} else if ("food-id".equals(reader.getNodeName())) {
					entry.setFoodId(Integer.parseInt(reader.getValue()));
				} else if ("id".equals(reader.getNodeName())) {
					entry.setId(Integer.parseInt(reader.getValue()));
				} else if ("logged-on".equals(reader.getNodeName())) {
					entry.setLoggedOn(reader.getValue());
				} else if ("servings-eaten".equals(reader.getNodeName())) {
					entry.setServingsEaten(Float.parseFloat(reader.getValue()));
				} else if ("user-id".equals(reader.getNodeName())) {
					entry.setUserId(Integer.parseInt(reader.getValue()));
				} else if ("calories-eaten".equals(reader.getNodeName())) {
					entry.setCaloriesEaten(Float.parseFloat(reader.getValue()));
				} else if ("total-fat-eaten".equals(reader.getNodeName())) {
					entry.setTotalFatEaten(Float.parseFloat(reader.getValue()));
				} else if ("total-carbs-eaten".equals(reader.getNodeName())) {
					entry.setTotalCarbsEaten(Float.parseFloat(reader.getValue()));
				} else if ("protein-eaten".equals(reader.getNodeName())) {
					entry.setProteinEaten(Float.parseFloat(reader.getValue()));
				} else if ("food-name".equals(reader.getNodeName())) {
					entry.setFoodName(reader.getValue());
				} else if ("food-picture-url".equals(reader.getNodeName())) {
					entry.setFoodPictureUrl(reader.getValue());
				} else if ("fiber-eaten".equals(reader.getNodeName())) {
					entry.setFiberEaten(Float.parseFloat(reader.getValue()));
				} else if ("sodium-eaten".equals(reader.getNodeName())) {
					entry.setSodiumEaten(Float.parseFloat(reader.getValue()));
				} else if ("cholesterol-eaten".equals(reader.getNodeName())) {
					entry.setCholesterolEaten(Float.parseFloat(reader.getValue()));
				} else if ("potassium-eaten".equals(reader.getNodeName())) {
					entry.setPotassiumEaten(Float.parseFloat(reader.getValue()));
				}
				reader.moveUp();
			}
		}
		return entry;
	}
}