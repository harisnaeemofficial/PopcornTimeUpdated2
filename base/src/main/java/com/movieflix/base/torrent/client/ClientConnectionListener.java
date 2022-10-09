package com.movieflix.base.torrent.client;

public interface ClientConnectionListener {

    public void onClientConnected();

    public void onClientDisconnected();
}