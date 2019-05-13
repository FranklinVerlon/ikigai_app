package br.com.franklin.ikigai_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import br.com.franklin.ikigai_app.R;

public class MainActivity extends AppCompatActivity {
    Button btTip;
    Button btStart;
    Button btStartManual;
    private ActivityPaintFragment frag1 = new ActivityPaintFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btTip = (Button) findViewById(R.id.btTip);
        btStart = (Button) findViewById(R.id.btStart);
        btStartManual = (Button) findViewById(R.id.btStartManual);

        btTip.setOnClickListener((v) -> {
            Intent it = new Intent(this, AboutActivity.class);
            startActivity(it);
        });

        btStart.setOnClickListener((v) -> {
            Intent it = new Intent(this, PaintActivity.class);
            startActivity(it);
        });

        btStartManual.setOnClickListener((v) -> {
            Intent it = new Intent(this, PaintManualActivity.class);
            startActivity(it);
        });
    }
}
