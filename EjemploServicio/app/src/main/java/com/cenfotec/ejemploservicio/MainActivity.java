package com.cenfotec.ejemploservicio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    //Instancia del servicio. Esto es solo necesario si ocupamos
    //que este activity se comunique con el servicio. Habilita comunicacion
    //desde el Activity hacia el Servicio.
    private ServicioEjemplo mService;

    //Instancia del Recibidor (BroadcastReceiver). Esto es solo necesario
    //si ocupamos que el servicio pueda enviar broadcasts al activity
    //de tal forma que este pueda recibirlas.
    private Recibidor recibidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.boton);
        Button button2 = (Button) findViewById(R.id.boton2);

        //El boton 1 va a iniciar el servicio, y asociarse a el
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creamos un Intent con el servicio
                Intent intentEjemplo= new Intent(MainActivity.this, ServicioEjemplo.class);

                //Lo iniciamos. Esto va a crear el servicio
                // y llamar a onStartCommand() dentro de dicha clase
                startService(intentEjemplo);

                //Esto asocia al activity con el servicio y es solo necesario
                // si queremos poder llamar metodos expuestos por el Servicio
                // desde el activity.
                //IMPORTANTE: El servicio no esta asociado hasta que el metodo
                // onServiceConnected() (abajo) es llamado automaticamente
                // por el sistema.
                bindService(intentEjemplo, MainActivity.this, Context.BIND_AUTO_CREATE);
            }
        });

        //El boton 2 solo funciona luego de haberse asociado
        //el activity con el servicio por medio de bindService().
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Revisamos si el servicio esta asociado en
                //este momento (ver onServiceConnected abajo)
                if(estamosAsociados){
                    //Obtenemos el valor actual de X del servicio
                    int x = mService.obtenerValorActual();
                    //Mostramos el valor de X
                    Toast.makeText(MainActivity.this, ""+x, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Inicializamos el recibidor
        recibidor = new Recibidor();

        //Lo registramos para que pueda recibir broadcasts de parte
        registerReceiver(recibidor, new IntentFilter(ServicioEjemplo.NUEVO_VALOR));
    }

    //Usamos esta bandera para saber si estamos asociados al servicio o no
    //si no lo estuvieramos, no podriamos usar metodos del servicio
    boolean estamosAsociados = false;

    //Este metodo es llamado automaticamente cuando el servicio
    //se conecta al activity comor resultado de la llamada bindService()
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        //Activamos la bandera
        estamosAsociados = true;

        //Asignamos estas variables para tener referencias al servicio
        ServicioEjemplo.BinderEjemplo binder = (ServicioEjemplo.BinderEjemplo) service;
        mService = binder.obtenerServicio();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        estamosAsociados = false;
    }

    //Este es el BroadcastReceiver que recibe los broadcasts enviados desde
    //el servicio. En este caso, lo unico que hace es mostrar el valor recibido
    //en un toast.
    public class Recibidor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(MainActivity.this, ""+intent.getIntExtra("valor", 0), Toast.LENGTH_SHORT).show();
        }
    }


}
