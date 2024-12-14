package com.backend.Netflix;

import com.backend.Netflix.torrent.TorrentManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NetflixApplication {
	public static void main(String[] args) {

		TorrentManager torrentManager = new TorrentManager();
		torrentManager.run();


		SpringApplication.run(NetflixApplication.class, args);
	}
}