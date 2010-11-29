/**
 * 
 */
package com.missian.example.client.hessian;

import java.io.IOException;

import com.caucho.hessian.client.HessianProxyFactory;
import com.missian.example.bean.Hello;

public class HessianClient {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String url = "http://localhost:1235/hello";

		HessianProxyFactory factory = new HessianProxyFactory();
		Hello basic = (Hello) factory.create(Hello.class, url);

		System.out.println("hello(): " + basic.hello("aa", 123));
		System.out.println("hello(): " + basic.hello("aa", 123));
		System.out.println("hello(): " + basic.hello("aa", 123));
		System.out.println("hello(): " + basic.hello("aa", 123));
		System.out.println("hello(): " + basic.hello("aa", 123));
	}
}
