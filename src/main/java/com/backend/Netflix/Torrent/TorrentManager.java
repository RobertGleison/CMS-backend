package com.backend.Netflix.Torrent;

import org.libtorrent4j.*;
import org.libtorrent4j.alerts.Alert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import  java.nio.file.Files;

public class TorrentManager {
    public static SessionManager sessionManager = new SessionManager();
    List<TorrentHandle> RunningTorrents = new ArrayList<TorrentHandle>();
    String trackerIP;
    public void Torrent(){
        sessionManager = new SessionManager();

        sessionManager.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                System.out.println(alert);
            }
        });
    }
    //moviePath é o caminho do arquivo mp4 a ser convertido está no bucket mas montaremos o bucket em /mnt/bucket
    // então será algo do tipo /mnt/bucket/avatar/avatarHD.mp4
    public File createTorrent(File moviePath, String outputFileName) {
        TorrentBuilder torrentBuilder = new TorrentBuilder().addTracker(trackerIP).setPrivate(true).path(moviePath);
        byte[] torrent;
        try {
        TorrentBuilder.Result result = torrentBuilder.generate();
        torrent = result.entry().bencode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File outfile;
        Path outputPath = Path.of(moviePath.getParentFile().toString(), outputFileName + ".torrent");
        TorrentInfo ti = new TorrentInfo(torrent);
        sessionManager.download(ti, moviePath);
        TorrentHandle handle = sessionManager.find(ti.infoHash());
        RunningTorrents.add(handle);
        handle.forceReannounce(1);
        try {
            outfile = Files.write(outputPath,torrent).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  outfile;
    }
    public  void reannounceAllTorrents(){
        for (TorrentHandle handle : RunningTorrents){
            handle.forceReannounce();
        }
    }
}