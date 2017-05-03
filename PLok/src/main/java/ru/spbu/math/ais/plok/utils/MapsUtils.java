package ru.spbu.math.ais.plok.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapsUtils {
	
	public static Map<String, Object> flatten(Map<String, Object> map) {
		return flattenTo(new LinkedHashMap<String, Object>(), map);
	}
	
	private static Map<String, Object> flattenTo(Map<String, Object> result, Map<String, Object> map){
		for (Entry<String, Object> entry : map.entrySet()){
			if (entry.getValue() instanceof Map) {
				result.putAll(flatten((Map<String, Object>) entry.getValue()));
			}else{
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

}
