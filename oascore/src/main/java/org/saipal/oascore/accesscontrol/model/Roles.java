package org.saipal.oascore.accesscontrol.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Roles {
	
	public String id;
	public String name;
	public String display_name;
	
	public void loadData(RequestParser doc) {
		for (Field f : this.getClass().getFields()) {
			String fname = f.getName();
			try {
				f.set(this, doc.getElementById(fname).value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<String> searchables() {
		return Arrays.asList("name","display_name");
	}
	
	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("name", "required");
		rules.put("display_name", "required");
		return rules;
	}


}
