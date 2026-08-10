package com.handling.editor;

import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShizukuFileService extends IShizukuFileService.Stub {

    @Override
    public boolean copyFile(
            String sourcePath,
            String destinationPath) {

        FileInputStream entrada = null;
        FileOutputStream saida = null;

        try {

            File origem = new File(sourcePath);
            File destino = new File(destinationPath);

            if (!origem.exists()) {
                return false;
            }

            File pasta = destino.getParentFile();

            if (pasta != null && !pasta.exists()) {
                pasta.mkdirs();
            }

            entrada = new FileInputStream(origem);
            saida = new FileOutputStream(destino, false);

            byte[] buffer = new byte[8192];

            int quantidade;

            while ((quantidade = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, quantidade);
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
    public boolean writeFile(
            String destinationPath,
            String content) {

        FileOutputStream saida = null;

        try {

            File destino =
                    new File(destinationPath);

            File pasta =
                    destino.getParentFile();

            if (pasta != null && !pasta.exists()) {
                pasta.mkdirs();
            }

            saida =
                    new FileOutputStream(
                            destino,
                            false
                    );

            saida.write(
                    content.getBytes("UTF-8")
            );

            saida.flush();

            return true;

        } catch (Exception e) {

            return false;

        } finally {

            try {

                if (saida != null) {
                    saida.close();
                }

            } catch (Exception ignored) {
            }
        }
    }
}
