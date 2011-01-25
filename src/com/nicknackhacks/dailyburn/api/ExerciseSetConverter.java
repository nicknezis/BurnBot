package com.nicknackhacks.dailyburn.api;


import com.nicknackhacks.dailyburn.model.ExerciseSet;
import com.nicknackhacks.dailyburn.model.ExerciseSet.SetType;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ExerciseSetConverter implements Converter {

	
	public boolean canConvert(Class clazz) {
		return clazz.equals(ExerciseSet.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		//TODO: Implement this method if needed
	}
	
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		//FoodLogEntry entries = new FoodLogEntries();
		String nodeName = null;
		ExerciseSet set = new ExerciseSet();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				nodeName = reader.getNodeName();
				if ("created-at".equals(nodeName)) {
					set.setCreatedAt(reader.getValue());
				} else if ("id".equals(nodeName)) {
					set.setId(Integer.parseInt(reader.getValue()));
				} else if ("reps".equals(nodeName)) {
					set.setReps(Integer.parseInt(reader.getValue()));
				} else if ("set-order".equals(nodeName)) {
					set.setSetOrder(Integer.parseInt(reader.getValue()));
				} else if ("set-type".equals(nodeName)) {
					if("CardioSet".contentEquals(reader.getValue())) {
						set.setSetType(SetType.CardioSet);						
					} else if("WeightSet".contentEquals(reader.getValue())) {
						set.setSetType(SetType.WeightSet);
					}
				} else if ("logged-on".equals(nodeName)) {
					set.setLoggedOn(reader.getValue());
				} else if ("calories-burned".equals(nodeName)) {
					set.setCaloriesBurned(Integer.parseInt(reader.getValue()));
				} else if ("workout-id".equals(nodeName)) {
					set.setWorkoutId(Integer.parseInt(reader.getValue()));
				} else if ("exercise-id".equals(nodeName)) {
					set.setExerciseId(Integer.parseInt(reader.getValue()));
				} else if ("exercise-name".equals(nodeName)) {
					set.setExerciseName(reader.getValue());
				} else if ("weight".equals(nodeName)) {
					set.setWeight(Float.parseFloat(reader.getValue()));
				} else if ("distance".equals(nodeName)) {
					set.setDistance(Float.parseFloat(reader.getValue()));
				} else if ("time".equals(nodeName)) {
					set.setTime(Float.parseFloat(reader.getValue()));
				} else if ("incline".equals(nodeName)) {
					set.setIncline(Float.parseFloat(reader.getValue()));
				} else if ("heart-rate".equals(nodeName)) {
					set.setHeartRate(Float.parseFloat(reader.getValue()));
				} else if ("calorie".equals(nodeName)) {
					set.setCalorie(Integer.parseInt(reader.getValue()));
				} else if ("weight-unit".equals(nodeName)) {
					set.setWeightUnit(reader.getValue());
				} else if ("distance-unit".equals(nodeName)) {
					set.setDistanceUnit(reader.getValue());
				} else if ("time-unit".equals(nodeName)) {
					set.setTimeUnit(reader.getValue());
				} else if ("incline-unit".equals(nodeName)) {
					set.setInclineUnit(nodeName);
				} else if ("heart-rate-unit".equals(nodeName)) {
					set.setHeartRateUnit(reader.getValue());
				}
				reader.moveUp();
			}
		}
		return set;
	}
}