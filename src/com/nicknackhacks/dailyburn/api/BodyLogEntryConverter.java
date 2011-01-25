package com.nicknackhacks.dailyburn.api;


import com.nicknackhacks.dailyburn.model.BodyLogEntry;
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
		String nodeName = null;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Boolean.parseBoolean(reader.getAttribute("nil"))) {
				reader.moveUp();
			} else {
				nodeName = reader.getNodeName();
				if ("id".equals(nodeName)) {
					entry.setId(Integer.parseInt(reader.getValue()));
				} else if ("logged-on".equals(nodeName)) {
					entry.setLoggedOn(reader.getValue());
				} else if ("user-id".equals(nodeName)) {
					entry.setUserId(Integer.parseInt(reader.getValue()));
				} else if ("body-metric-identifier".equals(nodeName)) {
					entry.setMetricIdentifier(reader.getValue());
				} else if ("unit".equals(nodeName)) {
					entry.setUnit(reader.getValue());
				} else if ("value".equals(nodeName)) {
					entry.setValue(Float.parseFloat(reader.getValue()));
				}
				reader.moveUp();
			}
		}
		return entry;
	}
}