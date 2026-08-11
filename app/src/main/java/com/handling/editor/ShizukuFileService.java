package com.handling.editor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ShizukuFileService extends Service {

    private final IShizukuFileService.Stub binder =
            new IShizukuFileService.Stub() {

        @Override
        public boolean writeFile(String path, String content) {

            try {

                java.io.File arquivo =
                        new java.io.File(path);

                java.io.File pasta =
                        arquivo.getParentFile();

                if (pasta != null && !pasta.exists()) {
                    pasta.mkdirs();
                }

                java.io.BufferedWriter escritor =
                        new java.io.BufferedWriter(
                                new java.io.OutputStreamWriter(
                                        new java.io.FileOutputStream(arquivo),
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

            java.io.FileInputStream entrada = null;
            java.io.FileOutputStream saida = null;

            try {

                java.io.File origem =
                        new java.io.File(source);

                java.io.File destino =
                        new java.io.File(destination);

                java.io.File pasta =
                        destino.getParentFile();

                if (pasta != null && !pasta.exists()) {
                    pasta.mkdirs();
                }

                entrada =
                        new java.io.FileInputStream(origem);

                saida =
                        new java.io.FileOutputStream(destino);

                byte[] buffer =
                        new byte[8192];

                int quantidade;

                while (
                        (quantidade = entrada.read(buffer)) != -1
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
        public boolean fileExists(String path) {

            try {

                return new java.io.File(path).exists();

            } catch (Exception e) {

                return false;
            }
        }

        @Override
        public void destroy() {

            stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }
}
