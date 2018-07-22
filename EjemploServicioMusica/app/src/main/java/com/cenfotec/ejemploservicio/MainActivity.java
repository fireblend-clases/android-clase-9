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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.boton);

        //El boton 1 va a iniciar el servicio, y asociarse a el
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(estamosAsociados){
                    boolean playing = mService.pauseResume();

                    if(playing){
                        button.setText("Pausa");
                    } else {
                        button.setText("Continuar");
                    }
                    return;
                }

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


                button.setText("Pausa");
            }
        });
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
        ServicioEjemplo.LocalBinder binder = (ServicioEjemplo.LocalBinder) service;
        mService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        estamosAsociados = false;
    }
}
