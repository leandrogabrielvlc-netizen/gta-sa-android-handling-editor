package com.handling.editor;

import android.content.Context;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

public class ShizukuFileService
        extends IShizukuFileService.Stub {

    private Context context;

    public ShizukuFileService() {
    }

    public ShizukuFileService(Context context) {
        this.context = context;
    }

    @Override
    public boolean writeFile(
            String path,
            String content) {

        try {

            File arquivo =
                    new File(path);

            File pasta =
                    arquivo.getParentFile();

            if (pasta != null &&
                !pasta.exists()) {

                pasta.mkdirs();
            }

            BufferedWriter escritor =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(arquivo),
                                    "UTF-8"
                            )
                    );

            escritor.write(content);

            escritor.flush();
            escritor.close();

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public boolean copyFile(
            String source,
            String destination) {

        FileInputStream entrada = null;
        FileOutputStream saida = null;

        try {

            File origem =
                    new File(source);

            File destino =
                    new File(destination);

            File pasta =
                    destino.getParentFile();

            if (pasta != null &&
                !pasta.exists()) {

                pasta.mkdirs();
            }

            entrada =
                    new FileInputStream(origem);

            saida =
                    new FileOutputStream(destino);

            byte[] buffer =
                    new byte[8192];

            int quantidade;

            while (
                    (quantidade =
                            entrada.read(buffer)) != -1
            ) {

                saida.write(
                        buffer,
                        0,
                        quantidade
                );
            }

            saida.flush();

            return true;

        } catch (Exception e) {

            return false;

        } finally {

            try {

                if (entrada != null) {
                    entrada.close();
                }

            } catch (Exception ignored) {
            }

            try {

                if (saida != null) {
                    saida.close();
                }

            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean fileExists(
            String path) {

        try {

            return new File(path).exists();

        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public void destroy() {

        System.exit(0);
    }
        }
