/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.nr.logging.mparticle.LogStreamHandler;

public class Main {

	public static void main(String[] args) {
		final String requestDir = "jsonRequests";
		LogStreamHandler handler = new LogStreamHandler();
		File sampleDir = new File(requestDir);
		for (String sample : sampleDir.list()) {
			String sampleFile = sampleDir.getPath() + "/" + sample;
			try {
				System.out.println(String.format("BEGIN: %s",
						sampleFile));
				InputStream targetStream = new FileInputStream(sampleFile);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				handler.handleRequest(targetStream,
						baos,
						null);
				System.out.println(String.format("Main.main: result %s",
						baos.toString()));
			} catch (Exception e) {
				System.err.println(String.format("Main.main: %s",
						e.getMessage()));
			} finally {
				System.out.println(String.format("END  : %s\n",
						sampleFile));
			}
		}
	}

	public static void main2(String[] args) throws IOException {
		LogStreamHandler handler = new LogStreamHandler();
		Files.lines(Paths.get("jsonRequests/mparticle_sample.txt"))
				.forEach(line -> {
					InputStream targetStream = new ByteArrayInputStream(line.getBytes());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try {
						handler.handleRequest(targetStream,
								baos,
								null);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(String.format("Main.main: result %s",
							baos.toString()));
				});
	}
}