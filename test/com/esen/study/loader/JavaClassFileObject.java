package com.esen.study.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class JavaClassFileObject extends SimpleJavaFileObject {
	//用于存储class字节
	ByteArrayOutputStream outputStream;

	public JavaClassFileObject(String className, Kind kind) {
		super(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind);
		outputStream = new ByteArrayOutputStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return outputStream;
	}

	public byte[] getClassBytes() {
		return outputStream.toByteArray();
	}
}
