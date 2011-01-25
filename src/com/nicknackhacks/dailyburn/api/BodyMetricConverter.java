package com.nicknackhacks.dailyburn.api;


import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class BodyMetricConverter implements Converter {

	public boolean canConvert(Class clazz) {
		return clazz.equals(BodyMetric.class);
	}

	//TODO: Fix
	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		BodyMetric metric = (BodyMetric) value;
		writer.startNode("id");
		writer.addAttribute("type", "integer");
		writer.setValue(Integer.toString(metric.getId()));
		writer.endNode();
		writer.startNode("name");
		writer.setValue(metric.getName());
		writer.endNode();
		writer.startNode("pro");
		writer.setValue(Boolean.toString(metric.isPro()));
		writer.endNode();
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		BodyMetric metric = new BodyMetric();
		String nodeName = null;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				nodeName = reader.getNodeName();
				if ("id".equals(nodeName)) {
					metric.setId(Integer.parseInt(reader.getValue()));
				} else if ("name".equals(nodeName)) {
					metric.setName(reader.getValue());
				} else if ("pro".equals(nodeName)) {
					metric.setPro(Boolean.parseBoolean(reader.getValue()));
				} else if ("body-metric-identifier".equals(nodeName)) {
					metric.setMetricIdentifier(reader.getValue());
				} else if ("unit".equals(nodeName)) {
					metric.setUnit(reader.getValue());
				}
				reader.moveUp();
			}
		}
		return metric;
	}
}