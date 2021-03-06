/*
 *	 Copyright [2010] Stanley Ding(Dingshengyu)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Missian is based on hessian, mina and spring. Any project who uses 
 *	 missian must agree to hessian, mima and spring's license.
 *	  Hessian: http://hessian.caucho.com/
 *    Mina:http://mina.apache.org
 *	  Spring(Optional):http://www.springsource.org/	 
 *
 *   @author stanley
 *	 @date 2010-11-28
 */
package com.missian.common.util;


public class Constants {
	public static final String BEAN_NAME_CHARSET = "ASCII";
	public static final int DEFAULT_THREADPOOL_SIZE = 10;
	public static final int INIT_BUF_SIZE = 256;
	public static final String HTTP_HEADER_SEQ = "Missian-Sequence";
	public static final String HTTP_HEADER_BEANNAME = "Missian-Bean";
	public static final String HTTP_HEADER_METHOD = "Missian-Method";
	public static final String DECODER = "_DECODER_";
	public static final String ENCODER = "_ENCODER_";
}
