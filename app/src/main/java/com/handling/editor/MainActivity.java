package com.handling.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import android.graphics.Color;
import android.graphics.Typeface;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ScrollView;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;

import rikka.shizuku.Shizuku;

// =============================================================
// LISTVIEW PERSONALIZADO
// =============================================================

class ListaVeiculosView extends ListView {

private int alturaMaxima;

public ListaVeiculosView(Context context) {

    super(context);

    alturaMaxima =
        (int) (
            300 *
            getResources()
            .getDisplayMetrics()
            .density
        );
}

public void setAlturaMaxima(int altura) {

    alturaMaxima = altura;

    requestLayout();
}

@Override
protected void onMeasure(
    int widthMeasureSpec,
    int heightMeasureSpec) {

    int alturaSpec =
        MeasureSpec.makeMeasureSpec(
            alturaMaxima,
            MeasureSpec.AT_MOST
        );

    super.onMeasure(
        widthMeasureSpec,
        alturaSpec
    );
}

}

// =============================================================
// MAIN ACTIVITY
// =============================================================

public class MainActivity extends Activity {

private static final int ABRIR_ARQUIVO = 100;
private static final int EXPORTAR_ARQUIVO = 200;

// Permissão utilizada pelo Shizuku
private static final int SHIZUKU_PERMISSION_CODE = 500;

// =========================================================
// CAMINHOS DO JOGO
// =========================================================

private static final String HANDLING_DATA =
    "/storage/emulated/0/Android/data/com.brp.game/files/data/handling.cfg";

private static final String HANDLING_SAMP =
    "/storage/emulated/0/Android/data/com.brp.game/files/SAMP/handling.cfg";


// =========================================================
// CORES
// =========================================================

private final int FUNDO =
    Color.rgb(18, 18, 18);

private final int CARD =
    Color.rgb(30, 30, 30);

private final int CAMPO =
    Color.rgb(45, 45, 45);

private final int TEXTO =
    Color.WHITE;

private final int TEXTO_SECUNDARIO =
    Color.rgb(180, 180, 180);

private final int VERDE =
    Color.rgb(100, 220, 100);

private final int VERMELHO =
    Color.rgb(220, 80, 80);


// =========================================================
// INTERFACE
// =========================================================

private LinearLayout layoutPrincipal;

private TextView status;

private TextView veiculoSelecionado;

private EditText valorVelocidade;
private EditText valorAceleracao;
private EditText valorFreio;
private EditText valorDirecao;
private EditText valorTracao;

private TextView padraoVelocidade;
private TextView padraoAceleracao;
private TextView padraoFreio;
private TextView padraoDirecao;
private TextView padraoTracao;


// =========================================================
// DADOS DO HANDLING
// =========================================================

private ArrayList<String> linhasHandling =
    new ArrayList<String>();

private ArrayList<String> nomesVeiculos =
    new ArrayList<String>();

private ArrayList<String> carros =
    new ArrayList<String>();

private ArrayList<String> motos =
    new ArrayList<String>();

private ArrayList<String> aereos =
    new ArrayList<String>();

private ArrayList<String> maritimos =
    new ArrayList<String>();

private HashMap<String, String> linhasPorVeiculo =
    new HashMap<String, String>();

private HashMap<String, String> linhasOriginais =
    new HashMap<String, String>();

private String categoriaAtual =
    "CARRO";

private String nomeVeiculoAtual =
    null;


// =========================================================
// INÍCIO
// =========================================================

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    criarInterface();
    configurarShizuku();
}


// =========================================================
// RESULTADO DOS SELETORES
// =========================================================

@Override
protected void onActivityResult(
    int requestCode,
    int resultCode,
    Intent data) {

    super.onActivityResult(
        requestCode,
        resultCode,
        data
    );

    if (
        requestCode == ABRIR_ARQUIVO &&
        resultCode == RESULT_OK &&
        data != null
    ) {

        Uri arquivo =
            data.getData();

        if (arquivo != null) {

            lerHandling(arquivo);
        }

        return;
    }

    if (
        requestCode == EXPORTAR_ARQUIVO &&
        resultCode == RESULT_OK &&
        data != null
    ) {

        Uri destino =
            data.getData();

        if (destino != null) {

            exportarHandling(destino);
        }
    }
}


// =========================================================
// LER HANDLING.CFG
// =========================================================

private void lerHandling(
    Uri arquivo) {

    linhasHandling.clear();

    nomesVeiculos.clear();

    carros.clear();
    motos.clear();
    aereos.clear();
    maritimos.clear();

    linhasPorVeiculo.clear();
    linhasOriginais.clear();

    nomeVeiculoAtual = null;

    try {

        InputStream entrada =
            getContentResolver()
            .openInputStream(arquivo);

        if (entrada == null) {

            throw new Exception(
                "Não foi possível abrir o arquivo."
            );
        }

        BufferedReader leitor =
            new BufferedReader(
                new InputStreamReader(
                    entrada,
                    "UTF-8"
                )
            );

        String linha;

        while (
            (linha = leitor.readLine()) != null
        ) {

            linhasHandling.add(linha);

            String nome =
                obterNomeVeiculo(linha);

            if (nome != null) {

                nomesVeiculos.add(nome);

                String chave =
                    nome.toUpperCase();

                linhasPorVeiculo.put(
                    chave,
                    linha
                );

                linhasOriginais.put(
                    chave,
                    linha
                );

                String tipo =
                    identificarTipo(linha);

                if (tipo.equals("MOTO")) {

                    motos.add(nome);

                } else if (tipo.equals("AEREO")) {

                    aereos.add(nome);

                } else if (tipo.equals("MARITIMO")) {

                    maritimos.add(nome);

                } else {

                    carros.add(nome);
                }
            }
        }

        leitor.close();

        status.setText(
            "● handling.cfg carregado - "
            + nomesVeiculos.size()
            + " veículos encontrados"
        );

        status.setTextColor(VERDE);

        ToastMessage(
            "Handling carregado com sucesso!"
        );

    } catch (Exception e) {

        status.setText(
            "● Erro ao carregar handling.cfg"
        );

        status.setTextColor(VERMELHO);

        ToastMessage(
            "Erro ao ler o arquivo."
        );
    }
}


// =========================================================
// IDENTIFICAR TIPO
// =========================================================

private String identificarTipo(
    String linha) {

    String[] partes =
        linha.trim()
        .split("\\s+");

    if (partes.length < 33) {

        return "CARRO";
    }

    String flagsTexto =
        partes[31];

    try {

        long flags =
            Long.parseLong(
                flagsTexto,
                16
            );

        if (
            (flags & 0x01000000L) != 0
        ) {

            return "MOTO";
        }

        if (
            (flags & 0x02000000L) != 0
        ) {

            return "AEREO";
        }

        if (
            (flags & 0x04000000L) != 0
        ) {

            return "AEREO";
        }

        if (
            (flags & 0x08000000L) != 0
        ) {

            return "MARITIMO";
        }

    } catch (Exception e) {

        return "CARRO";
    }

    return "CARRO";
}


// =========================================================
// OBTER NOME
// =========================================================

private String obterNomeVeiculo(
    String linha) {

    if (linha == null) {

        return null;
    }

    linha =
        linha.trim();

    if (linha.length() == 0) {

        return null;
    }

    if (linha.startsWith(";")) {

        return null;
    }

    if (linha.startsWith(">")) {

        return null;
    }

    String[] partes =
        linha.split("\\s+");

    if (partes.length < 21) {

        return null;
    }

    String nome =
        partes[0];

    if (nome.length() > 14) {

        return null;
    }

    return nome;
}


// =========================================================
// INTERFACE
// =========================================================

private void criarInterface() {

    ScrollView scroll =
        new ScrollView(this);

    scroll.setBackgroundColor(FUNDO);

    layoutPrincipal =
        new LinearLayout(this);

    layoutPrincipal.setOrientation(
        LinearLayout.VERTICAL
    );

    layoutPrincipal.setPadding(
        30,
        30,
        30,
        30
    );

    scroll.addView(layoutPrincipal);


    TextView titulo =
        new TextView(this);

    titulo.setText(
        "GTA SA Handling Editor"
    );

    titulo.setTextColor(TEXTO);

    titulo.setTextSize(26);

    titulo.setTypeface(
        Typeface.DEFAULT,
        Typeface.BOLD
    );

    titulo.setGravity(Gravity.CENTER);

    layoutPrincipal.addView(titulo);


    TextView subtitulo =
        new TextView(this);

    subtitulo.setText(
        "Editor de handling.cfg para Android"
    );

    subtitulo.setTextColor(
        TEXTO_SECUNDARIO
    );

    subtitulo.setTextSize(14);

    subtitulo.setGravity(Gravity.CENTER);

    layoutPrincipal.addView(subtitulo);

    adicionarEspaco(25);


    adicionarTitulo("ARQUIVO");


    Button abrir =
        criarBotao(
            "📂  Abrir handling.cfg"
        );

    abrir.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent =
                    new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                    );

                intent.addCategory(
                    Intent.CATEGORY_OPENABLE
                );

                intent.setType("*/*");

                startActivityForResult(
                    intent,
                    ABRIR_ARQUIVO
                );
            }
        }
    );

    layoutPrincipal.addView(abrir);


    Button exportar =
        criarBotao(
            "💾  Exportar handling.cfg"
        );

    exportar.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                iniciarExportacao();
            }
        }
    );

    layoutPrincipal.addView(exportar);


    // =====================================================
    // SUBSTITUIÇÃO DIRETA VIA SHIZUKU
    // =====================================================

    Button substituir =
        criarBotao(
            "🔄  Substituir handling no jogo"
        );

    substituir.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                iniciarSubstituicao();
            }
        }
    );

    layoutPrincipal.addView(substituir);

    adicionarEspaco(20);


    adicionarTitulo(
        "CATEGORIA DE VEÍCULO"
    );


    Button botaoCarros =
        criarBotao("🚗  Carros");

    Button botaoMotos =
        criarBotao("🏍️  Motos");

    Button botaoAereos =
        criarBotao("✈️  Aéreos");

    Button botaoMaritimos =
        criarBotao("🚤  Marítimos");


    layoutPrincipal.addView(botaoCarros);
    layoutPrincipal.addView(botaoMotos);
    layoutPrincipal.addView(botaoAereos);
    layoutPrincipal.addView(botaoMaritimos);


    botaoCarros.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                categoriaAtual = "CARRO";

                abrirDialogoVeiculos(
                    "🚗  Selecionar carro",
                    carros
                );
            }
        }
    );


    botaoMotos.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                categoriaAtual = "MOTO";

                abrirDialogoVeiculos(
                    "🏍️  Selecionar moto",
                    motos
                );
            }
        }
    );


    botaoAereos.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                categoriaAtual = "AEREO";

                abrirDialogoVeiculos(
                    "✈️  Selecionar aéreo",
                    aereos
                );
            }
        }
    );


    botaoMaritimos.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                categoriaAtual = "MARITIMO";

                abrirDialogoVeiculos(
                    "🚤  Selecionar marítimo",
                    maritimos
                );
            }
        }
    );


    adicionarEspaco(20);


    adicionarTitulo(
        "VEÍCULO SELECIONADO"
    );


    veiculoSelecionado =
        new TextView(this);

    veiculoSelecionado.setText(
        "Nenhum veículo selecionado"
    );

    veiculoSelecionado.setTextColor(
        TEXTO_SECUNDARIO
    );

    veiculoSelecionado.setTextSize(16);

    veiculoSelecionado.setPadding(
        10,
        20,
        10,
        20
    );

    layoutPrincipal.addView(
        veiculoSelecionado
    );


    adicionarTitulo(
        "PARÂMETROS DO VEÍCULO"
    );


    valorVelocidade =
        adicionarCampoParametro(
            "Velocidade máxima",
            "—"
        );

    padraoVelocidade =
        adicionarTextoPadrao();


    valorFreio =
        adicionarCampoParametro(
            "Freio",
            "—"
        );

    padraoFreio =
        adicionarTextoPadrao();


    valorAceleracao =
        adicionarCampoParametro(
            "Força de aceleração",
            "—"
        );

    padraoAceleracao =
        adicionarTextoPadrao();


    valorDirecao =
        adicionarCampoParametro(
            "Ângulo de virar direção",
            "—"
        );

    padraoDirecao =
        adicionarTextoPadrao();


    valorTracao =
        adicionarCampoParametro(
            "Aderência",
            "—"
        );

    padraoTracao =
        adicionarTextoPadrao();


    adicionarEspaco(15);


    adicionarTitulo("AÇÕES");


    Button salvar =
        criarBotao(
            "💾  Salvar alterações"
        );

    salvar.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                salvarAlteracoes();
            }
        }
    );

    layoutPrincipal.addView(salvar);


    Button restaurar =
        criarBotao(
            "↩  Restaurar padrão"
        );

    restaurar.setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                restaurarAlteracoes();
            }
        }
    );

    layoutPrincipal.addView(restaurar);

    adicionarEspaco(25);


    adicionarTitulo("STATUS");


    status =
        new TextView(this);

    status.setText(
        "● Nenhum handling.cfg carregado"
    );

    status.setTextColor(
        TEXTO_SECUNDARIO
    );

    status.setTextSize(14);

    status.setPadding(
        10,
        10,
        10,
        20
    );

    layoutPrincipal.addView(status);


    setContentView(scroll);
}


// =========================================================
// EXPORTAÇÃO
// =========================================================

private void iniciarExportacao() {

    if (
        linhasHandling == null ||
        linhasHandling.size() == 0
    ) {

        ToastMessage(
            "Abra um handling.cfg primeiro."
        );

        return;
    }

    Intent intent =
        new Intent(
            Intent.ACTION_CREATE_DOCUMENT
        );

    intent.addCategory(
        Intent.CATEGORY_OPENABLE
    );

    intent.setType("text/plain");

    intent.putExtra(
        Intent.EXTRA_TITLE,
        "handling.cfg"
    );

    startActivityForResult(
        intent,
        EXPORTAR_ARQUIVO
    );
}


// =========================================================
// INICIAR SUBSTITUIÇÃO VIA SHIZUKU
// =========================================================

private void iniciarSubstituicao() {

    if (
        linhasHandling == null ||
        linhasHandling.size() == 0
    ) {

        ToastMessage(
            "Abra um handling.cfg primeiro."
        );

        return;
    }


    if (!Shizuku.pingBinder()) {

        status.setText(
            "● Shizuku não está disponível"
        );

        status.setTextColor(
            VERMELHO
        );

        ToastMessage(
            "Abra o Shizuku e inicie o serviço."
        );

        return;
    }


    if (
        android.os.Build.VERSION.SDK_INT >= 23
    ) {

        if (
            Shizuku.checkSelfPermission()
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            status.setText(
                "● Aguardando autorização do Shizuku"
            );

            status.setTextColor(
                TEXTO_SECUNDARIO
            );

            ToastMessage(
                "Autorize o Handling Editor no Shizuku."
            );

            Shizuku.requestPermission(
                SHIZUKU_PERMISSION_CODE
            );

            return;
        }
    }


    substituirHandlingViaShizuku();
}


// =========================================================
// RESULTADO DA AUTORIZAÇÃO SHIZUKU
// =========================================================

private void configurarShizuku() {

    Shizuku.addRequestPermissionResultListener(
        new Shizuku.OnRequestPermissionResultListener() {

            @Override
            public void onRequestPermissionResult(
                int requestCode,
                int grantResult) {

                if (
                    requestCode
                    != SHIZUKU_PERMISSION_CODE
                ) {

                    return;
                }


                if (
                    grantResult
                    == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {

                    runOnUiThread(
                        new Runnable() {

                            @Override
                            public void run() {

                                ToastMessage(
                                    "Shizuku autorizado!"
                                );

                                substituirHandlingViaShizuku();
                            }
                        }
                    );

                } else {

                    runOnUiThread(
                        new Runnable() {

                            @Override
                            public void run() {

                                status.setText(
                                    "● Permissão do Shizuku negada"
                                );

                                status.setTextColor(
                                    VERMELHO
                                );

                                ToastMessage(
                                    "Permissão do Shizuku negada."
                                );
                            }
                        }
                    );
                }
            }
        }
    );
}


// =========================================================
// SUBSTITUIR HANDLING
// =========================================================

private void substituirHandlingViaShizuku() {

    status.setText(
        "● Preparando handling para o jogo..."
    );

    status.setTextColor(
        TEXTO_SECUNDARIO
    );


    new Thread(
        new Runnable() {

            @Override
            public void run() {

                try {

                    File temporario =
                        new File(
                            getExternalFilesDir(null),
                            "handling.cfg"
                        );


                    escreverArquivoTemporario(
                        temporario
                    );


                    String origem =
                        temporario.getAbsolutePath();


                    String comando =
                        "cp \"" +
                        origem +
                        "\" \"" +
                        HANDLING_DATA +
                        "\" && " +
                        "cp \"" +
                        origem +
                        "\" \"" +
                        HANDLING_SAMP +
                        "\"";


                    java.lang.Process processo =
                        Shizuku.newProcess(
                            new String[] {
                                "sh",
                                "-c",
                                comando
                            },
                            null,
                            null
                        );


                    int resultado =
                        processo.waitFor();


                    final int codigo =
                        resultado;


                    if (codigo == 0) {

                        runOnUiThread(
                            new Runnable() {

                                @Override
                                public void run() {

                                    status.setText(
                                        "● Handling substituído na DATA e SAMP"
                                    );

                                    status.setTextColor(
                                        VERDE
                                    );

                                    ToastMessage(
                                        "Handling substituído nos dois diretórios!"
                                    );
                                }
                            }
                        );

                    } else {

                        runOnUiThread(
                            new Runnable() {

                                @Override
                                public void run() {

                                    status.setText(
                                        "● Shizuku não conseguiu substituir o handling"
                                    );

                                    status.setTextColor(
                                        VERMELHO
                                    );

                                    ToastMessage(
                                        "Erro ao substituir o handling. Código: "
                                        + codigo
                                    );
                                }
                            }
                        );
                    }


                    temporario.delete();


                } catch (final Exception e) {

                    runOnUiThread(
                        new Runnable() {

                            @Override
                            public void run() {

                                status.setText(
                                    "● Erro ao executar Shizuku"
                                );

                                status.setTextColor(
                                    VERMELHO
                                );

                                ToastMessage(
                                    "Erro: "
                                    + e.getMessage()
                                );
                            }
                        }
                    );
                }
            }
        }
    ).start();
}


// =========================================================
// CRIAR HANDLING TEMPORÁRIO
// =========================================================

private void escreverArquivoTemporario(
    File arquivo)
    throws Exception {

    OutputStream saida =
        new java.io.FileOutputStream(
            arquivo
        );


    BufferedWriter escritor =
        new BufferedWriter(
            new OutputStreamWriter(
                saida,
                "UTF-8"
            )
        );


    for (
        int i = 0;
        i < linhasHandling.size();
        i++
    ) {

        String linha =
            linhasHandling.get(i);


        String nome =
            obterNomeVeiculo(linha);


        if (nome != null) {

            String novaLinha =
                linhasPorVeiculo.get(
                    nome.toUpperCase()
                );


            if (novaLinha != null) {

                linha = novaLinha;
            }
        }


        escritor.write(linha);

        escritor.newLine();
    }


    escritor.flush();

    escritor.close();
}


// =========================================================
// EXPORTAR HANDLING
// =========================================================

private void exportarHandling(
    Uri destino) {

    if (
        linhasHandling == null ||
        linhasHandling.size() == 0
    ) {

        ToastMessage(
            "Nenhum handling carregado."
        );

        return;
    }


    boolean sucesso =
        escreverHandlingNaUri(
            destino
        );


    if (sucesso) {

        status.setText(
            "● handling.cfg exportado com sucesso"
        );

        status.setTextColor(VERDE);

        ToastMessage(
            "handling.cfg exportado com sucesso!"
        );

    } else {

        status.setText(
            "● Erro ao exportar handling.cfg"
        );

        status.setTextColor(VERMELHO);

        ToastMessage(
            "Erro ao salvar o arquivo."
        );
    }
}


// =========================================================
// ESCRITA NORMAL EM URI
// =========================================================

private boolean escreverHandlingNaUri(
    Uri destino) {

    OutputStream saida = null;

    BufferedWriter escritor = null;

    try {

        saida =
            getContentResolver()
            .openOutputStream(
                destino,
                "wt"
            );

        if (saida == null) {

            return false;
        }

        escritor =
            new BufferedWriter(
                new OutputStreamWriter(
                    saida,
                    "UTF-8"
                )
            );


        for (
            int i = 0;
            i < linhasHandling.size();
            i++
        ) {

            String linha =
                linhasHandling.get(i);


            String nome =
                obterNomeVeiculo(linha);


            if (nome != null) {

                String novaLinha =
                    linhasPorVeiculo.get(
                        nome.toUpperCase()
                    );


                if (novaLinha != null) {

                    linha = novaLinha;
                }
            }


            escritor.write(linha);

            escritor.newLine();
        }


        escritor.flush();

        return true;

    } catch (Exception e) {

        return false;

    } finally {

        try {

            if (escritor != null) {

                escritor.close();

            } else if (saida != null) {

                saida.close();
            }

        } catch (Exception ignored) {
        }
    }
}


// =========================================================
// DIÁLOGO DE VEÍCULOS
// =========================================================

private void abrirDialogoVeiculos(
    String titulo,
    final ArrayList<String> listaOriginal) {

    if (
        listaOriginal == null ||
        listaOriginal.size() == 0
    ) {

        ToastMessage(
            "Nenhum veículo encontrado."
        );

        return;
    }


    LinearLayout layout =
        new LinearLayout(this);

    layout.setOrientation(
        LinearLayout.VERTICAL
    );

    layout.setPadding(
        20,
        0,
        20,
        5
    );


    final EditText pesquisa =
        new EditText(this);

    pesquisa.setHint(
        "🔎  Pesquisar veículo..."
    );

    pesquisa.setSingleLine(true);

    pesquisa.setTextColor(Color.WHITE);

    pesquisa.setHintTextColor(
        Color.rgb(160, 160, 160)
    );

    pesquisa.setTextSize(15);

    pesquisa.setBackgroundColor(CAMPO);

    pesquisa.setPadding(
        15,
        8,
        15,
        8
    );

    layout.addView(pesquisa);


    final TextView contador =
        new TextView(this);

    contador.setTextColor(
        TEXTO_SECUNDARIO
    );

    contador.setTextSize(12);

    contador.setPadding(
        5,
        4,
        5,
        8
    );

    layout.addView(contador);


    final ArrayList<String> filtrados =
        new ArrayList<String>();

    filtrados.addAll(listaOriginal);

    contador.setText(
        criarTextoContador(
            filtrados.size()
        )
    );


    final ListaVeiculosView lista =
        new ListaVeiculosView(this);


    int alturaTela =
        getResources()
        .getDisplayMetrics()
        .heightPixels;


    lista.setAlturaMaxima(
        (int) (
            alturaTela * 0.50f
        )
    );


    final ArrayAdapter<String> adapter =
        new ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            filtrados
        ) {

            @Override
            public View getView(
                int position,
                View convertView,
                ViewGroup parent) {

                TextView view =
                    (TextView)
                    super.getView(
                        position,
                        convertView,
                        parent
                    );

                view.setTextColor(TEXTO);

                view.setTextSize(15);

                view.setPadding(
                    18,
                    12,
                    18,
                    12
                );

                view.setBackgroundColor(CARD);

                return view;
            }
        };


    lista.setAdapter(adapter);


    layout.addView(
        lista,
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    );


    final AlertDialog dialogo =
        new AlertDialog.Builder(this)
        .setTitle(titulo)
        .setView(layout)
        .setNegativeButton(
            "Cancelar",
            null
        )
        .create();


    pesquisa.addTextChangedListener(
        new TextWatcher() {

            @Override
            public void beforeTextChanged(
                CharSequence s,
                int start,
                int count,
                int after) {
            }

            @Override
            public void onTextChanged(
                CharSequence s,
                int start,
                int before,
                int count) {

                String busca =
                    s.toString()
                    .trim()
                    .toUpperCase();

                filtrados.clear();

                for (
                    int i = 0;
                    i < listaOriginal.size();
                    i++
                ) {

                    String nome =
                        listaOriginal.get(i);

                    if (
                        busca.length() == 0
                        ||
                        nome.toUpperCase()
                        .contains(busca)
                    ) {

                        filtrados.add(nome);
                    }
                }

                adapter.notifyDataSetChanged();

                contador.setText(
                    criarTextoContador(
                        filtrados.size()
                    )
                );

                lista.requestLayout();
            }

            @Override
            public void afterTextChanged(
                Editable s) {
            }
        }
    );


    lista.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(
                AdapterView<?> parent,
                View view,
                int position,
                long id) {

                if (
                    position < 0 ||
                    position >= filtrados.size()
                ) {

                    return;
                }

                String nome =
                    filtrados.get(position);

                dialogo.dismiss();

                selecionarVeiculo(nome);
            }
        }
    );


    dialogo.show();


    lista.post(
        new Runnable() {

            @Override
            public void run() {

                lista.requestLayout();
            }
        }
    );
}


// =========================================================
// CONTADOR
// =========================================================

private String criarTextoContador(
    int quantidade) {

    if (quantidade == 0) {

        return "Nenhum veículo encontrado";
    }

    if (quantidade == 1) {

        return "1 veículo encontrado";
    }

    return quantidade
        + " veículos encontrados";
}


// =========================================================
// SELECIONAR VEÍCULO
// =========================================================

private void selecionarVeiculo(
    String nome) {

    String linha =
        linhasPorVeiculo.get(
            nome.toUpperCase()
        );

    if (linha == null) {

        ToastMessage(
            "Linha do veículo não encontrada."
        );

        return;
    }

    nomeVeiculoAtual = nome;

    veiculoSelecionado.setText(
        "Veículo selecionado: "
        + nome
    );

    mostrarParametros(linha);

    ToastMessage(
        nome + " selecionado"
    );
}


// =========================================================
// MOSTRAR PARÂMETROS
// =========================================================

private void mostrarParametros(
    String linha) {

    String[] partes =
        linha.trim()
        .split("\\s+");

    if (partes.length < 21) {

        ToastMessage(
            "Linha inválida."
        );

        return;
    }

    try {

        valorVelocidade.setText(partes[12]);

        valorFreio.setText(partes[17]);

        valorAceleracao.setText(partes[13]);

        valorDirecao.setText(partes[20]);

        valorTracao.setText(partes[8]);

        atualizarValoresPadrao(
            nomeVeiculoAtual
        );

    } catch (Exception e) {

        ToastMessage(
            "Erro ao interpretar os parâmetros."
        );
    }
}


// =========================================================
// VALORES PADRÃO
// =========================================================

private void atualizarValoresPadrao(
    String nome) {

    if (nome == null) {

        return;
    }

    String original =
        linhasOriginais.get(
            nome.toUpperCase()
        );

    if (original == null) {

        return;
    }

    String[] partes =
        original.trim()
        .split("\\s+");

    if (partes.length < 21) {

        return;
    }

    padraoVelocidade.setText(
        "Padrão: "
        + partes[12]
        + " km/h"
    );

    padraoFreio.setText(
        "Padrão: "
        + partes[17]
    );

    padraoAceleracao.setText(
        "Padrão: "
        + partes[13]
    );

    padraoDirecao.setText(
        "Padrão: "
        + partes[20]
    );

    padraoTracao.setText(
        "Padrão: "
        + partes[8]
    );
}


// =========================================================
// SALVAR ALTERAÇÕES
// =========================================================

private void salvarAlteracoes() {

    if (nomeVeiculoAtual == null) {

        ToastMessage(
            "Selecione um veículo primeiro."
        );

        return;
    }

    String linhaAtual =
        linhasPorVeiculo.get(
            nomeVeiculoAtual.toUpperCase()
        );

    if (linhaAtual == null) {

        ToastMessage(
            "Linha do veículo não encontrada."
        );

        return;
    }

    String novaLinha =
        alterarLinhaHandling(linhaAtual);

    if (novaLinha == null) {

        ToastMessage(
            "Um dos valores informados é inválido."
        );

        return;
    }

    linhasPorVeiculo.put(
        nomeVeiculoAtual.toUpperCase(),
        novaLinha
    );

    atualizarLinhaNaLista(
        linhaAtual,
        novaLinha
    );

    mostrarParametros(novaLinha);

    status.setText(
        "● "
        + nomeVeiculoAtual
        + " alterado na memória"
    );

    status.setTextColor(VERDE);

    ToastMessage(
        "Alterações salvas!"
    );
}


// =========================================================
// ALTERAR LINHA
// =========================================================

private String alterarLinhaHandling(
    String linha) {

    try {

        double velocidade =
            Double.parseDouble(
                valorVelocidade
                .getText()
                .toString()
                .trim()
            );

        double freio =
            Double.parseDouble(
                valorFreio
                .getText()
                .toString()
                .trim()
            );

        double aceleracao =
            Double.parseDouble(
                valorAceleracao
                .getText()
                .toString()
                .trim()
            );

        double direcao =
            Double.parseDouble(
                valorDirecao
                .getText()
                .toString()
                .trim()
            );

        double tracao =
            Double.parseDouble(
                valorTracao
                .getText()
                .toString()
                .trim()
            );


        if (
            velocidade < 0 ||
            freio < 0 ||
            aceleracao < 0 ||
            direcao < 0 ||
            tracao < 0
        ) {

            return null;
        }


        String[] partes =
            linha.trim()
            .split("\\s+");


        if (partes.length < 21) {

            return null;
        }


        partes[8] =
            valorTracao
            .getText()
            .toString()
            .trim();

        partes[12] =
            valorVelocidade
            .getText()
            .toString()
            .trim();

        partes[13] =
            valorAceleracao
            .getText()
            .toString()
            .trim();

        partes[17] =
            valorFreio
            .getText()
            .toString()
            .trim();

        partes[20] =
            valorDirecao
            .getText()
            .toString()
            .trim();


        StringBuilder novaLinha =
            new StringBuilder();


        for (
            int i = 0;
            i < partes.length;
            i++
        ) {

            if (i > 0) {

                novaLinha.append(" ");
            }

            novaLinha.append(partes[i]);
        }


        return novaLinha.toString();

    } catch (Exception e) {

        return null;
    }
}


// =========================================================
// RESTAURAR ALTERAÇÕES
// =========================================================

private void restaurarAlteracoes() {

    if (nomeVeiculoAtual == null) {

        ToastMessage(
            "Selecione um veículo primeiro."
        );

        return;
    }


    String original =
        linhasOriginais.get(
            nomeVeiculoAtual.toUpperCase()
        );


    if (original == null) {

        ToastMessage(
            "Linha original não encontrada."
        );

        return;
    }


    String atual =
        linhasPorVeiculo.get(
            nomeVeiculoAtual.toUpperCase()
        );


    linhasPorVeiculo.put(
        nomeVeiculoAtual.toUpperCase(),
        original
    );


    atualizarLinhaNaLista(
        atual,
        original
    );


    mostrarParametros(original);


    status.setText(
        "● "
        + nomeVeiculoAtual
        + " restaurado para o padrão"
    );


    status.setTextColor(VERDE);


    ToastMessage(
        "Valores originais restaurados."
    );
}


// =========================================================
// ATUALIZAR LINHA NA LISTA
// =========================================================

private void atualizarLinhaNaLista(
    String linhaAntiga,
    String linhaNova) {

    if (linhaAntiga == null) {

        return;
    }


    for (
        int i = 0;
        i < linhasHandling.size();
        i++
    ) {

        if (
            linhasHandling
            .get(i)
            .equals(linhaAntiga)
        ) {

            linhasHandling.set(
                i,
                linhaNova
            );

            return;
        }
    }
}


// =========================================================
// CAMPO DE PARÂMETRO
// =========================================================

private EditText adicionarCampoParametro(
    String nome,
    String valor) {

    LinearLayout bloco =
        new LinearLayout(this);

    bloco.setOrientation(
        LinearLayout.VERTICAL
    );

    bloco.setBackgroundColor(CARD);

    bloco.setPadding(
        12,
        10,
        12,
        8
    );


    LinearLayout.LayoutParams blocoParams =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );


    blocoParams.setMargins(
        0,
        3,
        0,
        3
    );


    layoutPrincipal.addView(
        bloco,
        blocoParams
    );


    TextView titulo =
        new TextView(this);

    titulo.setText(nome);

    titulo.setTextColor(TEXTO);

    titulo.setTextSize(14);

    titulo.setTypeface(
        Typeface.DEFAULT,
        Typeface.BOLD
    );

    bloco.addView(titulo);


    EditText campo =
        new EditText(this);

    campo.setText(valor);

    campo.setTextColor(TEXTO);

    campo.setHintTextColor(
        TEXTO_SECUNDARIO
    );

    campo.setTextSize(16);

    campo.setSingleLine(true);

    campo.setSelectAllOnFocus(true);

    campo.setInputType(
        InputType.TYPE_CLASS_NUMBER
        |
        InputType.TYPE_NUMBER_FLAG_DECIMAL
        |
        InputType.TYPE_NUMBER_FLAG_SIGNED
    );

    campo.setBackgroundColor(CAMPO);

    campo.setPadding(
        15,
        8,
        15,
        8
    );

    bloco.addView(campo);

    return campo;
}


// =========================================================
// TEXTO PADRÃO
// =========================================================

private TextView adicionarTextoPadrao() {

    TextView texto =
        new TextView(this);

    texto.setText("Padrão: —");

    texto.setTextColor(
        TEXTO_SECUNDARIO
    );

    texto.setTextSize(12);

    texto.setPadding(
        5,
        3,
        5,
        6
    );

    layoutPrincipal.addView(texto);

    return texto;
}


// =========================================================
// CRIAR BOTÃO
// =========================================================

private Button criarBotao(
    String texto) {

    Button botao =
        new Button(this);

    botao.setText(texto);

    botao.setTextSize(15);

    botao.setTextColor(TEXTO);

    botao.setAllCaps(false);


    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );


    params.setMargins(
        0,
        8,
        0,
        8
    );


    botao.setLayoutParams(params);

    return botao;
}


// =========================================================
// TÍTULO
// =========================================================

private void adicionarTitulo(
    String texto) {

    TextView titulo =
        new TextView(this);

    titulo.setText(texto);

    titulo.setTextColor(TEXTO);

    titulo.setTextSize(14);

    titulo.setTypeface(
        Typeface.DEFAULT,
        Typeface.BOLD
    );

    titulo.setPadding(
        5,
        10,
        5,
        10
    );

    layoutPrincipal.addView(titulo);
}


// =========================================================
// ESPAÇO
// =========================================================

private void adicionarEspaco(
    int tamanho) {

    View espaco =
        new View(this);

    layoutPrincipal.addView(
        espaco,
        new LinearLayout.LayoutParams(
            1,
            tamanho
        )
    );
}


// =========================================================
// TOAST
// =========================================================

private void ToastMessage(
    String mensagem) {

    android.widget.Toast.makeText(
        MainActivity.this,
        mensagem,
        android.widget.Toast.LENGTH_SHORT
    ).show();
}

		}

// =============================================================
// LISTVIEW PERSONALIZADO
// =============================================================

class ListaVeiculosView extends ListView {

    private int alturaMaxima;

    public ListaVeiculosView(Context context) {

        super(context);

        alturaMaxima =
            (int) (
			300 *
			getResources()
			.getDisplayMetrics()
			.density
            );
    }

    public void setAlturaMaxima(int altura) {

        alturaMaxima = altura;

        requestLayout();
    }

    @Override
    protected void onMeasure(
        int widthMeasureSpec,
        int heightMeasureSpec) {

        int alturaSpec =
            MeasureSpec.makeMeasureSpec(
			alturaMaxima,
			MeasureSpec.AT_MOST
		);

        super.onMeasure(
            widthMeasureSpec,
            alturaSpec
        );
    }
}


// =============================================================
// MAIN ACTIVITY
// =============================================================

public class MainActivity extends Activity {

    // =========================================================
    // CÓDIGOS
    // =========================================================

    private static final int ABRIR_ARQUIVO = 100;

    private static final int EXPORTAR_ARQUIVO = 200;

    private static final int SUBSTITUIR_DATA = 300;

    private static final int SUBSTITUIR_SAMP = 301;


    // =========================================================
    // CORES
    // =========================================================

    private final int FUNDO =
	Color.rgb(18, 18, 18);

    private final int CARD =
	Color.rgb(30, 30, 30);

    private final int CAMPO =
	Color.rgb(45, 45, 45);

    private final int TEXTO =
	Color.WHITE;

    private final int TEXTO_SECUNDARIO =
	Color.rgb(180, 180, 180);

    private final int VERDE =
	Color.rgb(100, 220, 100);

    private final int VERMELHO =
	Color.rgb(220, 80, 80);


    // =========================================================
    // INTERFACE
    // =========================================================

    private LinearLayout layoutPrincipal;

    private TextView status;

    private TextView veiculoSelecionado;

    private EditText valorVelocidade;
    private EditText valorAceleracao;
    private EditText valorFreio;
    private EditText valorDirecao;
    private EditText valorTracao;

    private TextView padraoVelocidade;
    private TextView padraoAceleracao;
    private TextView padraoFreio;
    private TextView padraoDirecao;
    private TextView padraoTracao;


    // =========================================================
    // DADOS DO HANDLING
    // =========================================================

    private ArrayList<String> linhasHandling =
	new ArrayList<String>();

    private ArrayList<String> nomesVeiculos =
	new ArrayList<String>();

    private ArrayList<String> carros =
	new ArrayList<String>();

    private ArrayList<String> motos =
	new ArrayList<String>();

    private ArrayList<String> aereos =
	new ArrayList<String>();

    private ArrayList<String> maritimos =
	new ArrayList<String>();

    private HashMap<String, String> linhasPorVeiculo =
	new HashMap<String, String>();

    private HashMap<String, String> linhasOriginais =
	new HashMap<String, String>();

    private String categoriaAtual =
	"CARRO";


    // =========================================================
    // VEÍCULO ATUAL
    // =========================================================

    private String nomeVeiculoAtual =
	null;


    // =========================================================
    // URI DOS DOIS HANDLINGS DO JOGO
    // =========================================================

    private Uri handlingDataUri =
	null;

    private Uri handlingSampUri =
	null;


    // =========================================================
    // INÍCIO
    // =========================================================

    @Override
    protected void onCreate(
        Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        criarInterface();
    }


    // =========================================================
    // RESULTADO DOS SELETORES
    // =========================================================

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        );


        // =====================================================
        // ABRIR HANDLING
        // =====================================================

        if (
            requestCode == ABRIR_ARQUIVO &&
            resultCode == RESULT_OK &&
            data != null
			) {

            Uri arquivo =
                data.getData();

            if (arquivo != null) {

                lerHandling(
                    arquivo
                );
            }

            return;
        }


        // =====================================================
        // EXPORTAR
        // =====================================================

        if (
            requestCode == EXPORTAR_ARQUIVO &&
            resultCode == RESULT_OK &&
            data != null
			) {

            Uri destino =
                data.getData();

            if (destino != null) {

                exportarHandling(
                    destino
                );
            }

            return;
        }


        // =====================================================
        // HANDLING DATA
        // =====================================================

        if (
            requestCode == SUBSTITUIR_DATA &&
            resultCode == RESULT_OK &&
            data != null
			) {

            Uri arquivo =
                data.getData();

            if (arquivo == null) {

                ToastMessage(
                    "Arquivo DATA não selecionado."
                );

                return;
            }


            handlingDataUri =
                arquivo;


            /*
             * Agora abre automaticamente
             * o segundo arquivo.
             */

            ToastMessage(
                "Agora selecione o handling.cfg da pasta SAMP."
            );


            Intent intent =
                new Intent(
				Intent.ACTION_OPEN_DOCUMENT
			);

            intent.addCategory(
                Intent.CATEGORY_OPENABLE
            );

            intent.setType(
                "*/*"
            );

            startActivityForResult(
                intent,
                SUBSTITUIR_SAMP
            );


            return;
        }


        // =====================================================
        // HANDLING SAMP
        // =====================================================

        if (
            requestCode == SUBSTITUIR_SAMP &&
            resultCode == RESULT_OK &&
            data != null
			) {

            Uri arquivo =
                data.getData();

            if (arquivo == null) {

                ToastMessage(
                    "Arquivo SAMP não selecionado."
                );

                return;
            }


            handlingSampUri =
                arquivo;


            /*
             * Os dois arquivos foram escolhidos.
             * Agora substituímos diretamente.
             */

            substituirNosDoisArquivos();

            return;
        }
    }


    // =========================================================
    // LER HANDLING.CFG
    // =========================================================

    private void lerHandling(
        Uri arquivo) {

        linhasHandling.clear();

        nomesVeiculos.clear();

        carros.clear();
        motos.clear();
        aereos.clear();
        maritimos.clear();

        linhasPorVeiculo.clear();

        linhasOriginais.clear();

        nomeVeiculoAtual = null;


        try {

            InputStream entrada =
                getContentResolver()
                .openInputStream(arquivo);


            if (entrada == null) {

                throw new Exception(
                    "Não foi possível abrir o arquivo."
                );
            }


            BufferedReader leitor =
                new BufferedReader(
				new InputStreamReader(
					entrada,
					"UTF-8"
				)
			);


            String linha;


            while (
                (linha = leitor.readLine()) != null
				) {

                linhasHandling.add(
                    linha
                );


                String nome =
                    obterNomeVeiculo(
					linha
				);


                if (nome != null) {

                    nomesVeiculos.add(
                        nome
                    );


                    String chave =
                        nome.toUpperCase();


                    linhasPorVeiculo.put(
                        chave,
                        linha
                    );


                    linhasOriginais.put(
                        chave,
                        linha
                    );


                    String tipo =
                        identificarTipo(
						linha
					);


                    if (
                        tipo.equals("MOTO")
						) {

                        motos.add(
                            nome
                        );

                    } else if (
                        tipo.equals("AEREO")
						) {

                        aereos.add(
                            nome
                        );

                    } else if (
                        tipo.equals("MARITIMO")
						) {

                        maritimos.add(
                            nome
                        );

                    } else {

                        carros.add(
                            nome
                        );
                    }
                }
            }


            leitor.close();


            status.setText(
                "● handling.cfg carregado - "
                + nomesVeiculos.size()
                + " veículos encontrados"
            );


            status.setTextColor(
                VERDE
            );


            ToastMessage(
                "Handling carregado com sucesso!"
            );


        } catch (Exception e) {

            status.setText(
                "● Erro ao carregar handling.cfg"
            );


            status.setTextColor(
                VERMELHO
            );


            ToastMessage(
                "Erro ao ler o arquivo."
            );
        }
    }


    // =========================================================
    // IDENTIFICAR TIPO
    // =========================================================

    private String identificarTipo(
        String linha) {

        String[] partes =
            linha.trim()
            .split("\\s+");


        if (
            partes.length < 33
			) {

            return "CARRO";
        }


        String flagsTexto =
            partes[31];


        try {

            long flags =
                Long.parseLong(
				flagsTexto,
				16
			);


            if (
                (flags & 0x01000000L) != 0
				) {

                return "MOTO";
            }


            if (
                (flags & 0x02000000L) != 0
				) {

                return "AEREO";
            }


            if (
                (flags & 0x04000000L) != 0
				) {

                return "AEREO";
            }


            if (
                (flags & 0x08000000L) != 0
				) {

                return "MARITIMO";
            }


        } catch (Exception e) {

            return "CARRO";
        }


        return "CARRO";
    }


    // =========================================================
    // OBTER NOME
    // =========================================================

    private String obterNomeVeiculo(
        String linha) {

        if (linha == null) {

            return null;
        }


        linha =
            linha.trim();


        if (
            linha.length() == 0
			) {

            return null;
        }


        if (
            linha.startsWith(";")
			) {

            return null;
        }


        if (
            linha.startsWith(">")
			) {

            return null;
        }


        String[] partes =
            linha.split("\\s+");


        if (
            partes.length < 21
			) {

            return null;
        }


        String nome =
            partes[0];


        if (
            nome.length() > 14
			) {

            return null;
        }


        return nome;
    }


    // =========================================================
    // INTERFACE
    // =========================================================

    private void criarInterface() {

        ScrollView scroll =
            new ScrollView(this);


        scroll.setBackgroundColor(
            FUNDO
        );


        layoutPrincipal =
            new LinearLayout(this);


        layoutPrincipal.setOrientation(
            LinearLayout.VERTICAL
        );


        layoutPrincipal.setPadding(
            30,
            30,
            30,
            30
        );


        scroll.addView(
            layoutPrincipal
        );


        // =====================================================
        // TÍTULO
        // =====================================================

        TextView titulo =
            new TextView(this);


        titulo.setText(
            "GTA SA Handling Editor"
        );


        titulo.setTextColor(
            TEXTO
        );


        titulo.setTextSize(
            26
        );


        titulo.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        );


        titulo.setGravity(
            Gravity.CENTER
        );


        layoutPrincipal.addView(
            titulo
        );


        TextView subtitulo =
            new TextView(this);


        subtitulo.setText(
            "Editor de handling.cfg para Android"
        );


        subtitulo.setTextColor(
            TEXTO_SECUNDARIO
        );


        subtitulo.setTextSize(
            14
        );


        subtitulo.setGravity(
            Gravity.CENTER
        );


        layoutPrincipal.addView(
            subtitulo
        );


        adicionarEspaco(25);


        // =====================================================
        // ARQUIVO
        // =====================================================

        adicionarTitulo(
            "ARQUIVO"
        );


        Button abrir =
            criarBotao(
			"📂  Abrir handling.cfg"
		);


        abrir.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    Intent intent =
                        new Intent(
						Intent.ACTION_OPEN_DOCUMENT
					);


                    intent.addCategory(
                        Intent.CATEGORY_OPENABLE
                    );


                    intent.setType(
                        "*/*"
                    );


                    startActivityForResult(
                        intent,
                        ABRIR_ARQUIVO
                    );
                }
            }
        );


        layoutPrincipal.addView(
            abrir
        );


        Button exportar =
            criarBotao(
			"💾  Exportar handling.cfg"
		);


        exportar.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    iniciarExportacao();
                }
            }
        );


        layoutPrincipal.addView(
            exportar
        );


        // =====================================================
        // NOVO BOTÃO
        // =====================================================

        Button substituir =
            criarBotao(
			"🔄  Substituir handling no jogo"
		);


        substituir.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    iniciarSubstituicao();
                }
            }
        );


        layoutPrincipal.addView(
            substituir
        );


        adicionarEspaco(20);


        // =====================================================
        // CATEGORIAS
        // =====================================================

        adicionarTitulo(
            "CATEGORIA DE VEÍCULO"
        );


        Button botaoCarros =
            criarBotao(
			"🚗  Carros"
		);


        Button botaoMotos =
            criarBotao(
			"🏍️  Motos"
		);


        Button botaoAereos =
            criarBotao(
			"✈️  Aéreos"
		);


        Button botaoMaritimos =
            criarBotao(
			"🚤  Marítimos"
		);


        layoutPrincipal.addView(
            botaoCarros
        );


        layoutPrincipal.addView(
            botaoMotos
        );


        layoutPrincipal.addView(
            botaoAereos
        );


        layoutPrincipal.addView(
            botaoMaritimos
        );


        botaoCarros.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    categoriaAtual =
                        "CARRO";


                    abrirDialogoVeiculos(
                        "🚗  Selecionar carro",
                        carros
                    );
                }
            }
        );


        botaoMotos.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    categoriaAtual =
                        "MOTO";


                    abrirDialogoVeiculos(
                        "🏍️  Selecionar moto",
                        motos
                    );
                }
            }
        );


        botaoAereos.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    categoriaAtual =
                        "AEREO";


                    abrirDialogoVeiculos(
                        "✈️  Selecionar aéreo",
                        aereos
                    );
                }
            }
        );


        botaoMaritimos.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    categoriaAtual =
                        "MARITIMO";


                    abrirDialogoVeiculos(
                        "🚤  Selecionar marítimo",
                        maritimos
                    );
                }
            }
        );


        adicionarEspaco(20);


        // =====================================================
        // VEÍCULO SELECIONADO
        // =====================================================

        adicionarTitulo(
            "VEÍCULO SELECIONADO"
        );


        veiculoSelecionado =
            new TextView(this);


        veiculoSelecionado.setText(
            "Nenhum veículo selecionado"
        );


        veiculoSelecionado.setTextColor(
            TEXTO_SECUNDARIO
        );


        veiculoSelecionado.setTextSize(
            16
        );


        veiculoSelecionado.setPadding(
            10,
            20,
            10,
            20
        );


        layoutPrincipal.addView(
            veiculoSelecionado
        );


        // =====================================================
        // PARÂMETROS
        // =====================================================

        adicionarTitulo(
            "PARÂMETROS DO VEÍCULO"
        );


        valorVelocidade =
            adicionarCampoParametro(
			"Velocidade máxima",
			"—"
		);


        padraoVelocidade =
            adicionarTextoPadrao();


        valorFreio =
            adicionarCampoParametro(
			"Freio",
			"—"
		);


        padraoFreio =
            adicionarTextoPadrao();


        valorAceleracao =
            adicionarCampoParametro(
			"Força de aceleração",
			"—"
		);


        padraoAceleracao =
            adicionarTextoPadrao();


        valorDirecao =
            adicionarCampoParametro(
			"Ângulo de virar direção",
			"—"
		);


        padraoDirecao =
            adicionarTextoPadrao();


        valorTracao =
            adicionarCampoParametro(
			"Aderência",
			"—"
		);


        padraoTracao =
            adicionarTextoPadrao();


        adicionarEspaco(15);


        // =====================================================
        // AÇÕES
        // =====================================================

        adicionarTitulo(
            "AÇÕES"
        );


        Button salvar =
            criarBotao(
			"💾  Salvar alterações"
		);


        salvar.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    salvarAlteracoes();
                }
            }
        );


        layoutPrincipal.addView(
            salvar
        );


        Button restaurar =
            criarBotao(
			"↩  Restaurar padrão"
		);


        restaurar.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(
                    View v) {

                    restaurarAlteracoes();
                }
            }
        );


        layoutPrincipal.addView(
            restaurar
        );


        adicionarEspaco(25);


        // =====================================================
        // STATUS
        // =====================================================

        adicionarTitulo(
            "STATUS"
        );


        status =
            new TextView(this);


        status.setText(
            "● Nenhum handling.cfg carregado"
        );


        status.setTextColor(
            TEXTO_SECUNDARIO
        );


        status.setTextSize(
            14
        );


        status.setPadding(
            10,
            10,
            10,
            20
        );


        layoutPrincipal.addView(
            status
        );


        setContentView(
            scroll
        );
    }


    // =========================================================
    // INICIAR EXPORTAÇÃO NORMAL
    // =========================================================

    private void iniciarExportacao() {

        if (
            linhasHandling == null ||
            linhasHandling.size() == 0
			) {

            ToastMessage(
                "Abra um handling.cfg primeiro."
            );

            return;
        }


        Intent intent =
            new Intent(
			Intent.ACTION_CREATE_DOCUMENT
		);


        intent.addCategory(
            Intent.CATEGORY_OPENABLE
        );


        intent.setType(
            "text/plain"
        );


        intent.putExtra(
            Intent.EXTRA_TITLE,
            "handling.cfg"
        );


        startActivityForResult(
            intent,
            EXPORTAR_ARQUIVO
        );
    }


    // =========================================================
    // INICIAR SUBSTITUIÇÃO
    // =========================================================

    private void iniciarSubstituicao() {

        if (
            linhasHandling == null ||
            linhasHandling.size() == 0
			) {

            ToastMessage(
                "Abra um handling.cfg primeiro."
            );

            return;
        }


        handlingDataUri = null;
        handlingSampUri = null;


        /*
         * Primeiro o handling da DATA.
         */

        ToastMessage(
            "Selecione o handling.cfg da pasta DATA."
        );


        Intent intent =
            new Intent(
			Intent.ACTION_OPEN_DOCUMENT
		);


        intent.addCategory(
            Intent.CATEGORY_OPENABLE
        );


        intent.setType(
            "*/*"
        );


        startActivityForResult(
            intent,
            SUBSTITUIR_DATA
        );
    }


    // =========================================================
    // SUBSTITUIR NOS DOIS ARQUIVOS
    // =========================================================

    private void substituirNosDoisArquivos() {

        if (
            handlingDataUri == null ||
            handlingSampUri == null
			) {

            ToastMessage(
                "Os dois arquivos precisam ser selecionados."
            );

            return;
        }


        boolean dataOK =
            escreverHandlingNaUri(
			handlingDataUri
		);


        if (!dataOK) {

            status.setText(
                "● Erro ao substituir handling da DATA"
            );


            status.setTextColor(
                VERMELHO
            );


            ToastMessage(
                "Não foi possível substituir o handling da DATA."
            );

            return;
        }


        boolean sampOK =
            escreverHandlingNaUri(
			handlingSampUri
		);


        if (!sampOK) {

            status.setText(
                "● DATA substituída, mas erro na SAMP"
            );


            status.setTextColor(
                VERMELHO
            );


            ToastMessage(
                "DATA substituída. Erro ao substituir SAMP."
            );

            return;
        }


        status.setText(
            "● Handling substituído na DATA e SAMP"
        );


        status.setTextColor(
            VERDE
        );


        ToastMessage(
            "Handling substituído nos dois diretórios!"
        );
    }


    // =========================================================
    // ESCREVER HANDLING EM UMA URI
    // =========================================================

    private boolean escreverHandlingNaUri(
        Uri destino) {

        OutputStream saida = null;

        BufferedWriter escritor = null;


        try {

            /*
             * "wt" solicita que o conteúdo anterior
             * seja truncado e substituído.
             */

            saida =
                getContentResolver()
                .openOutputStream(
				destino,
				"wt"
			);


            if (saida == null) {

                return false;
            }


            escritor =
                new BufferedWriter(
				new OutputStreamWriter(
					saida,
					"UTF-8"
				)
			);


            for (
                int i = 0;
                i < linhasHandling.size();
			i++
            ) {

                String linha =
                    linhasHandling.get(i);


                String nome =
                    obterNomeVeiculo(
					linha
				);


                /*
                 * Se o veículo foi alterado,
                 * utiliza a versão modificada.
                 */

                if (nome != null) {

                    String novaLinha =
                        linhasPorVeiculo.get(
						nome.toUpperCase()
					);


                    if (novaLinha != null) {

                        linha =
                            novaLinha;
                    }
                }


                escritor.write(
                    linha
                );


                escritor.newLine();
            }


            escritor.flush();


            return true;


        } catch (Exception e) {

            return false;


        } finally {

            try {

                if (escritor != null) {

                    escritor.close();

                } else if (saida != null) {

                    saida.close();
                }

            } catch (Exception ignored) {
            }
        }
    }


    // =========================================================
    // EXPORTAR HANDLING
    // =========================================================

    private void exportarHandling(
        Uri destino) {

        if (
            linhasHandling == null ||
            linhasHandling.size() == 0
			) {

            ToastMessage(
                "Nenhum handling carregado."
            );

            return;
        }


        boolean sucesso =
            escreverHandlingNaUri(
			destino
		);


        if (sucesso) {

            status.setText(
                "● handling.cfg exportado com sucesso"
            );


            status.setTextColor(
                VERDE
            );


            ToastMessage(
                "handling.cfg exportado com sucesso!"
            );

        } else {

            status.setText(
                "● Erro ao exportar handling.cfg"
            );


            status.setTextColor(
                VERMELHO
            );


            ToastMessage(
                "Erro ao salvar o arquivo."
            );
        }
    }


    // =========================================================
    // DIÁLOGO DE VEÍCULOS
    // =========================================================

    private void abrirDialogoVeiculos(
        String titulo,
        final ArrayList<String> listaOriginal) {

        if (
            listaOriginal == null ||
            listaOriginal.size() == 0
			) {

            ToastMessage(
                "Nenhum veículo encontrado."
            );

            return;
        }


        LinearLayout layout =
            new LinearLayout(this);


        layout.setOrientation(
            LinearLayout.VERTICAL
        );


        layout.setPadding(
            20,
            0,
            20,
            5
        );


        final EditText pesquisa =
            new EditText(this);


        pesquisa.setHint(
            "🔎  Pesquisar veículo..."
        );


        pesquisa.setSingleLine(
            true
        );


        pesquisa.setTextColor(
            Color.WHITE
        );


        pesquisa.setHintTextColor(
            Color.rgb(
                160,
                160,
                160
            )
        );


        pesquisa.setTextSize(
            15
        );


        pesquisa.setBackgroundColor(
            CAMPO
        );


        pesquisa.setPadding(
            15,
            8,
            15,
            8
        );


        layout.addView(
            pesquisa
        );


        final TextView contador =
            new TextView(this);


        contador.setTextColor(
            TEXTO_SECUNDARIO
        );


        contador.setTextSize(
            12
        );


        contador.setPadding(
            5,
            4,
            5,
            8
        );


        layout.addView(
            contador
        );


        final ArrayList<String> filtrados =
            new ArrayList<String>();


        filtrados.addAll(
            listaOriginal
        );


        contador.setText(
            criarTextoContador(
                filtrados.size()
            )
        );


        final ListaVeiculosView lista =
            new ListaVeiculosView(
			this
		);


        int alturaTela =
            getResources()
            .getDisplayMetrics()
            .heightPixels;


        lista.setAlturaMaxima(
            (int) (
			alturaTela * 0.50f
            )
        );


        final ArrayAdapter<String> adapter =
            new ArrayAdapter<String>(
			this,
			android.R.layout.simple_list_item_1,
			filtrados
		) {

			@Override
			public View getView(
				int position,
				View convertView,
				ViewGroup parent) {

				TextView view =
					(TextView)
					super.getView(
					position,
					convertView,
					parent
				);


				view.setTextColor(
					TEXTO
				);


				view.setTextSize(
					15
				);


				view.setPadding(
					18,
					12,
					18,
					12
				);


				view.setBackgroundColor(
					CARD
				);


				return view;
			}
		};


        lista.setAdapter(
            adapter
        );


        layout.addView(
            lista,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        );


        final AlertDialog dialogo =
            new AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(layout)
            .setNegativeButton(
			"Cancelar",
			null
		)
            .create();


        pesquisa.addTextChangedListener(
            new TextWatcher() {

                @Override
                public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
                }


                @Override
                public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                    String busca =
                        s.toString()
                        .trim()
                        .toUpperCase();


                    filtrados.clear();


                    for (
                        int i = 0;
                        i < listaOriginal.size();
					i++
                    ) {

                        String nome =
                            listaOriginal.get(i);


                        if (
                            busca.length() == 0
                            ||
                            nome.toUpperCase()
                            .contains(busca)
							) {

                            filtrados.add(
                                nome
                            );
                        }
                    }


                    adapter.notifyDataSetChanged();


                    contador.setText(
                        criarTextoContador(
                            filtrados.size()
                        )
                    );


                    lista.requestLayout();
                }


                @Override
                public void afterTextChanged(
                    Editable s) {
                }
            }
        );


        lista.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id) {

                    if (
                        position < 0 ||
                        position >= filtrados.size()
						) {

                        return;
                    }


                    String nome =
                        filtrados.get(
						position
					);


                    dialogo.dismiss();


                    selecionarVeiculo(
                        nome
                    );
                }
            }
        );


        dialogo.show();


        lista.post(
            new Runnable() {

                @Override
                public void run() {

                    lista.requestLayout();
                }
            }
        );
    }


    // =========================================================
    // CONTADOR
    // =========================================================

    private String criarTextoContador(
        int quantidade) {

        if (
            quantidade == 0
			) {

            return "Nenhum veículo encontrado";
        }


        if (
            quantidade == 1
			) {

            return "1 veículo encontrado";
        }


        return quantidade
            + " veículos encontrados";
    }


    // =========================================================
    // SELECIONAR VEÍCULO
    // =========================================================

    private void selecionarVeiculo(
        String nome) {

        String linha =
            linhasPorVeiculo.get(
			nome.toUpperCase()
		);


        if (linha == null) {

            ToastMessage(
                "Linha do veículo não encontrada."
            );

            return;
        }


        nomeVeiculoAtual =
            nome;


        veiculoSelecionado.setText(
            "Veículo selecionado: "
            + nome
        );


        mostrarParametros(
            linha
        );


        ToastMessage(
            nome + " selecionado"
        );
    }


    // =========================================================
    // MOSTRAR PARÂMETROS
    // =========================================================

    private void mostrarParametros(
        String linha) {

        String[] partes =
            linha.trim()
            .split("\\s+");


        if (
            partes.length < 21
			) {

            ToastMessage(
                "Linha inválida."
            );

            return;
        }


        try {

            valorVelocidade.setText(
                partes[12]
            );


            valorFreio.setText(
                partes[17]
            );


            valorAceleracao.setText(
                partes[13]
            );


            valorDirecao.setText(
                partes[20]
            );


            valorTracao.setText(
                partes[8]
            );


            atualizarValoresPadrao(
                nomeVeiculoAtual
            );


        } catch (Exception e) {

            ToastMessage(
                "Erro ao interpretar os parâmetros."
            );
        }
    }


    // =========================================================
    // VALORES PADRÃO
    // =========================================================

    private void atualizarValoresPadrao(
        String nome) {

        if (nome == null) {

            return;
        }


        String original =
            linhasOriginais.get(
			nome.toUpperCase()
		);


        if (original == null) {

            return;
        }


        String[] partes =
            original.trim()
            .split("\\s+");


        if (
            partes.length < 21
			) {

            return;
        }


        padraoVelocidade.setText(
            "Padrão: "
            + partes[12]
            + " km/h"
        );


        padraoFreio.setText(
            "Padrão: "
            + partes[17]
        );


        padraoAceleracao.setText(
            "Padrão: "
            + partes[13]
        );


        padraoDirecao.setText(
            "Padrão: "
            + partes[20]
        );


        padraoTracao.setText(
            "Padrão: "
            + partes[8]
        );
    }


    // =========================================================
    // SALVAR ALTERAÇÕES
    // =========================================================

    private void salvarAlteracoes() {

        if (
            nomeVeiculoAtual == null
			) {

            ToastMessage(
                "Selecione um veículo primeiro."
            );

            return;
        }


        String linhaAtual =
            linhasPorVeiculo.get(
			nomeVeiculoAtual.toUpperCase()
		);


        if (linhaAtual == null) {

            ToastMessage(
                "Linha do veículo não encontrada."
            );

            return;
        }


        String novaLinha =
            alterarLinhaHandling(
			linhaAtual
		);


        if (novaLinha == null) {

            ToastMessage(
                "Um dos valores informados é inválido."
            );

            return;
        }


        linhasPorVeiculo.put(
            nomeVeiculoAtual.toUpperCase(),
            novaLinha
        );


        atualizarLinhaNaLista(
            linhaAtual,
            novaLinha
        );


        mostrarParametros(
            novaLinha
        );


        status.setText(
            "● "
            + nomeVeiculoAtual
            + " alterado na memória"
        );


        status.setTextColor(
            VERDE
        );


        ToastMessage(
            "Alterações salvas!"
        );
    }


    // =========================================================
    // ALTERAR LINHA
    // =========================================================

    private String alterarLinhaHandling(
        String linha) {

        try {

            double velocidade =
                Double.parseDouble(
				valorVelocidade
				.getText()
				.toString()
				.trim()
			);


            double freio =
                Double.parseDouble(
				valorFreio
				.getText()
				.toString()
				.trim()
			);


            double aceleracao =
                Double.parseDouble(
				valorAceleracao
				.getText()
				.toString()
				.trim()
			);


            double direcao =
                Double.parseDouble(
				valorDirecao
				.getText()
				.toString()
				.trim()
			);


            double tracao =
                Double.parseDouble(
				valorTracao
				.getText()
				.toString()
				.trim()
			);


            if (
                velocidade < 0 ||
                freio < 0 ||
                aceleracao < 0 ||
                direcao < 0 ||
                tracao < 0
				) {

                return null;
            }


            String[] partes =
                linha.trim()
                .split("\\s+");


            if (
                partes.length < 21
				) {

                return null;
            }


            partes[8] =
                valorTracao
                .getText()
                .toString()
                .trim();


            partes[12] =
                valorVelocidade
                .getText()
                .toString()
                .trim();


            partes[13] =
                valorAceleracao
                .getText()
                .toString()
                .trim();


            partes[17] =
                valorFreio
                .getText()
                .toString()
                .trim();


            partes[20] =
                valorDirecao
                .getText()
                .toString()
                .trim();


            StringBuilder novaLinha =
                new StringBuilder();


            for (
                int i = 0;
                i < partes.length;
			i++
            ) {

                if (i > 0) {

                    novaLinha.append(
                        " "
                    );
                }


                novaLinha.append(
                    partes[i]
                );
            }


            return novaLinha.toString();


        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // RESTAURAR ALTERAÇÕES
    // =========================================================

    private void restaurarAlteracoes() {

        if (
            nomeVeiculoAtual == null
			) {

            ToastMessage(
                "Selecione um veículo primeiro."
            );

            return;
        }


        String original =
            linhasOriginais.get(
			nomeVeiculoAtual.toUpperCase()
		);


        if (original == null) {

            ToastMessage(
                "Linha original não encontrada."
            );

            return;
        }


        String atual =
            linhasPorVeiculo.get(
			nomeVeiculoAtual.toUpperCase()
		);


        linhasPorVeiculo.put(
            nomeVeiculoAtual.toUpperCase(),
            original
        );


        atualizarLinhaNaLista(
            atual,
            original
        );


        mostrarParametros(
            original
        );


        status.setText(
            "● "
            + nomeVeiculoAtual
            + " restaurado para o padrão"
        );


        status.setTextColor(
            VERDE
        );


        ToastMessage(
            "Valores originais restaurados."
        );
    }


    // =========================================================
    // ATUALIZAR LINHA NA LISTA
    // =========================================================

    private void atualizarLinhaNaLista(
        String linhaAntiga,
        String linhaNova) {

        if (
            linhaAntiga == null
			) {

            return;
        }


        for (
            int i = 0;
            i < linhasHandling.size();
		i++
        ) {

            if (
                linhasHandling
                .get(i)
                .equals(linhaAntiga)
				) {

                linhasHandling.set(
                    i,
                    linhaNova
                );

                return;
            }
        }
    }


    // =========================================================
    // CAMPO DE PARÂMETRO
    // =========================================================

    private EditText adicionarCampoParametro(
        String nome,
        String valor) {

        LinearLayout bloco =
            new LinearLayout(this);


        bloco.setOrientation(
            LinearLayout.VERTICAL
        );


        bloco.setBackgroundColor(
            CARD
        );


        bloco.setPadding(
            12,
            10,
            12,
            8
        );


        LinearLayout.LayoutParams blocoParams =
            new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		);


        blocoParams.setMargins(
            0,
            3,
            0,
            3
        );


        layoutPrincipal.addView(
            bloco,
            blocoParams
        );


        TextView titulo =
            new TextView(this);


        titulo.setText(
            nome
        );


        titulo.setTextColor(
            TEXTO
        );


        titulo.setTextSize(
            14
        );


        titulo.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        );


        bloco.addView(
            titulo
        );


        EditText campo =
            new EditText(this);


        campo.setText(
            valor
        );


        campo.setTextColor(
            TEXTO
        );


        campo.setHintTextColor(
            TEXTO_SECUNDARIO
        );


        campo.setTextSize(
            16
        );


        campo.setSingleLine(
            true
        );


        campo.setSelectAllOnFocus(
            true
        );


        campo.setInputType(
            InputType.TYPE_CLASS_NUMBER
            |
            InputType.TYPE_NUMBER_FLAG_DECIMAL
            |
            InputType.TYPE_NUMBER_FLAG_SIGNED
        );


        campo.setBackgroundColor(
            CAMPO
        );


        campo.setPadding(
            15,
            8,
            15,
            8
        );


        bloco.addView(
            campo
        );


        return campo;
    }


    // =========================================================
    // TEXTO PADRÃO
    // =========================================================

    private TextView adicionarTextoPadrao() {

        TextView texto =
            new TextView(this);


        texto.setText(
            "Padrão: —"
        );


        texto.setTextColor(
            TEXTO_SECUNDARIO
        );


        texto.setTextSize(
            12
        );


        texto.setPadding(
            5,
            3,
            5,
            6
        );


        layoutPrincipal.addView(
            texto
        );


        return texto;
    }


    // =========================================================
    // CRIAR BOTÃO
    // =========================================================

    private Button criarBotao(
        String texto) {

        Button botao =
            new Button(this);


        botao.setText(
            texto
        );


        botao.setTextSize(
            15
        );


        botao.setTextColor(
            TEXTO
        );


        botao.setAllCaps(
            false
        );


        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		);


        params.setMargins(
            0,
            8,
            0,
            8
        );


        botao.setLayoutParams(
            params
        );


        return botao;
    }


    // =========================================================
    // TÍTULO
    // =========================================================

    private void adicionarTitulo(
        String texto) {

        TextView titulo =
            new TextView(this);


        titulo.setText(
            texto
        );


        titulo.setTextColor(
            TEXTO
        );


        titulo.setTextSize(
            14
        );


        titulo.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        );


        titulo.setPadding(
            5,
            10,
            5,
            10
        );


        layoutPrincipal.addView(
            titulo
        );
    }


    // =========================================================
    // ESPAÇO
    // =========================================================

    private void adicionarEspaco(
        int tamanho) {

        View espaco =
            new View(this);


        layoutPrincipal.addView(
            espaco,
            new LinearLayout.LayoutParams(
                1,
                tamanho
            )
        );
    }


    // =========================================================
    // TOAST
    // =========================================================

    private void ToastMessage(
        String mensagem) {

        android.widget.Toast.makeText(
            MainActivity.this,
            mensagem,
            android.widget.Toast.LENGTH_SHORT
        ).show();
    }
}
