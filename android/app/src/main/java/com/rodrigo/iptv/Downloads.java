package com.rodrigo.iptv;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * Downloads para assistir offline. Usa o gerenciador de downloads do Android (continua com a tela apagada,
 * mostra o progresso na notificação e retoma se a conexão cair). Os arquivos ficam na pasta do próprio app
 * (não pede permissão de armazenamento e some se o app for desinstalado).
 */
final class Downloads {

    private final Context ctx;
    private final DownloadManager dm;

    Downloads(Context context) {
        this.ctx = context.getApplicationContext();
        this.dm = (DownloadManager) this.ctx.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /** Enfileira um download. Devolve o id do gerenciador, ou -1 se não foi possível. */
    long start(String url, String fileName, String title, boolean wifiOnly, String userAgent) {
        try {
            if (dm == null || url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
                return -1;
            }
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setTitle(title == null ? "Master IPTV" : title);
            r.setDescription("Master IPTV");
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setAllowedNetworkTypes(wifiOnly
                    ? DownloadManager.Request.NETWORK_WIFI
                    : (DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE));
            r.setAllowedOverRoaming(false);
            r.setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_MOVIES, safeName(fileName));
            if (userAgent != null && userAgent.length() > 0) {
                r.addRequestHeader("User-Agent", userAgent);
            }
            return dm.enqueue(r);
        } catch (Throwable t) {
            return -1;
        }
    }

    /**
     * Situação dos downloads pedidos, em JSON: [{"id":1,"s":"running","b":bytes,"t":total,"r":motivo,"u":"file://..."}].
     * s = pending | running | paused | done | failed. Um download que o sistema não conhece mais (cancelado
     * pelo usuário nas notificações) ou cujo arquivo sumiu simplesmente não aparece. Devolve null se a consulta falhou.
     */
    String status(long[] ids) {
        if (dm == null) {
            return null;
        }
        JSONArray out = new JSONArray();
        if (ids == null || ids.length == 0) {
            return out.toString();
        }
        Cursor c = null;
        try {
            c = dm.query(new DownloadManager.Query().setFilterById(ids));
            if (c == null) {
                return null;
            }
            int iId = c.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
            int iSt = c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
            int iBy = c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            int iTo = c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int iRe = c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
            int iLo = c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);
            while (c.moveToNext()) {
                int st = c.getInt(iSt);
                String local = c.getString(iLo);
                JSONObject o = new JSONObject();
                o.put("id", c.getLong(iId));
                o.put("s", state(st));
                o.put("b", c.getLong(iBy));
                o.put("t", c.getLong(iTo));
                o.put("r", c.getInt(iRe));
                if (st == DownloadManager.STATUS_SUCCESSFUL) {
                    if (local == null || !exists(local)) {
                        continue;
                    }
                    o.put("u", local);
                }
                out.put(o);
            }
            return out.toString();
        } catch (Throwable t) {
            return null;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /** Cancela e apaga (o arquivo, inclusive o parcial, é removido junto). */
    boolean remove(long id) {
        try {
            return dm != null && dm.remove(id) > 0;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Espaço livre para downloads, em bytes (-1 se não deu para saber). */
    long freeBytes() {
        try {
            File dir = ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if (dir == null) {
                return -1;
            }
            return new StatFs(dir.getPath()).getAvailableBytes();
        } catch (Throwable t) {
            return -1;
        }
    }

    private static String state(int st) {
        switch (st) {
            case DownloadManager.STATUS_PENDING:
                return "pending";
            case DownloadManager.STATUS_RUNNING:
                return "running";
            case DownloadManager.STATUS_PAUSED:
                return "paused";
            case DownloadManager.STATUS_SUCCESSFUL:
                return "done";
            default:
                return "failed";
        }
    }

    private static boolean exists(String localUri) {
        try {
            String p = Uri.parse(localUri).getPath();
            return p != null && new File(p).exists();
        } catch (Throwable t) {
            return false;
        }
    }

    private static String safeName(String n) {
        String s = n == null ? "" : n.replaceAll("[^A-Za-z0-9._-]", "_");
        return s.length() == 0 ? "download.mp4" : s;
    }
}
