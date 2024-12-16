package com.backend.Netflix.Torrent;


import com.frostwire.jlibtorrent.*;
import com.frostwire.jlibtorrent.alerts.Alert;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import  java.nio.file.Files;

public class TorrentManager {
    public SessionManager sessionManager;
    List<TorrentHandle> RunningTorrents = new ArrayList<TorrentHandle>();
    String trackerUrl = "http://netflixppup.duckdns.org:8000/announce";
    String localUrl = "http://localhost:8000/announce";
    public TorrentManager(){
        sessionManager = new SessionManager();
        sessionManager.stopDht();
        sessionManager.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                System.out.println("Torrent alerts: " + alert);
            }
        });
        sessionManager.start();
    }


    // dirpath tem que ser: mnt/bucket/avatar
    // moviename tem que ser HD_video.mp4
    public byte[] createTorrent(String dirPath, String moviename) {
        System.out.println("Enter in create torrent of TorrentManager");
        //moviepath: /mnt/bucket/avatar/HD_video.mp4
        File moviePath = new File(dirPath, moviename);
        System.out.println("movie: " + moviePath);
        TorrentBuilder torrentBuilder = new TorrentBuilder().addTracker(trackerUrl).setPrivate(true).path(moviePath).addTracker(localUrl);
        byte[] torrent;
        try {
            TorrentBuilder.Result result = torrentBuilder.generate();
            torrent = result.entry().bencode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TorrentInfo ti = new TorrentInfo(torrent);
        sessionManager.download(ti, new File(dirPath));
        TorrentHandle handle = sessionManager.find(ti.infoHash());
        RunningTorrents.add(handle);
        handle.forceReannounce(1);
        return torrent;
    }

    public void addTorrent(Path workingDir, Path torrentPath) {
        File torrentFile = torrentPath.toFile();
        TorrentInfo ti = new TorrentInfo(torrentFile);
        sessionManager.download(ti, workingDir.toFile());
    }

    public  void reannounceAllTorrents(){
        for (TorrentHandle handle : RunningTorrents){
            try {
                handle.forceReannounce();
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }



}