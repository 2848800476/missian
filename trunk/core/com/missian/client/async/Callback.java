package com.missian.client.async;

public interface Callback{
	void call(Object value) throws Exception;
	Class<?> getAcceptValueType();
}
