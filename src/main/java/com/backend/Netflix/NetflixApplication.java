package com.backend.Netflix;

import com.backend.Netflix.Torrent.TorrentManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class NetflixApplication {
	public static TorrentManager torrentManager;
	public static void main(String[] args) {
		torrentManager = new TorrentManager();
		SpringApplication.run(NetflixApplication.class, args);
	}
}