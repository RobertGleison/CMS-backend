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
    String trackerIP = "http://netflixppup.duckdns.org:8000/announce";
    public TorrentManager(){
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
        sessionManager.start();
    }


    // dirpath tem que ser: mnt/bucket/avatar
    // moviename tem que ser HD_video.mp4
    public byte[] createTorrent(String dirPath, String moviename) {
        System.out.println("Enter in create torrent of TorrentManager");
        //moviepath: /mnt/bucket/avatar/HD_video.mp4
        File moviePath = new File(dirPath, moviename);
        System.out.println("movie: " + moviePath);
        TorrentBuilder torrentBuilder = new TorrentBuilder().addTracker(trackerIP).setPrivate(true).path(moviePath);
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

    public  void reannounceAllTorrents(){
        for (TorrentHandle handle : RunningTorrents){
            handle.forceReannounce();
        }
    }



}