package com.ngse.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.bitshares.bitshareswallet.wallet.account_object;
import com.bitshares.bitshareswallet.wallet.common.ConvertUriToFilePath;
import com.bitshares.bitshareswallet.wallet.common.ErrorCode;
import com.bitshares.bitshareswallet.wallet.exception.ErrorCodeException;
import com.bitshares.bitshareswallet.wallet.exception.NetworkStatusException;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.franmontiel.localechanger.LocaleChanger;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.evrazcoin.evrazwallet.R;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ImportActivty extends AppCompatActivity {
    private Toolbar mToolbar;
    private KProgressHUD mProcessHud;
    private int mnModel;

    public static final int ACCOUNT_MODEL = 1;
    public static final int WALLET_MODEL_WIF_KEY = 2;
    public static final int WALLET_MODEL_BIN_FILE = 3;
    public static final int WALLET_MODEL_BRAIN_KEY = 4;

    private static final int SELECT_FILE_CODE = 1;

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, SELECT_FILE_CODE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_import_activty);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProcessHud = KProgressHUD.create(ImportActivty.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please Wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        findViewById(R.id.buttonImport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 将按钮转换成为进度条
                mProcessHud.show();

                String strAccount = ((EditText) findViewById(R.id.editTextAccountName)).getText().toString();
                String strPassword = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();
                String strPrivateKey = ((EditText) findViewById(R.id.editTextPrivateKey)).getText().toString();

                if (mnModel == ACCOUNT_MODEL) {
                    processImport(strAccount, strPassword, null);
                } else if (mnModel == WALLET_MODEL_WIF_KEY) {
                    processImport(strAccount, strPassword, strPrivateKey);
                } else if (mnModel == WALLET_MODEL_BIN_FILE) {
                    if (TextUtils.isEmpty(strPassword)) {
                        processErrorCode(ErrorCode.ERROR_PASSWORD_INVALID);
                        return;
                    }

                    String strFilePath = ((EditText) findViewById(R.id.editTextFilePath)).getText().toString();
                    processImport(strAccount, strPassword, strFilePath);
                } else if (mnModel == WALLET_MODEL_BRAIN_KEY) {
                    processImport(strAccount, strPassword, strPrivateKey);
                }
            }
        });


        findViewById(R.id.buttonSelectFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        ImportActivty.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    String[] strPermission = new String[1];
                    strPermission[0] = android.Manifest.permission.READ_EXTERNAL_STORAGE;
                    ActivityCompat.requestPermissions(ImportActivty.this, strPermission, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, SELECT_FILE_CODE);
                }
            }
        });

        View layoutPrivateKey = findViewById(R.id.layoutPrivateKey);
        View layoutBrainKey = findViewById(R.id.layoutBrainKey);
        View layoutFileBin = findViewById(R.id.layoutFileBin);
        View layoutAccountName = findViewById(R.id.layoutAccountName);

        mnModel = getIntent().getIntExtra("model", 0);
        switch (mnModel) {
            case ACCOUNT_MODEL:
                getSupportActionBar().setTitle(R.string.auth);
                break;
            case WALLET_MODEL_BIN_FILE:
                getSupportActionBar().setTitle(R.string.import_activity_wallet_model);
                layoutFileBin.setVisibility(View.VISIBLE);
                layoutAccountName.setVisibility(View.GONE);
                break;
            case WALLET_MODEL_BRAIN_KEY:
                layoutBrainKey.setVisibility(View.VISIBLE);
                break;
            case WALLET_MODEL_WIF_KEY:
                layoutPrivateKey.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        ((EditText) findViewById(R.id.editTextAccountName)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                sha256_object.encoder encoder = new sha256_object.encoder();
                encoder.write(s.toString().getBytes());
                findViewById(R.id.viewAvatarTo).setVisibility(View.GONE);
                loadWebView(findViewById(R.id.webViewAvatarTo), 40, encoder.result().toString());
                processGetTransferToId(s.toString(), findViewById(R.id.textViewToId));
            }
        });
    }


    private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

    @SuppressLint("CheckResult")
    private void processGetTransferToId(final String strAccount, final TextView textViewTo) {
        Flowable.just(strAccount)
                .subscribeOn(Schedulers.io())
                .map(accountName -> {
                    account_object accountObject = BitsharesWalletWraper.getInstance().get_account_object(accountName);
                    if (accountObject == null) {
                        throw new ErrorCodeException(ErrorCode.ERROR_NO_ACCOUNT_OBJECT, "it can't find the account");
                    }

                    return accountObject;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountObject -> {
                    if (isFinishing() == false) {
                        textViewTo.setText("#" + accountObject.id.get_instance());
                    }
                }, throwable -> {
                    if (throwable instanceof NetworkStatusException || throwable instanceof ErrorCodeException) {
                        if (isFinishing() == false) {
                            textViewTo.setText("#none");
                        }
                    } else {
                        throw Exceptions.propagate(throwable);
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleChanger.configureBaseContext(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_FILE_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String strFilePath = uri.getPath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String strFilePathAboveKitKat = null;
                    try {
                        strFilePathAboveKitKat = ConvertUriToFilePath.getPathFromURI(this, uri);
                    } catch (IllegalArgumentException e) {
//                        Caused by java.lang.IllegalArgumentException: column '_data' does not exist
//                            at android.database.AbstractCursor.getColumnIndexOrThrow(AbstractCursor.java:333)
//                            at android.database.CursorWrapper.getColumnIndexOrThrow(CursorWrapper.java:87)
//                            at com.bitshares.bitshareswallet.wallet.common.ConvertUriToFilePath.getDataColumn(ConvertUriToFilePath.java:112)
//                            at com.bitshares.bitshareswallet.wallet.common.ConvertUriToFilePath.getPathFromURI(ConvertUriToFilePath.java:79)
//                            at com.bitshares.bitshareswallet.ImportActivty.onActivityResult(ImportActivty.java:156)
                        e.printStackTrace();
                    }
                    if (!TextUtils.isEmpty(strFilePathAboveKitKat)) {
                        strFilePath = strFilePathAboveKitKat;
                    }
                }

                EditText editTextFilePath = (EditText) findViewById(R.id.editTextFilePath);
                editTextFilePath.setText(strFilePath);
            }
        }
    }

    private void processImport(final String strAccount,
                               final String strPassword,
                               final String strVariant) {
        final TextView textView = (TextView) findViewById(R.id.textViewErrorInfo);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int nRet = BitsharesWalletWraper.getInstance().build_connect();
                if (nRet != 0) {
                    // // TODO: 01/09/2017 连接失败的处理
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProcessHud.dismiss();
                            textView.setText(R.string.import_activity_connect_failed);
                        }
                    });
                    return;
                }
                if (mnModel == WALLET_MODEL_WIF_KEY) {
                    nRet = BitsharesWalletWraper.getInstance().import_key(
                            strAccount,
                            strPassword,
                            strVariant
                    );
                } else if (mnModel == ACCOUNT_MODEL) {
                    nRet = BitsharesWalletWraper.getInstance().import_account_password(
                            strAccount,
                            strPassword
                    );
                } else if (mnModel == WALLET_MODEL_BRAIN_KEY) {
                    nRet = BitsharesWalletWraper.getInstance().import_brain_key(
                            strAccount,
                            strPassword,
                            strVariant
                    );
                } else if (mnModel == WALLET_MODEL_BIN_FILE) {
                    nRet = BitsharesWalletWraper.getInstance().import_file_bin(
                            strPassword,
                            strVariant
                    );
                }

                if (nRet == 0) {
                    mProcessHud.dismiss();
                    Intent intent = new Intent(ImportActivty.this, NewMainActivity.class);
//                    Intent intent = new Intent(ImportActivty.this, MainActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    processErrorCode(nRet);
                }
            }
        }).start();
    }

    private void processErrorCode(final int nRet) {
        final TextView textView = (TextView) findViewById(R.id.textViewErrorInfo);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessHud.dismiss();
                if (nRet == ErrorCode.ERROR_FILE_NOT_FOUND) {
                    textView.setText(R.string.import_activity_file_failed);
                } else if (nRet == ErrorCode.ERROR_FILE_READ_FAIL) {
                    textView.setText(R.string.import_activity_file_failed);
                } else if (nRet == ErrorCode.ERROR_NO_ACCOUNT_OBJECT) {
                    textView.setText(R.string.import_activity_account_name_invalid);
                } else if (nRet == ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY) {
                    textView.setText(R.string.import_activity_private_key_invalid);
                } else if (nRet == ErrorCode.ERROR_NETWORK_FAIL) {
                    textView.setText(R.string.import_activity_connect_failed);
                } else if (nRet == ErrorCode.ERROR_PASSWORD_INVALID) {
                    textView.setText(R.string.import_activity_password_invalid);
                } else if (nRet == ErrorCode.ERROR_FILE_BIN_PASSWORD_INVALID) {
                    textView.setText(R.string.import_activity_file_bin_password_invalid);
                } else if (nRet == ErrorCode.ERROR_UNKNOWN) {
                    textView.setText(R.string.import_activity_unknown_error);
                }
            }
        });
    }

}
