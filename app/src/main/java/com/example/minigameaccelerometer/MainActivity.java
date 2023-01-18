package com.example.minigameaccelerometer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private CanvasView canvas;
    private int circleRadius = 30;
    private float circleX;
    private float circleY;

    private Timer timer;
    private Handler handler;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float sensorX;
    private float sensorY;
    private float sensorZ;
    private long lastSensorUpdateTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //se creează un obiect de tip SensorManager,
        // care permite aplicației să gestioneze senzorii dispozitivului.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //se obține o referință la senzorul accelerometrului
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //listner-ul va fi notificat atunci când se detectează schimbări în valorile senzorului.
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //se obține o referință la ecranul dispozitivului
        Display display = getWindowManager().getDefaultDisplay();
        //se obține dimensiunea ecranului
        Point size = new Point();
        display.getSize(size);

        //latimea si inaltimea ecranului
        //int screenWidth = size.x;
        //int screenHigh = size.y;
        //se determina latimea si inaltimea in pixeli
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHigh = getResources().getDisplayMetrics().heightPixels;

        //se determina pozitia cercului in centrul ecranului
        circleX = screenWidth / 2 - circleRadius;
        circleY = screenHigh / 2 - circleRadius;

        //cu ajutorul aceastei clase se va desena cercul pe ecran
        canvas = new CanvasView(MainActivity.this);
        setContentView(canvas);

        //handler folosit pentru actualizarea afisarii cercului pe ecran
        handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                //ajuta la redare actualizată a obiectului
                canvas.invalidate();
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            //pentru a deplasa cercul in stanga sau in dreapta si in sus sau in jos,
            // in functie de valoarea lor.
                if(sensorX < 0){
                    circleX += 5;
                }else{
                    circleX -= 5;
                }

                if(sensorY > 0){
                    circleY += 5;
                }else{
                    circleY -= 5;
                }
            //Acest cod verifica daca cercul s-a apropiat prea mult de marginea ecranului si il readuce inapoi
            // la o distanta de 10 pixeli de margine.
                //daca distanta dintre coordonata X si marginea stanga a ecranului < 10
                // < 10 adica prea aproape de marginea ecranului
                if(circleX - circleRadius < 10){
                    circleX = 10 + circleRadius;
                }
                //se verifica daca distanta dintre coordonata X si marginea din dreapta a ecranului
                //se verifica daca suma coordonatei X si a razei cercului > latime ecranului - 10 pixeli
                //pe care dorim sa i adaugam ca sa nu iasa din ecran cercul
                if(circleX + circleRadius > screenWidth - 10){
                    circleX = screenWidth - 10 - circleRadius;
                }
                //daca distanta dintre coordonata Y si marginea de sus a ecranului < 10
                // < 10 adica prea aproape de marginea ecranului
                if(circleY - circleRadius < 10){
                    circleY = 10 + circleRadius;
                }
                //se verifica daca distanta dintre coordonata Y si marginea de jos a ecranului
                //se verifica daca suma coordonatei Y si a razei cercului > inaltimea ecranului - 150 pixeli
                //pe care dorim sa i adaugam ca sa nu iasa din ecran cercul
                if(circleY + circleRadius > screenHigh - 150){
                    circleY = screenHigh - 150 - circleRadius;
                }
                //trimite un mesaj gol catre handler pentru a trigger-ui redraw-ul cercului pe ecran.
                handler.sendEmptyMessage(0);
            }
        //incepe timer-ul si seteaza o perioada de 100 milisecunde pentru fiecare actualizare.
        }, 0, 100);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER){

            long currentTime = System.currentTimeMillis();
        //se verifică dacă timpul curent minus ultimul timp când s-au actualizat valorile senzorului
        //este mai mare de 100 milisecunde. Aceasta se face pentru a evita prelucrarea prea multor
        //evenimente și pentru a evita consumul inutil de resurse.
            if((currentTime - lastSensorUpdateTime) > 100){
                lastSensorUpdateTime = currentTime;
                //valoarea senzorului pe axa X,Y,Z se salvează într-o variabilă diferita.
                sensorX = sensorEvent.values[0];
                sensorY = sensorEvent.values[1];
                sensorZ = sensorEvent.values[2];
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class CanvasView extends View {
        //pentru a desena pe ecran
        private Paint pen;

        public CanvasView(Context context){
            super(context);
            setFocusable(true);

            pen = new Paint();
        }

        public void onDraw(Canvas screen){
            pen.setStyle(Paint.Style.FILL);
            pen.setAntiAlias(true);
            pen.setTextSize(30f);
            pen.setColor(Color.RED);
//se desenează un cerc pe ecran, folosind coordonatele X și Y, raza și obiectul Paint.
            screen.drawCircle(circleX, circleY, circleRadius, pen);
            //marirea vitezei
            //viteza pe axa X este 5 pixeli/sec
            //viteza pe axa Y este 5 pixeli/sec
            //viteza totala este 7 ( rad(5^2 + 5^2) = rad(50) = 7 pixeli/sec
//se actualizează coordonata X sau Y a cercului,
// în funcție de valoarea senzorului pe axa X sau Y
// și de o valoare de viteză.
            circleX -= sensorX * 5;
            circleY += sensorY * 5;
        }
    }
}