package com.nbodev.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

	private final Logger logger = LoggerFactory.getLogger(DemoController.class);

	@GetMapping("/")
	public ResponseEntity<String> info() throws UnknownHostException {

		try {
			final var sb = new StringBuilder();
			sb.append("hostname: ");
			sb.append(InetAddress.getLocalHost().getHostName());
			sb.append(" | ");
			sb.append("ip address: ");
			sb.append(InetAddress.getLocalHost().getHostAddress());
			sb.append(" | ");
			sb.append("time: ");
			sb.append(LocalDateTime.now());
			return ResponseEntity.ok(sb.toString());
		} catch (final UnknownHostException e) {
			logger.error("Issue while calling info.", e);
			throw e;
		}

	}

}
