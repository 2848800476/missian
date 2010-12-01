package com.missian.client.async.message;

import org.apache.mina.core.buffer.IoBuffer;

import com.missian.common.io.MissianMessage;

public class AsyncClientRequest extends MissianMessage{
	private IoBuffer outputBuffer;
	private String host;
	private int port;
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public IoBuffer getOutputBuffer() {
		return outputBuffer;
	}

	public void setOutputBuffer(IoBuffer outputBuffer) {
		this.outputBuffer = outputBuffer;
	}
}
