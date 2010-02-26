package com.nicknackhacks.dailyburn.api;


import com.nicknackhacks.dailyburn.model.Food;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FoodConverter implements Converter {

	public boolean canConvert(Class clazz) {
		return clazz.equals(Food.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		Food food = (Food) value;
		writer.startNode("brand");
		writer.setValue(food.getBrand());
		writer.endNode();
		writer.startNode("calories");
		writer.setValue(Integer.toString(food.getCalories()));
		writer.endNode();
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		Food food = new Food();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				if ("brand".equals(reader.getNodeName())) {
					food.setBrand(reader.getValue());
				} else if ("calories".equals(reader.getNodeName())) {
					food.setCalories(Integer.parseInt(reader.getValue()));
				} else if ("id".equals(reader.getNodeName())) {
					food.setId(Integer.parseInt(reader.getValue()));
				} else if ("name".equals(reader.getNodeName())) {
					food.setName(reader.getValue());
				} else if ("protein".equals(reader.getNodeName())) {
					food.setProtein(Float.parseFloat(reader.getValue()));
				} else if ("serving-size".equals(reader.getNodeName())) {
					food.setServingSize(reader.getValue());
				} else if ("total-carbs".equals(reader.getNodeName())) {
					food.setTotalCarbs(Float.parseFloat(reader.getValue()));
				} else if ("total-fat".equals(reader.getNodeName())) {
					food.setTotalFat(Float.parseFloat(reader.getValue()));
				} else if ("user-id".equals(reader.getNodeName())) {
					food.setUserId(Integer.parseInt(reader.getValue()));
				} else if ("thumb-url".equals(reader.getNodeName())) {
					food.setThumbUrl(reader.getValue());
				} else if ("usda".equals(reader.getNodeName())) {
					food.setUsda(Boolean.parseBoolean(reader.getValue()));
				}
				reader.moveUp();
			}
		}
		return food;
	}
}