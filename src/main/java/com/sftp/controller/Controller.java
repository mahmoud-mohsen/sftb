package com.sftp.controller;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sftp.SftpConfig.UploadGateway;

@RestController
public class Controller {
	private final static Logger log = LoggerFactory.getLogger(Controller.class);

	@Autowired
	UploadGateway uploadGateway;

	@PostMapping("upload")
	public boolean uploadFile() throws Exception {
		ChannelSftp channelSftp = setupJsch();
		channelSftp.connect();

		File file = new File("sample.txt");
		uploadGateway.upload(file);
		try {
			uploadGateway.upload(file);

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error when uploading file", e);
		}

		channelSftp.exit();
		return true;

	}

	@PostMapping("upload1")
	public boolean uploadFile1() throws Exception {
		ChannelSftp channelSftp = setupJsch();
		channelSftp.connect();

		String localFile = "sample.txt";
		try {
			channelSftp.put(localFile, "upload/sample.txt");

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error when uploading file", e);
		}

		channelSftp.exit();
		return true;

	}

	@PostMapping("download")
	public boolean downloadFile() throws Exception {
		ChannelSftp channelSftp = setupJsch();
		channelSftp.connect();

		String localFile = "sample1.txt";

		try {
			InputStream inputStream = channelSftp.get("/upload/sample.txt");
			OutputStream outputStream = null;

			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			System.out.println(outputStream);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error when downloading file", e);
		}

		channelSftp.exit();
		return true;

	}

	private String remoteHost = "192.168.8.95";
	private String username = "foo";
	private String password = "pass";

	private ChannelSftp setupJsch() throws JSchException {
		JSch jsch = new JSch();
		Session jschSession = jsch.getSession(username, remoteHost, 2222);
		jsch.setKnownHosts("known_hosts");
		jschSession.setPassword(password);

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		config.put("PreferredAuthentications", "password");

		jschSession.setConfig(config);
		jschSession.connect(5000);
		return (ChannelSftp) jschSession.openChannel("sftp");
	}

}
