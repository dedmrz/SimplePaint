package com.example.paint;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    //Переменные
    private PaintView paintView;
    private int defaultColor;
    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Button button; // Задание кнопни для открытия меню цветов

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Отвечает за работу приложения и расположение компонентов на экране

        paintView = findViewById(R.id.paintView); // поиск кисти по айди
        button = findViewById(R.id.change_color_button); // поиск кнопки по айди
        DisplayMetrics displayMetrics = new DisplayMetrics(); // переменная диссплея
        SeekBar seekBar = findViewById(R.id.seekBar); //слайдер для размера линии
        final TextView textView = findViewById(R.id.current_pen_size); //текстовое поле для отображения ширины линии

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics); //получение размеров экрана

        paintView.initialise(displayMetrics);

        textView.setText("Толщина линии: " + seekBar.getProgress()); // Толщина линии + текущий размер

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { //слущатель для кнопки
                openColourPicker(); //открытие цветового меню
            }

        });

        //слушатель для слайдера
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                paintView.setStrokeWidth(seekBar.getProgress());
                textView.setText("Толщина линии " + seekBar.getProgress());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    //Создание меню для сохранения и т.д.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater(); //переменная меню
        menuInflater.inflate(R.menu.main, menu); //получение составляющего из файла /res/menu/main.xml

        return super.onCreateOptionsMenu(menu);

    }

    //Проверка прав, ввывод окна для их получения в случае их отсутствия
    private void requestStoragePermission () {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Необходимы права")//заголовок
                    .setMessage("Необходимо для сохранения изображения")//текст
                    .setPositiveButton("ок", new DialogInterface.OnClickListener() { // позитивный ответ

                        @Override // слушатель для нажатия на позитивный ответ
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

                        }
                    }) //негативный ответ
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }

                    })
                    .create().show();

        } else {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

        }

    }

    //Вывод ссобщений о результате получения прав
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Права получены", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Ошибка получения прав", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    //слушатель для нажатия на пункты меню
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.clear_button: //нажатие на "очистить"
                paintView.clear(); //Вызов функции
                return true;
            case R.id.undo_button: //нажатие на "отменить"
                paintView.undo(); //Вызов функции
                return true;
            case R.id.redo_button: //нажатие на "Вернуть отмену"
                paintView.redo(); //Вызов функции
                return true;
            case R.id.save_button: //нажатие на "сохранить"

                //проверка наличия прав
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                }
                paintView.saveImage(); //Вызов функции
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    //открытие цветового меню
    private void openColourPicker () {

        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

                Toast.makeText(MainActivity.this, "Вернулись назад", Toast.LENGTH_LONG).show(); //вывод тоста о том что вернулись назад

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

                defaultColor = color;

                paintView.setColor(color);

            }

        });

        ambilWarnaDialog.show(); // отображение окна 

    }
}