package com.rodrigo.iptv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * Mantém o app vivo enquanto um vídeo/canal toca com a tela bloqueada ou o app em segundo plano.
 * Só existe enquanto há reprodução (a página avisa por setPlaying) e a opção estiver ligada.
 */
public class PlaybackService extends Service {

    private static final String CHANNEL = "playback";
    private static final int NOTIF_ID = 7;

    private PowerManager.WakeLock cpu;
    private WifiManager.WifiLock wifi;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26 && nm != null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Reprodução", NotificationManager.IMPORTANCE_LOW);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        int pf = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, pf);
        Notification.Builder nb = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        nb.setContentTitle("Master IPTV")
                .setContentText("Tocando em segundo plano — toque para voltar")
                .setSmallIcon(getApplicationInfo().icon)
                .setContentIntent(pi)
                .setOngoing(true);
        Notification n = nb.build();
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIF_ID, n);
            }
        } catch (Throwable t) {
            // sem permissão de serviço em primeiro plano: segue sem ele (o app só fica ativo na tela)
        }
        acquireLocks();
        return START_NOT_STICKY;
    }

    @SuppressWarnings("deprecation")
    private void acquireLocks() {
        try {
            if (cpu == null) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "masteriptv:play");
                cpu.setReferenceCounted(false);
            }
            if (!cpu.isHeld()) {
                cpu.acquire();
            }
            if (wifi == null) {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifi = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "masteriptv:play");
                wifi.setReferenceCounted(false);
            }
            if (!wifi.isHeld()) {
                wifi.acquire();
            }
        } catch (Throwable t) {
            // o serviço continua mesmo sem os bloqueios
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (cpu != null && cpu.isHeld()) {
                cpu.release();
            }
            if (wifi != null && wifi.isHeld()) {
                wifi.release();
            }
        } catch (Throwable t) {
            // ignora
        }
        super.onDestroy();
    }
}
