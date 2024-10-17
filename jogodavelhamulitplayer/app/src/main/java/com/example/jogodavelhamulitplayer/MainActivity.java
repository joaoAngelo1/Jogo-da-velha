package com.example.jogodavelhamulitplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private ImageView[][] tabuleiroImagem;
    private Button start;
    private int[][] tabuleiro;
    private int contClick;
    private Socket cliente;
    private DataInputStream entrada;
    private DataOutputStream saida;
    private boolean minhavez;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gridLayout = findViewById(R.id.gridLayout);
        start = findViewById(R.id.startId);
        tabuleiroImagem = new ImageView[3][3];
        tabuleiro = new int[3][3];
        contClick = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ImageView temp = new ImageView(getApplicationContext());
                temp.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.vazio));
                criarCampo(i, j, temp);
            }
        }


        new Thread(new ClienteThread()).start();

        for (int i = 0; i < tabuleiroImagem.length; i++) {
            for (int j = 0; j < tabuleiroImagem[0].length; j++) {
                int finalI = i;
                int finalJ = j;
                tabuleiroImagem[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tabuleiro[finalI][finalJ] == 0 && minhavez) {
                            marcarCampo(finalI, finalJ, contClick);
                            contClick++;
                            enviarCoordenadas(finalI, finalJ);
                            Log.d("coordenadas", "coordenadas enviadas");
                            minhavez = false;

                            if (ganhou()) {
                                Toast.makeText(MainActivity.this, "Você ganhou!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarTabuleiro();
            }
        });
    }
    private class ClienteThread implements Runnable {
        @Override
        public void run() {
            try {
                cliente = new Socket("192.168.186.138", 3382);
                entrada = new DataInputStream(cliente.getInputStream());
                saida = new DataOutputStream(cliente.getOutputStream());

                int quemComeca = entrada.readInt();

                minhavez = (quemComeca == 1);

                while (!cliente.isClosed()) {
                    receberCoordenadas();
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erro ao conectar ao servidor", Toast.LENGTH_SHORT).show()
                );
            }
        }
    }


    public void iniciarTabuleiro() {
        for (int i = 0; i < tabuleiroImagem.length; i++) {
            for (int j = 0; j < tabuleiroImagem[0].length; j++) {
                tabuleiroImagem[i][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.vazio));
                tabuleiro[i][j] = 0;
                tabuleiroImagem[i][j].setEnabled(true);
            }
        }
        contClick = 0;
    }
    public void criarCampo(int linha, int col, ImageView botao) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        layoutParams.setGravity(0);

        layoutParams.rowSpec = GridLayout.spec(linha, 1f);
        layoutParams.columnSpec = GridLayout.spec(col, 1f);
        layoutParams.setMargins(2, 2, 2, 2);
        botao.setLayoutParams(layoutParams);
        gridLayout.addView(botao);
        tabuleiroImagem[linha][col] = botao;
    }

    public void marcarCampo(int linha, int col, int cont) {
        if (cont % 2 == 0) {
            tabuleiro[linha][col] = 1;
            tabuleiroImagem[linha][col].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.x));
        } else {
            tabuleiro[linha][col] = 2;
            tabuleiroImagem[linha][col].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.o));
        }
    }
    public boolean ganhou() {
           for (int i = 0; i < tabuleiroImagem.length; i++) {
            for (int j = 0; j < tabuleiroImagem[0].length; j++) {
                if (j + 1 <= 2 && (j - 1) >= 0) {
                    if (tabuleiro[i][j-1] == 1 && tabuleiro[i][j] == 1 && tabuleiro[i][j+1] ==1){
                        Toast.makeText(this, "ganhou", Toast.LENGTH_SHORT).show();
                        tabuleiroImagem[i][j-1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadohorizontal));
                        tabuleiroImagem[i][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadohorizontal));
                        tabuleiroImagem[i][j+1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadohorizontal));
                        travarCelulas();
                        return true;

                    }
                    if (tabuleiro[i][j - 1] == 2 && tabuleiro[i][j] == 2 && tabuleiro[i][j + 1] == 2) {
                        Toast.makeText(this, "ganhou", Toast.LENGTH_SHORT).show();
                        tabuleiroImagem[i][j - 1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadohorizontal));
                        tabuleiroImagem[i][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadohorizontal));
                        tabuleiroImagem[i][j + 1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadohorizontal));
                        travarCelulas();
                        return true;


                    }
                }
                if (i + 1 <= 2 && (i - 1) >= 0) {
                    if (tabuleiro[i-1][j] == 1 && tabuleiro[i][j] == 1 &&  tabuleiro[i+1][j] == 1){
                        Toast.makeText(this, "ganhou", Toast.LENGTH_SHORT).show();
                        tabuleiroImagem[i-1][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadovertical));
                        tabuleiroImagem[i][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadovertical));
                        tabuleiroImagem[i+1][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadovertical));
                        travarCelulas();
                        return true;

                    }
                    if (tabuleiro[i - 1][j] == 2 && tabuleiro[i][j] == 2 && tabuleiro[i + 1][j] == 2) {
                        Toast.makeText(this, "ganhou", Toast.LENGTH_SHORT).show();
                        tabuleiroImagem[i - 1][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadovertical));
                        tabuleiroImagem[i][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadovertical));
                        tabuleiroImagem[i + 1][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadovertical));
                        travarCelulas();
                        return true;

                    }
                }
                if (i + 1 <= 2 && (i - 1) >= 0) {
                    if (j + 1 <= 2 && (j - 1) >= 0) {
                        if (tabuleiro[i][j] == 2 && tabuleiro[i + 1][j + 1] == 2 && tabuleiro[i - 1][j - 1] == 2) {
                            Toast.makeText(this, "ganhou", Toast.LENGTH_SHORT).show();
                            tabuleiroImagem[i][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadodiagonal));
                            tabuleiroImagem[i + 1][j + 1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadodiagonal));
                            tabuleiroImagem[i - 1][j - 1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ocortadodiagonal));
                            travarCelulas();
                            return true;

                        }
                        if (tabuleiro[i][j] == 1 && tabuleiro[i+1][j+1] == 1 &&  tabuleiro[i-1][j-1] == 1){
                            Toast.makeText(this, "ganhou", Toast.LENGTH_SHORT).show();
                            tabuleiroImagem[i][j].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadodiagonal));
                            tabuleiroImagem[i+1][j+1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadodiagonal));
                            tabuleiroImagem[i-1][j-1].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.xcortadodiagonal));
                            travarCelulas();
                            return true;

                        }



                    }

                }
            }
        }
        return false;
    }

    public void travarCelulas() {
        for (int i = 0; i < tabuleiroImagem.length; i++) {
            for (int j = 0; j < tabuleiroImagem[0].length; j++) {
                tabuleiroImagem[i][j].setEnabled(false);
            }
        }
    }


        private void enviarCoordenadas(int x, int y) {
            new Thread(() -> {
                try {
                    int coordenada = x * 10 + y;
                    saida.writeInt(coordenada); // Envia a coordenada
                    saida.flush();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),"Coordenada enviada: "+coordenada,Toast.LENGTH_SHORT).show()); // Log para verificar
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro ao enviar coordenadas", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }


    private void receberCoordenadas() {
        try {
            int coordenada = entrada.readInt();
            int x = coordenada / 10;
            int y = coordenada % 10;


            runOnUiThread(() -> {
                if (tabuleiro[x][y] == 0) {
                    marcarCampo(x, y, contClick);
                    contClick++;
                    minhavez = true;

                    if (ganhou()) {
                        Toast.makeText(MainActivity.this, "Oponente ganhou!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                   Toast.makeText(getApplicationContext(),"Célula já ocupada: (" + x + ", " + y + ")", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ReceberCoordenadas", "Erro ao receber coordenadas", e);
        }
    }
}