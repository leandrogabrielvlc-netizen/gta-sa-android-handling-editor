package com.handling.editor;

interface IShizukuFileService {
    boolean copyFile(String sourcePath, String destinationPath);
    boolean writeFile(String destinationPath, String content);
}
