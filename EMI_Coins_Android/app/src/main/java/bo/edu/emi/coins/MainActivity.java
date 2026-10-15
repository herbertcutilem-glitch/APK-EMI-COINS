package bo.edu.emi.coins;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);   // localStorage (saldos y cuentas demo)
        s.setAllowFileAccess(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String u = r.getUrl().toString();
                if (u.startsWith("file:///android_asset/")) return false;
                if (u.startsWith("blob:") || u.startsWith("data:")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, r.getUrl())); } catch (Exception e) {}
                return true;
            }
        });
        // Exportar CSV: el WebView no descarga blobs, se guarda con este puente
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void saveCsv(String name, String base64) {
                try {
                    File dir = getExternalFilesDir(null);
                    File f = new File(dir, name);
                    FileOutputStream o = new FileOutputStream(f);
                    o.write(Base64.decode(base64, Base64.DEFAULT));
                    o.close();
                    runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Historial exportado")
                        .setMessage("Guardado en:\n" + f.getAbsolutePath())
                        .setPositiveButton("OK", null).show());
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "No se pudo guardar el CSV", Toast.LENGTH_LONG).show());
                }
            }
        }, "AndroidBridge");
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
