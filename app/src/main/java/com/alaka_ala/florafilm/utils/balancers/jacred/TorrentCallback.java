package com.alaka_ala.florafilm.utils.balancers.jacred;
import java.util.List;

public interface TorrentCallback {
    void onSuccess(List<TorrentModel> torrents);
    void onError(String errorMessage);
}
