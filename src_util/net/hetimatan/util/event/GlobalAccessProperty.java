package net.hetimatan.util.event;

import java.util.Properties;

public class GlobalAccessProperty {
	public static GlobalAccessProperty sInstance = new GlobalAccessProperty();

	public static GlobalAccessProperty getInstance() {
		return sInstance;
	}

	private Properties mProp = new Properties();

	private GlobalAccessProperty() {
	}

	public void put(String key, String value) {
		mProp.put(key, value);
	}

	public String get(String key, String def) {
		if(mProp.containsKey(key)) {
			return mProp.getProperty(key);
		} else {
			return def;
		}
	}
}
