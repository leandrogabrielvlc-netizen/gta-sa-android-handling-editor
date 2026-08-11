package com.handling.editor;

interface IShizukuFileService {

    boolean writeFile(
        String path,
        String content
    );

    boolean copyFile(
        String source,
        String destination
    );

    boolean fileExists(
        String path
    );

    void destroy();
}
