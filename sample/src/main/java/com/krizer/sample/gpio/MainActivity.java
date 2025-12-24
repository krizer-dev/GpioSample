package com.krizer.sample.gpio;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.krizer.gpio.Gpio;
import com.krizer.gpio.GpioController;
import com.krizer.gpio.GpioEnum;
import com.krizer.gpio.GpioState;
import com.krizer.sample.gpio.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {
    private final Map<String, TextView> stateTextViews = new LinkedHashMap<>();
    private final Map<String, ToggleButton> activeButtons = new LinkedHashMap<>();
    private ActivityMainBinding binding;
    private GpioController gpioController;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean loop = true;
    private long backPressMs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gpioController = GpioController.getInstance(this);
        gpioController.startObserver();
        gpioController.setOnChangeGpioStateListener(this::onGpioStateChanged);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() - backPressMs > 1500) {
                    Toast.makeText(MainActivity.this, "종료하려면 한번 더 눌러주세요", Toast.LENGTH_SHORT).show();
                } else {
                    finishAffinity();
                }

                backPressMs = System.currentTimeMillis();
            }
        });

        binding.btnPresetMS68.setOnClickListener(v -> {
            binding.etName.setText(GpioEnum.MS68.C2.getName());
            binding.etAddress.setText(GpioEnum.MS68.C2.getAddress());
            onCreateBtnClicked();

            binding.etName.setText(GpioEnum.MS68.C3.getName());
            binding.etAddress.setText(GpioEnum.MS68.C3.getAddress());
            onCreateBtnClicked();

            binding.etName.setText(GpioEnum.MS68.C4.getName());
            binding.etAddress.setText(GpioEnum.MS68.C4.getAddress());
            onCreateBtnClicked();
        });

        binding.btnPresetS38.setOnClickListener(v -> {
            binding.etName.setText(GpioEnum.S38.A0.getName());
            binding.etAddress.setText(GpioEnum.S38.A0.getAddress());
            onCreateBtnClicked();

            binding.etName.setText(GpioEnum.S38.A1.getName());
            binding.etAddress.setText(GpioEnum.S38.A1.getAddress());
            onCreateBtnClicked();

            binding.etName.setText(GpioEnum.S38.A2.getName());
            binding.etAddress.setText(GpioEnum.S38.A2.getAddress());
            onCreateBtnClicked();
        });

        binding.btnPresetS58.setOnClickListener(v -> {
            binding.etName.setText(GpioEnum.S58.C2.getName());
            binding.etAddress.setText(GpioEnum.S58.C2.getAddress());
            onCreateBtnClicked();

            binding.etName.setText(GpioEnum.S58.C3.getName());
            binding.etAddress.setText(GpioEnum.S58.C3.getAddress());
            onCreateBtnClicked();

            binding.etName.setText(GpioEnum.S58.C4.getName());
            binding.etAddress.setText(GpioEnum.S58.C4.getAddress());
            onCreateBtnClicked();
        });

        binding.btnCreate.setOnClickListener(v -> onCreateBtnClicked());

        showGpioCmd();
    }

    private void onCreateBtnClicked() {
        String name = binding.etName.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "빈 데이터", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gpioController.getGpio(name) != null) {
            Toast.makeText(this, "이미 존재하는 GPIO입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.etName.setText("");
        binding.etAddress.setText("");

        Gpio gpio = gpioController.create(name, address);
        gpioController.registerGpioObserver(gpio);

        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setLayoutDirection(LinearLayout.HORIZONTAL);
        layoutParams.setMargins(12, 12, 12, 12);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setPadding(6, 6, 6, 6);

        TextView label = new TextView(MainActivity.this);
        LinearLayout.MarginLayoutParams labelParams = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(6, 6, 6, 6);
        label.setPadding(6, 6, 6, 6);
        label.setLayoutParams(labelParams);
        String text = gpio.getName() + "(" + gpio.getAddress() + ")";
        label.setText(text);

        TextView stateTextView = new TextView(MainActivity.this);
        LinearLayout.MarginLayoutParams stateTextViewParams = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stateTextViewParams.setMargins(6, 6, 6, 6);
        stateTextView.setPadding(6, 6, 6, 6);
        stateTextView.setLayoutParams(stateTextViewParams);
        stateTextView.setVisibility(View.GONE);

        ToggleButton activeButton = new ToggleButton(MainActivity.this);
        LinearLayout.MarginLayoutParams activeButtonParams = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        activeButtonParams.setMargins(6, 6, 6, 6);
        activeButton.setLayoutParams(activeButtonParams);
        activeButton.setTag(gpio);
        activeButton.setTextOn("HIGH");
        activeButton.setTextOff("LOW");
        activeButton.setChecked(false);
        stateTextView.setVisibility(View.GONE);
        activeButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gpio.setActive(isChecked ? Gpio.ACTIVE_HIGH : Gpio.ACTIVE_LOW);
            try {
                gpioController.writeOnce(gpio);
            } catch (InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });

        ToggleButton directionButton = new ToggleButton(MainActivity.this);
        LinearLayout.MarginLayoutParams directionButtonParams = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        directionButtonParams.setMargins(6, 6, 6, 6);
        directionButton.setLayoutParams(directionButtonParams);
        directionButton.setTag(gpio);
        directionButton.setTextOn("IN");
        directionButton.setTextOff("OUT");
        directionButton.setChecked(false);
        directionButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gpio.setDirection(isChecked ? Gpio.DIRECTION_IN : Gpio.DIRECTION_OUT);
            try {
                gpioController.writeOnce(gpio);
            } catch (InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });

        ImageView deleteImage = new ImageView(MainActivity.this);
        LinearLayout.LayoutParams deleteImageParams = new LinearLayout.LayoutParams(32, 32);
        deleteImageParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        deleteImage.setLayoutParams(deleteImageParams);
        deleteImage.setPadding(6, 6, 6, 6);
        deleteImage.setImageResource(R.drawable.delete);
        deleteImage.setOnClickListener(iv -> {
            gpioController.close(gpio);
            activeButtons.remove(gpio.getName());
            stateTextViews.remove(gpio.getName());
            binding.loGpio.removeView(linearLayout);
        });

        linearLayout.addView(label);
        linearLayout.addView(directionButton);
        linearLayout.addView(activeButton);
        linearLayout.addView(stateTextView);
        linearLayout.addView(deleteImage);
        binding.loGpio.addView(linearLayout);

        activeButtons.put(gpio.getName(), activeButton);
        stateTextViews.put(gpio.getName(), stateTextView);

        try {
            gpioController.writeOnce(gpio.init());
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

    private void showGpioCmd() {
        executorService.execute(() -> {
            while (true) {
                Process process = null;
                BufferedReader bufferedReader = null;

                try {
                    StringBuilder stringBuilder = new StringBuilder();

                    Thread.sleep(500);

                    if (!loop) {
                        break;
                    }

                    process = Runtime.getRuntime().exec("cat /d/gpio");
                    bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    process.waitFor();
                    ActivityCompat.getMainExecutor(MainActivity.this).execute(() -> binding.tvGpio.setText(stringBuilder.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void onGpioStateChanged(GpioState gpioState) {
        ActivityCompat.getMainExecutor(this).execute(() -> {
            TextView stateTextView = stateTextViews.get(gpioState.getName());
            ToggleButton activeToggleButton = activeButtons.get(gpioState.getName());

            if (stateTextView != null) {
                stateTextView.setVisibility(gpioState.getDirection() == Gpio.DIRECTION_OUT ? View.GONE : View.VISIBLE);
                if (gpioState.getDirection() == Gpio.DIRECTION_IN) {
                    stateTextView.setText(gpioState.getActive() == Gpio.ACTIVE_LOW ? "Low" : "High");
                }
            }

            if (activeToggleButton != null) {
                activeToggleButton.setVisibility(gpioState.getDirection() == Gpio.DIRECTION_OUT ? View.VISIBLE : View.GONE);
                if (gpioState.getDirection() == Gpio.DIRECTION_OUT) {
                    activeToggleButton.setChecked(gpioState.getActive() == Gpio.ACTIVE_HIGH);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        gpioController.terminate();
        gpioController = null;
        loop = false;

        super.onDestroy();
    }
}