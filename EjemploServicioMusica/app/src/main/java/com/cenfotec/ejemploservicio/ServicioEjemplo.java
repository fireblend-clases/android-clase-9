package com.cenfotec.ejemploservicio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Estudiantes on 14/09/2017.
 */

public class ServicioEjemplo extends Service implements MediaPlayer.OnPreparedListener {

    //Este es el ID de la notificacion que vamos a mostrar en pantalla
    //durante la existencia de este proceso
    private int ONGOING_NOTIFICATION_ID = 101;
    private String INTENT_ID = "Prueba";

    //Este es el constructor de la notificacion. Lo necesitamos
    //para poder actualizar los contenidos de la misma luego de
    //cada cambio de valor
    private NotificationCompat.Builder builder;

    //Una referencia al NotificationManager
    private NotificationManager nm;

    // La instancia de MediaPlayer con la que se va a reproducir el Stream
    MediaPlayer mMediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        NotifReceiver mReceiver = new NotifReceiver();
        registerReceiver(mReceiver, new IntentFilter(this.INTENT_ID));

        ejecutarServicio();

        return super.onStartCommand(intent, flags, startId);
    }

    private void ejecutarServicio() {
        try {
            //Recuperado del api disponible en: http://opml.radiotime.com/
            String url = "http://streamer.teknoweb.co/s967167041/listen"; // your URL here

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setOnPreparedListener(this);

            //Se va a preparar el stream de manera asincrona para no
            //detener el thread principal del servicio. En cuanto este
            //listo, se llama a onPrepared()
            mMediaPlayer.prepareAsync();
        } catch (Exception e){

        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mostrarNotificacion();
    }

    //Este metodo simplemente muestra la notificacion inicial
    private void mostrarNotificacion() {

        //Se prepara un Intent que va a llamar al NotifReceiver
        //(ver registro del receiver en OnStartCommand(...))
        Intent intent = new Intent();
        intent.setAction(INTENT_ID);

        //Este pendingIntent va a ser llamado cuando el usuario
        //presione el boton en la notificación
        PendingIntent pauseResume = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                0
        );

        //Toda notificacion ocupa un titulo,
        //una descripcion y un icono
        builder =
                new NotificationCompat.Builder(this, "ch1")
                        .setSmallIcon(R.drawable.icono)
                        .setContentTitle("Radio Player")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setWhen(0)
                        .addAction(0, mMediaPlayer.isPlaying()?"Pause":"Resume", pauseResume);

        //Se utiliza el metodo startForeground para mostrar
        // la notificacion
        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    //El IBinder es el objeto Binder que se le va a otorgar a
    //un activity que se conecte por medio del metodo bindService()
    //a este servicio.
    private final IBinder mBinder = new LocalBinder();


    //Creamos una implementacion de Binder para que devuelva
    //este servicio a quien lo necesite
    public class LocalBinder extends Binder {
        ServicioEjemplo getService() {
            return ServicioEjemplo.this;
        }
    }

    //Devolvemos mBinder
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //Metodo sencillo para pausar o continuar la reproduccion
    public boolean pauseResume(){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
        // Actualizar el texto del boton en la notificacion (ver Builder arriba)
        builder.mActions.get(0).title = mMediaPlayer.isPlaying()?"Pause":"Resume";
        nm.notify(ONGOING_NOTIFICATION_ID, builder.build());

        return mMediaPlayer.isPlaying();
    }

    // Este Broadcast Receiver es activado cuando se toca el boton de la
    // notificacion.
    public class NotifReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseResume();
        }
    };

}
