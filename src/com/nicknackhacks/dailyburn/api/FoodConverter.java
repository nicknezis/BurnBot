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
		String nodeName = null;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				nodeName = reader.getNodeName();
				if ("brand".equals(nodeName)) {
					food.setBrand(reader.getValue());
				} else if ("calories".equals(nodeName)) {
					food.setCalories(Integer.parseInt(reader.getValue()));
				} else if ("id".equals(nodeName)) {
					food.setId(Integer.parseInt(reader.getValue()));
				} else if ("name".equals(nodeName)) {
					food.setName(reader.getValue());
				} else if ("protein".equals(nodeName)) {
					food.setProtein(Float.parseFloat(reader.getValue()));
				} else if ("serving-size".equals(nodeName)) {
					food.setServingSize(reader.getValue());
				} else if ("total-carbs".equals(nodeName)) {
					food.setTotalCarbs(Float.parseFloat(reader.getValue()));
				} else if ("total-fat".equals(nodeName)) {
					food.setTotalFat(Float.parseFloat(reader.getValue()));
				} else if ("user-id".equals(nodeName)) {
					food.setUserId(Integer.parseInt(reader.getValue()));
				} else if ("thumb-url".equals(nodeName)) {
					food.setThumbUrl(reader.getValue());
				} else if ("usda".equals(nodeName)) {
					food.setUsda(Boolean.parseBoolean(reader.getValue()));
				}
				reader.moveUp();
			}
		}
		return food;
	}
}