package com.nicknackhacks.dailyburn.api;


import com.nicknackhacks.dailyburn.model.BodyLogEntry;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class BodyLogEntryConverter implements Converter {

	public boolean canConvert(Class clazz) {
		return clazz.equals(BodyLogEntry.class);
	}

	//TODO: Fix
	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		BodyLogEntry entry = (BodyLogEntry) value;
		writer.startNode("id");
		writer.addAttribute("type", "integer");
		writer.setValue(Integer.toString(entry.getId()));
		writer.endNode();
		writer.startNode("logged-on");
		writer.setValue(entry.getLoggedOn());
		writer.endNode();
		writer.startNode("user-id");
		writer.setValue(Integer.toString(entry.getUserId()));
		writer.endNode();
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		BodyLogEntry entry = new BodyLogEntry();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				if ("id".equals(reader.getNodeName())) {
					entry.setId(Integer.parseInt(reader.getValue()));
				} else if ("logged-on".equals(reader.getNodeName())) {
					entry.setLoggedOn(reader.getValue());
				} else if ("user-id".equals(reader.getNodeName())) {
					entry.setUserId(Integer.parseInt(reader.getValue()));
				} else if ("body-metric-identifier".equals(reader.getNodeName())) {
					entry.setMetricIdentifier(reader.getValue());
				} else if ("unit".equals(reader.getNodeName())) {
					entry.setUnit(reader.getValue());
				} else if ("value".equals(reader.getNodeName())) {
					entry.setValue(Float.parseFloat(reader.getValue()));
				}
				reader.moveUp();
			}
		}
		return entry;
	}
}