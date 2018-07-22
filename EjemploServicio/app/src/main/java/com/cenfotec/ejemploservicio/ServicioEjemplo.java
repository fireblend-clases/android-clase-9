package com.cenfotec.ejemploservicio;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Estudiantes on 14/09/2017.
 */

public class ServicioEjemplo extends Service {

    //Este es el valor con el que vamos a enviar broadcasts
    //que el activity puede recibir por medio de un BroadcastReceiver
    public static final String NUEVO_VALOR = "NUEVO_VALOR";

    //Este es el ID de la notificacion que vamos a mostrar en pantalla
    //durante la existencia de este proceso
    private int ONGOING_NOTIFICATION_ID = 101;

    //Este es el constructor de la notificacion. Lo necesitamos
    //para poder actualizar los contenidos de la misma luego de
    //cada cambio de valor
    private NotificationCompat.Builder builder;

    //Una referencia al NotificationManager
    private NotificationManager nm;

    //El valor que este servicio actualiza cada 5 segundos.
    private int x;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Agregamos llamadas de Log.d para que imprima texto en
        //la consola de Android. Esto es util para depurar servicios
        //y aplicaciones
        Log.d("VALOR", "Iniciamos el servicio");

        //Inicializamos la referencia al NotificationManager
        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //Mostramos la notificacion. Esto hace que el sistema
        //le de prioridad a este servicio como se la daria a
        //un activity
        mostrarNotificacion();

        //Aunque estemos en un servicio, cualquier operacion de
        //bloqueo que vaya a detener la ejecucion del thread
        //principal tiene que realizarse en su propio thread.
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                ejecutarServicio();
            }
        };
        thread.start();


        return super.onStartCommand(intent, flags, startId);
    }

    private void ejecutarServicio() {
        x = 0;
        // Este es un simple ciclo que actualiza el valor de
        // la variable X cada 5 segundos.
        while(true) {
            try {
                //Si X llega a ser igual a 10, el servicio muere
                if(x == 10){
                    stopSelf();
                    return;
                }

                //Mostramos el valor actual en consola
                Log.d("VALOR", ""+x);
                //Dormimos 5 segundos
                Thread.sleep(5000);
                //Incrementamos el valor de X
                x++;

                //Enviamos un broadcast con el nuevo valor de X.
                //Si un activity registro un BroadcastReceiver para
                //recibirlo y manejarlo, va a recibirlo luego de este
                //metodo.
                enviarBroadcast();

                //Actualizamos la notificación con el valor actual
                //de la variable X
                builder.setContentText("Valor actual: " + x);

                //Y se lo notificamos a la instancia de NotificationManager
                //para que actualize la notificacion
                nm.notify(
                        ONGOING_NOTIFICATION_ID,
                        builder.build());



            } catch (Exception e) {

            }
        }
    }


    private void enviarBroadcast() {
        //        //Simplemente se crea un intent con el identificador
        //unico del Broadcast que declaramos al principio
        Intent i = new Intent(NUEVO_VALOR);

        //Como es un intent, se le pueden agregar datos que
        //van a ser recibidos por quien este escuchando el
        //Broadcast
        i.putExtra("valor", x);

        //Enviamos el broadcast
        sendBroadcast(i);
    }

    //Este metodo simplemente muestra la notificacion inicial
    private void mostrarNotificacion() {
        //Toda notificacion ocupa un titulo,
        // una descripcion y un icono
        builder =
                new NotificationCompat.Builder(this, "ch")
                        .setSmallIcon(R.drawable.icono)
                        .setContentTitle("Ejemplo de Servicio")
                        .setContentText("Valor actual: 0");

        //Se utiliza el metodo startForeground para mostrar
        // la notificacion
        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    //El IBinder es el objeto Binder que se le va a otorgar a
    //un activity que se conecte por medio del metodo bindService()
    //a este servicio.
    private final IBinder mBinder = new BinderEjemplo();

    //Creamos una implementacion de Binder para que devuelva
    //este servicio a quien lo necesite
    public class BinderEjemplo extends Binder {
        ServicioEjemplo obtenerServicio() {
            return ServicioEjemplo.this;
        }
    }

    //Devolvemos mBinder
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //Este metodo publico es accesible por cualquier activity
    //que se haya conectado a este servicio por medio de bindService()
    public int obtenerValorActual(){
        return x;
    }

}
