package com.example.modul7;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
  private final ImageView[] imgSlot = new ImageView[3];
  private Button btnGet;
  private final ArrayList<String> arrayUrl = new ArrayList<>();
  private final gameActivity[] slot = new gameActivity[3];
  private boolean play = false;
  private ExecutorService executorService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    btnGet = findViewById(R.id.btn_get);
    imgSlot[0] = findViewById(R.id.img_slot1);
    imgSlot[1] = findViewById(R.id.img_slot2);
    imgSlot[2] = findViewById(R.id.img_slot3);


    executorService = Executors.newFixedThreadPool(3);

    getImageFromJSON();

    btnGet.setOnClickListener(view -> startGame());
  }

  private void startGame() {
    if (!play) {
      for (int i = 0; i < 3; i++) {
        slot[i].play = true;
      }
      for (int j = 0; j < 3; j++) {
        executorService.execute(slot[j]);
      }
      btnGet.setText("Stop");
      play = !play;
      return;
    } else
      for (int i = 0; i < 3; i++) {
        slot[i].play = false;
        btnGet.setText("Play");
        play = !play;
      }
  }

  private void getImageFromJSON() {
    Handler handler = new Handler(Looper.getMainLooper());
    executorService.execute(() -> {
      try {
        final String text = loadStringFromNetwork();
        try {
          JSONArray jsonArray = new JSONArray(text);
          for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            arrayUrl.add(jsonObject.getString("url"));
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      for (int j = 0; j < 3; j++) {
        slot[j] = new gameActivity(imgSlot[j], handler);
      }
    });
  }

  private String loadStringFromNetwork() throws IOException {
    final URL myUrl = new URL("https://661fe99e16358961cd95e3e5.mockapi.io/api/v1/items");
    final InputStream in = myUrl.openStream();
    final StringBuilder out = new StringBuilder();
    final byte[] buffer = new byte[1024];
    try {
      for (int ctr; (ctr = in.read(buffer)) != -1; ) {
        out.append(new String(buffer, 0, ctr));
      }
    } catch (IOException e) {
      throw new RuntimeException("Gagal mendapatkan text", e);
    }
    return out.toString();
  }

  class gameActivity implements Runnable {
    ImageView imageView;
    Random random = new Random();
    boolean play = true;
    int imgIndex = 0;
    Handler handler;
    String url = "";

    public gameActivity(ImageView imageView, Handler handler) {
      this.imageView = imageView;
      this.handler = handler;
    }

    public void run() {
      while (play) {
        imgIndex = random.nextInt(3);
        runOnUiThread(() -> handler.post(() -> {
          url = arrayUrl.get(imgIndex);
          Glide.with(MainActivity.this).load(url).into(imageView);
        }));

        try {
          Thread.sleep(random.nextInt(400));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };
  }
}
