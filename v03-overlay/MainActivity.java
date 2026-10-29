package com.pitnudge.app;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.graphics.Insets;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.os.Build;
import android.os.Bundle;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.CustomCredential;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int REQ_GPS = 3001;
    private static final int REQ_FILE = 3002;
    private static final String HOME = "https://appassets.androidplatform.net/assets/index.html";
    private WebView webView;
    private GeolocationPermissions.Callback gpsCallback;
    private String gpsOrigin;
    private ValueCallback<Uri[]> pendingFiles;
    private boolean cloudConfigured=false;
    private FirebaseAuth auth;
    private FirebaseFirestore cloud;
    private android.os.Handler uploadHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable pendingUpload;
    private String pendingJson;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Android 15+ draws content edge-to-edge by default. Place the WebView
        // inside a container that respects system-bar insets so the status bar
        // never overlaps the app header and the nav bar never covers buttons.
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xff0b1018);
        webView = new WebView(this);
        webView.setBackgroundColor(0xff0b1018);
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int left, top, right, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                left = bars.left; top = bars.top;
                right = bars.right; bottom = bars.bottom;
            } else {
                left = insets.getSystemWindowInsetLeft();
                top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight();
                bottom = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(left, top, right, bottom);
            return insets;
        });
        setContentView(root);
        root.requestApplyInsets();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setSupportMultipleWindows(false);
        // All app JavaScript is bundled locally. Outside links open in another app.
        webView.addJavascriptInterface(new AndroidBridge(), "NativePitNudge");
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return externalNavigation(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return externalNavigation(Uri.parse(url));
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb) {
                if (!origin.startsWith("https://appassets.androidplatform.net")) {
                    cb.invoke(origin, false, false);
                    return;
                }
                if (hasGpsPermission()) {
                    cb.invoke(origin, true, false);
                } else {
                    gpsOrigin = origin;
                    gpsCallback = cb;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_GPS);
                }
            }
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFiles != null) pendingFiles.onReceiveValue(null);
                pendingFiles = callback;
                try {
                    Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
                    chooser.setType("image/*");
                    chooser.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(chooser, "Choose vehicle or receipt photo"), REQ_FILE);
                    return true;
                } catch (Exception e) {
                    pendingFiles = null;
                    callback.onReceiveValue(null);
                    return false;
                }
            }
        });
        webView.loadUrl(HOME);
        // A separate Firebase project must be configured by the owner first.
        // Without one, sign-in buttons are disabled and local garage works normally.
        if(!FirebaseApp.getApps(this).isEmpty()){
            try {
                auth=FirebaseAuth.getInstance();
                cloud=FirebaseFirestore.getInstance();
                cloudConfigured=true;
                auth.addAuthStateListener(a -> {
                    emitAuthState();
                    if(a.getCurrentUser()!=null) fetchCloud();
                });
            } catch(Exception err){android.util.Log.e("PitNudge", "Firebase setup", err);}
        }
    }

    private boolean externalNavigation(Uri url) {
        if ("appassets.androidplatform.net".equalsIgnoreCase(url.getHost())) return false;
        String scheme = url.getScheme();
        if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)
                || "tel".equalsIgnoreCase(scheme) || "geo".equalsIgnoreCase(scheme)) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, url)); }
            catch (Exception e) { Toast.makeText(this, "No app available for this link", Toast.LENGTH_SHORT).show(); }
        }
        return true;
    }

    private boolean hasGpsPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(code, permissions, grants);
        if (code == REQ_GPS && gpsCallback != null) {
            gpsCallback.invoke(gpsOrigin, hasGpsPermission(), false);
            gpsCallback = null;
            gpsOrigin = null;
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE && pendingFiles != null) {
            Uri result = resultCode == RESULT_OK && data != null ? data.getData() : null;
            pendingFiles.onReceiveValue(result == null ? null : new Uri[]{result});
            pendingFiles = null;
        }
    }

    private void emitJs(String handler, String payload){
        String script="window.PitNudgeAccount&&window.PitNudgeAccount."+handler+"("+JSONObject.quote(payload)+")";
        runOnUiThread(() -> {if(webView!=null)webView.evaluateJavascript(script,null);});
    }
    private String authState(){
        JSONObject o=new JSONObject();
        try{
            o.put("configured", cloudConfigured);
            FirebaseUser user=auth==null?null:auth.getCurrentUser();
            if(user!=null){o.put("uid", user.getUid());o.put("email", user.getEmail()==null?"":user.getEmail());
                o.put("provider", user.getProviderId());}
        }catch(Exception e){}
        return o.toString();
    }
    private void emitAuthState(){emitJs("onAuthState", authState());}
    private void emitError(String message){emitJs("onError", message);}
    private void signInWithApple(){
        if(!cloudConfigured){emitError("Google and Apple accounts have not been configured.");return;}
        OAuthProvider.Builder builder=OAuthProvider.newBuilder("apple.com");
        builder.setScopes(java.util.Arrays.asList("email", "name"));
        if(auth.getPendingAuthResult()!=null){
            auth.getPendingAuthResult().addOnSuccessListener(result -> emitAuthState())
                .addOnFailureListener(e -> emitError("Apple sign-in failed: "+e.getMessage()));
        }else{
            auth.startActivityForSignInWithProvider(this,builder.build())
                .addOnSuccessListener(result -> emitAuthState())
                .addOnFailureListener(e -> emitError("Apple sign-in failed: "+e.getMessage()));
        }
    }
    private void signInWithGoogle(){
        if(!cloudConfigured){emitError("Cloud sign-in is not configured yet.");return;}
        int clientId=getResources().getIdentifier("default_web_client_id","string",getPackageName());
        if(clientId==0){emitError("Google sign-in needs a Firebase web client ID.");return;}
        // This is an explicit button tap, so request Google's dedicated account-picker
        // flow instead of the bottom-sheet GetGoogleIdOption (which can yield
        // NoCredentialException even with a Google account on the device).
        GetSignInWithGoogleOption option=new GetSignInWithGoogleOption.Builder(getString(clientId)).build();
        GetCredentialRequest request=new GetCredentialRequest.Builder().addCredentialOption(option).build();
        CredentialManager.create(this).getCredentialAsync(this,request,null,getMainExecutor(),
            new CredentialManagerCallback<GetCredentialResponse,GetCredentialException>(){
                @Override public void onResult(GetCredentialResponse response){
                    if(!(response.getCredential() instanceof CustomCredential)){
                        emitError("No supported Google account was selected.");return;
                    }
                    CustomCredential credential=(CustomCredential)response.getCredential();
                    if(!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())){
                        emitError("Google credential type was not supported.");return;
                    }
                    try{
                        String token=GoogleIdTokenCredential.createFrom(credential.getData()).getIdToken();
                        auth.signInWithCredential(GoogleAuthProvider.getCredential(token,null))
                            .addOnSuccessListener(result -> emitAuthState())
                            .addOnFailureListener(e -> emitError("Google sign-in failed: "+e.getMessage()));
                    }catch(Exception ex){emitError("Google token could not be read.");}
                }
                @Override public void onError(GetCredentialException error){
                    android.util.Log.w("PitNudge", "Google sign-in request failed: "+error.getType(), error);
                    if(error instanceof androidx.credentials.exceptions.NoCredentialException) {
                        emitError("Google account picker couldn't find an available account. Check your phone has a Google account in Settings, and update Google Play services.");
                    } else {
                        emitError("Google sign-in couldn't finish: "+error.getMessage());
                    }
                }
            });
    }
    private void fetchCloud(){
        if(cloud==null||auth==null||auth.getCurrentUser()==null)return;
        cloud.collection("users").document(auth.getCurrentUser().getUid()).collection("data").document("garage")
        .get().addOnSuccessListener(doc -> {
            JSONObject o=new JSONObject();try{
                o.put("found",doc.exists()&&doc.contains("json"));
                if(doc.exists()&&doc.contains("json"))o.put("json", doc.getString("json"));
                if(doc.exists()&&doc.getTimestamp("updatedAt")!=null)o.put("updatedAt", doc.getTimestamp("updatedAt").toDate().getTime());
            }catch(Exception e){}emitJs("onCloudResult",o.toString());
        }).addOnFailureListener(e -> {
            JSONObject o=new JSONObject();try{o.put("error",e.getMessage());}catch(Exception ex){}
            emitJs("onCloudResult",o.toString());
        });
    }
    private void saveCloud(String json){
        if(auth==null||cloud==null||auth.getCurrentUser()==null){emitError("Sign in first.");return;}
        if(json==null||json.length()>700000){emitError("Cloud backup is too large; export a local backup.");return;}
        pendingJson=json;
        if(pendingUpload!=null)uploadHandler.removeCallbacks(pendingUpload);
        pendingUpload=()->{
            String writeJson=pendingJson;
            String uid=auth.getCurrentUser()==null?null:auth.getCurrentUser().getUid();
            if(uid==null)return;
            Map<String,Object> data=new HashMap<>();data.put("json",writeJson);data.put("updatedAt",FieldValue.serverTimestamp());
            cloud.collection("users").document(uid).collection("data").document("garage").set(data)
                .addOnSuccessListener(v->{JSONObject o=new JSONObject();try{o.put("ok",true);}catch(Exception e){}emitJs("onCloudWrite",o.toString());})
                .addOnFailureListener(e->{JSONObject o=new JSONObject();try{o.put("ok",false);o.put("error",e.getMessage());}catch(Exception x){}emitJs("onCloudWrite",o.toString());});
        };
        uploadHandler.postDelayed(pendingUpload,1500);
    }
    private class AndroidBridge {
        @JavascriptInterface public String getAuthState(){return authState();}
        @JavascriptInterface public void signInGoogle(){runOnUiThread(()->signInWithGoogle());}
        @JavascriptInterface public void signInApple(){runOnUiThread(()->signInWithApple());}
        @JavascriptInterface public void signOut(){runOnUiThread(()->{
            if(pendingUpload!=null)uploadHandler.removeCallbacks(pendingUpload);
            if(auth!=null)auth.signOut();emitAuthState();
        });}
        @JavascriptInterface public void fetchCloud(){runOnUiThread(()->MainActivity.this.fetchCloud());}
        @JavascriptInterface public void saveCloud(String json){runOnUiThread(()->MainActivity.this.saveCloud(json));}

        @JavascriptInterface public void saveFile(String fileName, String contents, String mime) {
            String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (safeName.isEmpty()) safeName = "pitnudge-export.txt";
            try {
                OutputStream os;
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues vals = new ContentValues();
                    vals.put(MediaStore.Downloads.DISPLAY_NAME, safeName);
                    vals.put(MediaStore.Downloads.MIME_TYPE, mime == null ? "text/plain" : mime);
                    vals.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PitNudge");
                    Uri dest = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, vals);
                    if (dest == null) throw new Exception("Could not create download");
                    os = getContentResolver().openOutputStream(dest);
                } else {
                    File folder = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    if (folder == null) throw new Exception("No storage available");
                    os = new FileOutputStream(new File(folder, safeName));
                }
                if (os == null) throw new Exception("Could not open download");
                try (OutputStream stream = os) {
                    stream.write(contents.getBytes(StandardCharsets.UTF_8));
                }
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Saved export to PitNudge downloads", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Could not save export: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }
    }

    @Override public void onBackPressed() {
        // HTML tabs are a single local page; don't silently close the app mid-session.
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
