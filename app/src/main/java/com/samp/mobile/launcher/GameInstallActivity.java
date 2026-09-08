package com.samp.mobile.launcher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.samp.mobile.R;
import com.samp.mobile.launcher.config.Config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GameInstallActivity extends AppCompatActivity {
    private static final int REQUEST_IMPORT_TREE = 4107;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean workerRunning;
    private volatile boolean cancelled;
    private volatile HttpURLConnection activeConnection;

    private ProgressBar progressBar;
    private TextView statusText;
    private TextView percentText;
    private TextView sizeText;
    private TextView speedText;
    private TextView readyText;
    private Button downloadButton;
    private Button importButton;
    private Button cancelButton;

    public static boolean areGameFilesReady(Context context) {
        File gameDirectory = context.getExternalFilesDir(null);
        if (gameDirectory == null) return false;
        for (String required : Config.REQUIRED_GAME_FILES) {
            File file = new File(gameDirectory, required);
            if (!file.isFile() || file.length() == 0) return false;
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_install);

        progressBar = findViewById(R.id.install_progress);
        statusText = findViewById(R.id.install_status);
        percentText = findViewById(R.id.install_percent);
        sizeText = findViewById(R.id.install_size);
        speedText = findViewById(R.id.install_speed);
        readyText = findViewById(R.id.install_ready);
        downloadButton = findViewById(R.id.button_download_game);
        importButton = findViewById(R.id.button_import_game);
        cancelButton = findViewById(R.id.button_cancel_install);

        downloadButton.setOnClickListener(v -> startDownload());
        importButton.setOnClickListener(v -> openImportPicker());
        cancelButton.setOnClickListener(v -> cancelWork());
        cancelButton.setVisibility(View.GONE);
        refreshReadyState();
    }

    private void refreshReadyState() {
        boolean ready = areGameFilesReady(this);
        readyText.setText(ready ? "جاهز للدخول إلى السيرفر" : "ملفات اللعبة غير مكتملة بعد");
        readyText.setTextColor(getColor(ready ? android.R.color.holo_green_dark : android.R.color.darker_gray));
    }

    private void startDownload() {
        if (workerRunning) return;
        startWorker("download");
    }

    private void openImportPicker() {
        if (workerRunning) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_IMPORT_TREE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT_TREE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri treeUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Some document providers do not offer persistable permissions; the current copy still works.
            }
            startWorker(treeUri);
        }
    }

    private void startWorker(final String operation) {
        workerRunning = true;
        cancelled = false;
        setBusy(true);
        executor.execute(() -> {
            try {
                if ("download".equals(operation)) {
                    downloadAndInstall();
                }
            } catch (Exception error) {
                if (cancelled) {
                    postStatus("تم إلغاء العملية", false);
                } else {
                    postStatus("تعذر التثبيت: " + safeMessage(error), true);
                }
            } finally {
                workerRunning = false;
                mainHandler.post(() -> {
                    setBusy(false);
                    refreshReadyState();
                });
            }
        });
    }

    private void startWorker(final Uri treeUri) {
        workerRunning = true;
        cancelled = false;
        setBusy(true);
        executor.execute(() -> {
            try {
                importAndInstall(treeUri);
            } catch (Exception error) {
                if (cancelled) {
                    postStatus("تم إلغاء العملية", false);
                } else {
                    postStatus("تعذر الاستيراد: " + safeMessage(error), true);
                }
            } finally {
                workerRunning = false;
                mainHandler.post(() -> {
                    setBusy(false);
                    refreshReadyState();
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        mainHandler.post(() -> {
            downloadButton.setEnabled(!busy);
            importButton.setEnabled(!busy);
            cancelButton.setVisibility(busy ? View.VISIBLE : View.GONE);
            if (!busy) progressBar.setIndeterminate(false);
        });
    }

    private void cancelWork() {
        cancelled = true;
        HttpURLConnection connection = activeConnection;
        if (connection != null) connection.disconnect();
        postStatus("جارٍ إلغاء العملية...", false);
    }

    private void downloadAndInstall() throws Exception {
        File installDirectory = getInstallDirectory();
        File archive = new File(installDirectory, "game-cache.zip.part");
        deleteRecursively(archive);
        HttpURLConnection connection = null;
        try {
            postStatus("جارٍ تنزيل ملفات اللعبة كاملة...", false);
            URL url = new URL(Config.GAME_ARCHIVE_URL);
            connection = (HttpURLConnection) url.openConnection();
            activeConnection = connection;
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("Accept", "application/zip,application/octet-stream,*/*");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) throw new IOException("HTTP " + responseCode);
            long total = connection.getContentLengthLong();
            if (total <= 0) total = Config.GAME_ARCHIVE_SIZE_BYTES;
            long startedAt = System.currentTimeMillis();
            long transferred = 0;
            byte[] buffer = new byte[1024 * 1024];
            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 OutputStream output = new BufferedOutputStream(new FileOutputStream(archive))) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (cancelled) throw new IOException("cancelled");
                    output.write(buffer, 0, read);
                    transferred += read;
                    if (transferred % (4L * 1024L * 1024L) < read) {
                        publishProgress(transferred, total, startedAt);
                    }
                }
            }
            publishProgress(transferred, total, startedAt);
            if (Config.GAME_ARCHIVE_SIZE_BYTES > 0 && transferred != Config.GAME_ARCHIVE_SIZE_BYTES) {
                throw new IOException("حجم الأرشيف غير مطابق: " + formatBytes(transferred));
            }
            verifyArchiveHash(archive);
            extractAndMerge(archive);
            deleteRecursively(archive);
            postStatus("اكتمل تثبيت ملفات اللعبة", false);
        } finally {
            activeConnection = null;
            if (cancelled) deleteRecursively(archive);
            if (connection != null) connection.disconnect();
        }
    }

    private void verifyArchiveHash(File archive) throws Exception {
        postStatus("جارٍ فحص سلامة الأرشيف...", false);
        String expected = Config.GAME_ARCHIVE_SHA256 == null ? "" : Config.GAME_ARCHIVE_SHA256.trim();
        if (!isSha256(expected)) {
            postStatus("تم التنزيل؛ قيمة SHA-256 في بيانات السيرفر غير مكتملة", true);
            return;
        }
        String actual = sha256(archive);
        if (!expected.equalsIgnoreCase(actual)) {
            throw new IOException("فشل فحص SHA-256");
        }
    }

    private void importAndInstall(Uri treeUri) throws Exception {
        postStatus("جارٍ نسخ ملفات اللعبة من المجلد المحدد...", false);
        File staging = new File(getInstallDirectory(), "manual-import");
        deleteRecursively(staging);
        if (!staging.mkdirs() && !staging.isDirectory()) throw new IOException("تعذر إنشاء مساحة مؤقتة");
        DocumentFile selected = DocumentFile.fromTreeUri(this, treeUri);
        if (selected == null || !selected.isDirectory()) throw new IOException("المجلد غير صالح");
        DocumentFile source = resolveImportRoot(selected);
        copyDocuments(source, staging);
        mergeStagedFiles(staging);
        deleteRecursively(staging);
        if (!areGameFilesReady(this)) throw new IOException("لم يتم العثور على الملفات المطلوبة بعد النسخ");
        postStatus("تم استيراد ملفات اللعبة بنجاح", false);
    }

    private DocumentFile resolveImportRoot(DocumentFile selected) {
        String name = selected.getName();
        if ("files".equalsIgnoreCase(name)) return selected;
        for (DocumentFile child : selected.listFiles()) {
            if (child.isDirectory() && "files".equalsIgnoreCase(child.getName())) return child;
        }
        return selected;
    }

    private void copyDocuments(DocumentFile source, File targetDirectory) throws Exception {
        for (DocumentFile child : source.listFiles()) {
            if (cancelled) throw new IOException("cancelled");
            String safeName = safeName(child.getName());
            File target = new File(targetDirectory, safeName);
            if (child.isDirectory()) {
                if (!target.mkdirs() && !target.isDirectory()) throw new IOException("تعذر إنشاء مجلد");
                copyDocuments(child, target);
            } else if (child.isFile()) {
                try (InputStream input = getContentResolver().openInputStream(child.getUri());
                     OutputStream output = new BufferedOutputStream(new FileOutputStream(target))) {
                    if (input == null) throw new IOException("تعذر قراءة ملف");
                    copyStream(input, output);
                }
            }
        }
    }

    private void extractAndMerge(File archive) throws Exception {
        postStatus("جارٍ استخراج الملفات بأمان...", false);
        File staging = new File(getInstallDirectory(), "zip-extract");
        deleteRecursively(staging);
        if (!staging.mkdirs() && !staging.isDirectory()) throw new IOException("تعذر إنشاء مساحة الاستخراج");
        try (ZipInputStream input = new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024 * 1024];
            while ((entry = input.getNextEntry()) != null) {
                if (cancelled) throw new IOException("cancelled");
                String name = safeZipPath(entry.getName());
                File output = new File(staging, name);
                if (!isInside(staging, output)) throw new IOException("مسار ZIP غير آمن");
                if (entry.isDirectory()) {
                    if (!output.mkdirs() && !output.isDirectory()) throw new IOException("تعذر إنشاء مجلد ZIP");
                } else {
                    File parent = output.getParentFile();
                    if (parent != null && !parent.mkdirs() && !parent.isDirectory()) throw new IOException("تعذر إنشاء مجلد ZIP");
                    try (OutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(output))) {
                        copyStream(input, fileOutput, buffer);
                    }
                }
                input.closeEntry();
            }
        }
        mergeStagedFiles(new File(staging, "files").isDirectory() ? new File(staging, "files") : staging);
        deleteRecursively(staging);
        if (!areGameFilesReady(this)) throw new IOException("الأرشيف لا يحتوي على ملفات اللعبة المطلوبة");
    }

    private void mergeStagedFiles(File source) throws Exception {
        File gameDirectory = getGameDirectory();
        if (!gameDirectory.exists() && !gameDirectory.mkdirs()) throw new IOException("تعذر إنشاء مجلد اللعبة");
        copyDirectory(source, gameDirectory);
    }

    private void copyDirectory(File source, File target) throws Exception {
        File[] children = source.listFiles();
        if (children == null) throw new IOException("تعذر قراءة الملفات المؤقتة");
        for (File child : children) {
            if (cancelled) throw new IOException("cancelled");
            File destination = new File(target, child.getName());
            if (child.isDirectory()) {
                if (!destination.mkdirs() && !destination.isDirectory()) throw new IOException("تعذر إنشاء مجلد");
                copyDirectory(child, destination);
            } else {
                File partial = new File(destination.getPath() + ".part");
                try (InputStream input = new BufferedInputStream(new FileInputStream(child));
                     OutputStream output = new BufferedOutputStream(new FileOutputStream(partial))) {
                    copyStream(input, output);
                }
                if (destination.exists() && !destination.delete()) throw new IOException("تعذر استبدال ملف");
                if (!partial.renameTo(destination)) throw new IOException("تعذر تثبيت ملف");
            }
        }
    }

    private void publishProgress(final long current, final long total, final long startedAt) {
        mainHandler.post(() -> {
            progressBar.setIndeterminate(false);
            long denominator = total > 0 ? total : Config.GAME_ARCHIVE_SIZE_BYTES;
            int percent = denominator > 0 ? (int) Math.min(10000L, current * 10000L / denominator) : 0;
            progressBar.setProgress(percent);
            percentText.setText(String.format(Locale.US, "%d%%", percent / 100));
            sizeText.setText(formatBytes(current) + " / " + formatBytes(denominator));
            long elapsed = Math.max(1L, System.currentTimeMillis() - startedAt);
            speedText.setText(formatBytes((current * 1000L) / elapsed) + "/ث");
        });
    }

    private void postStatus(final String message, final boolean warning) {
        mainHandler.post(() -> {
            statusText.setText(message);
            statusText.setTextColor(getColor(warning ? android.R.color.holo_orange_dark : android.R.color.darker_gray));
        });
    }

    private File getGameDirectory() throws IOException {
        File directory = getExternalFilesDir(null);
        if (directory == null) throw new IOException("مسار ملفات اللعبة غير متاح");
        return directory;
    }

    private File getInstallDirectory() throws IOException {
        File directory = new File(getGameDirectory(), ".lv-installer");
        if (!directory.mkdirs() && !directory.isDirectory()) throw new IOException("تعذر إنشاء مساحة التثبيت");
        return directory;
    }

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        copyStream(input, output, new byte[1024 * 1024]);
    }

    private static void copyStream(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
    }

    private static String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[1024 * 1024];
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            int read;
            while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
        }
        StringBuilder result = new StringBuilder();
        for (byte value : digest.digest()) result.append(String.format(Locale.US, "%02x", value));
        return result.toString();
    }

    private static boolean isSha256(String value) {
        return value != null && value.matches("[0-9a-fA-F]{64}");
    }

    private static String safeZipPath(String path) throws IOException {
        if (path == null || path.length() == 0 || path.startsWith("/") || path.indexOf('\0') >= 0) throw new IOException("مسار ZIP غير صالح");
        String normalized = path.replace('\\', '/');
        for (String part : normalized.split("/")) {
            if (part.length() == 0 || ".".equals(part) || "..".equals(part) || part.indexOf(':') >= 0) throw new IOException("مسار ZIP غير آمن");
        }
        return normalized;
    }

    private static String safeName(String name) throws IOException {
        if (name == null || name.length() == 0 || ".".equals(name) || "..".equals(name) || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0 || name.indexOf('\0') >= 0) throw new IOException("اسم ملف غير آمن");
        return name;
    }

    private static boolean isInside(File parent, File child) throws IOException {
        String parentPath = parent.getCanonicalPath() + File.separator;
        return child.getCanonicalPath().startsWith(parentPath);
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursively(child);
        }
        file.delete();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024L * 1024L) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024L * 1024L) return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private static String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.length() == 0 ? "خطأ غير معروف" : message;
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            cancelled = true;
            HttpURLConnection connection = activeConnection;
            if (connection != null) connection.disconnect();
            executor.shutdownNow();
        }
        super.onDestroy();
    }
}
