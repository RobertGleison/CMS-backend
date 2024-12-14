package com.backend.Netflix;

import com.backend.Netflix.Torrent.TorrentManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.table.TableRowSorter;

@SpringBootApplication
public class NetflixApplication {
	public static TorrentManager torrentManager;
	public static void main(String[] args) {
		torrentManager = new TorrentManager();
		SpringApplication.run(NetflixApplication.class, args);
	}
}