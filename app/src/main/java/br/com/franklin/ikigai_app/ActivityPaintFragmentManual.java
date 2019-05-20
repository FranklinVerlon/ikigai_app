package br.com.franklin.ikigai_app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import br.com.franklin.ikigai_app.R;

public class ActivityPaintFragmentManual extends Fragment {
    private DoodleViewManual doodleView; //para desenhar e lidar com os eventos de toque na tela
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    //para evitar que mais de um diálogo seja exibido
    private boolean dialogOnScreen = false;
    //limiar que deve ser superado para considerar
    //que o usuário balançou o dispositivo
    private static final int ACCELERATION_THRESHOLD = 1000000;
    //código que identifica o pedido de permissão
    //para acesso ao armazenamento externo
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    private android.app.AlertDialog alerta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_paint_manual, container, false);
        setHasOptionsMenu(true);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.information))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        alerta = builder.create();
        alerta.show();

        doodleView = v.findViewById(R.id.doodleViewManual);
        //aceleração atual
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        //última conhecida
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        //será atualizada quando uma nova aceleração for detectada
        acceleration = 0.00f;

        return v;
    }

    private final SensorEventListener sensorEventListener =
            new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    //verifica se um diálogo já não está sendo exibido
                    if (!dialogOnScreen){
                        //pega a aceleração nos três eixos
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values [2];
                        //última aceleração se torna a atual
                        //faz sentido depois da primeira vez
                        lastAcceleration = currentAcceleration;
                        //atualiza a aceleração atual
                        currentAcceleration = x * x + y * y + z * z;
                        //qual a diferença entre a aceleração que já
                        //existia e a nova detectada?
                        acceleration = currentAcceleration *
                                (currentAcceleration - lastAcceleration);
                        //passou do limiar?
                        if (acceleration > ACCELERATION_THRESHOLD){
                            confirmErase();//faremos a seguir
                        }
                    }

                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int
                        accuracy) {
                }
    };

    private void confirmErase (){
        EraseImageDialogFragment eraseFragment = new EraseImageDialogFragment();
        eraseFragment.show(getFragmentManager(),"erase dialog");
    }

    private void enableAccelerometerListening(){
        //obtém o serviço
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        //registra o observer
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void disableAccelerometerListening(){
        //obtém o serviço
        SensorManager sensorManager =
                (SensorManager) getActivity().
                        getSystemService(Context.SENSOR_SERVICE);
        //desregistra o observer
        sensorManager.unregisterListener(
                sensorEventListener,

                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doodle_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.color:
                ColorDialogFragment colorDialog =
                        new ColorDialogFragment();
                colorDialog.show(getFragmentManager(), "color dialog");
                return true;
            case R.id.line_width:
                LineWidthDialogFragment widthDialog =  new LineWidthDialogFragment();
                widthDialog.show(getFragmentManager(), "line width dialog");
                return true;
            case R.id.delete:
                confirmErase();
                return true;
            case R.id.save:
                //saveImage();//faremos em breve
                return true;
            case R.id.print:
                //doodleView.printImage();//faremos em breve
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage (){
        if (getContext().
                checkSelfPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            //permissão ainda não concedida
            //usuário já disse não uma vez? se sim, explicar
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )){
                //constroi um (builder de) diálogo
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.permission_explanation);
                builder.setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //pede a permissão
                                requestPermissions(new String []{

                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},

                                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                            }
                        });
                //mostra
                builder.create().show();
            }
            else{
                //usuário ainda não disse não, só pede a permissão
                requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            //permissão já concedida, salva
            //doodleView.saveImage();//faremos em breve
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case SAVE_IMAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    //doodleView.saveImage();//faremos já já
                }
        }
    }

    public DoodleViewManual getDoodleView(){
        return this.doodleView;
    }
    public void setDialogOnScreen(boolean dialogOnScreen) {
        this.dialogOnScreen = dialogOnScreen;
    }






}
