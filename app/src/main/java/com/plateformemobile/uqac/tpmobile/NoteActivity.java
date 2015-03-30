package com.plateformemobile.uqac.tpmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.ImageButton;

import com.samsung.sdraw.AbstractSettingView;
import com.samsung.sdraw.CanvasView;
import com.samsung.sdraw.SettingView;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;

import java.io.File;


public class NoteActivity extends Activity {
    SCanvasView mCanvasView;
    ImageButton mPenBtn;
    ImageButton mEraserBtn;
    ImageButton mUndoBtn;
    ImageButton mRedoBtn;
    ImageButton mSaveBtn;
    ImageButton upBtn;
    ImageButton imageBtn;
    ImageButton textBtn;
    SettingView mSettingView;
    boolean isSaved = true;
    boolean autoSaveOnExit = false;
    boolean canGoBack = true;
    String location = "";
    File mFolder = null;
    ProgressDialog progressDialog;
    SharedPreferences settings;

    String DEFAULT_APP_DIRECTORY = "";
    String DEFAULT_APP_IMAGEDATA_DIRECTORY = DEFAULT_APP_DIRECTORY;

    int CAMERA_REQUEST = 1888;
    int REQUEST_CODE_SELECT_IMAGE_OBJECT = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        settings = PreferenceManager.getDefaultSharedPreferences(NoteActivity.this);
        DEFAULT_APP_DIRECTORY = settings.getString("storageLocation", "/sdcard/Memo");
        DEFAULT_APP_IMAGEDATA_DIRECTORY = DEFAULT_APP_DIRECTORY;
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.saving_dialog));
        progressDialog.setCancelable(false);
        mPenBtn = (ImageButton) findViewById(R.id.settingBtn);
        mPenBtn.setOnClickListener(mPenClickListener);
        mEraserBtn = (ImageButton) findViewById(R.id.eraseBtn);
        mEraserBtn.setOnClickListener(mEraserClickListener);
        mUndoBtn = (ImageButton) findViewById(R.id.undoBtn);
        mUndoBtn.setOnClickListener(undoNredoBtnClickListener);
        mRedoBtn = (ImageButton) findViewById(R.id.redoBtn);
        mRedoBtn.setOnClickListener(undoNredoBtnClickListener);
        mSaveBtn = (ImageButton) findViewById(R.id.saveBtn);
        mSaveBtn.setOnClickListener(saveBtnClickListener);
        upBtn = (ImageButton) findViewById(R.id.upBtn);
        upBtn.setOnClickListener(upBtnClickListener);
        imageBtn = (ImageButton) findViewById(R.id.imageBtn);
        imageBtn.setOnClickListener(imageBtnClickListener);
        textBtn = (ImageButton) findViewById(R.id.textBtn);
        textBtn.setOnClickListener(textBtnClickListener);
        mCanvasView = (SCanvasView) findViewById(R.id.canvas_view);
        mSettingView = (SettingView) findViewById(R.id.setting_view);
        mCanvasView.setSettingView(mSettingView);
        mCanvasView.setHistoryUpdateListener(historyUpdateListener);

        mFolder = new File(DEFAULT_APP_IMAGEDATA_DIRECTORY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private OnClickListener undoNredoBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v == mUndoBtn) {
                mCanvasView.undo();
            } else if (v == mRedoBtn) {
                mCanvasView.redo();
            }
            mUndoBtn.setEnabled(mCanvasView.isUndoable());
            mRedoBtn.setEnabled(mCanvasView.isRedoable());
            isSaved = false;
            mSaveBtn.setEnabled(true);
        }
    };
    OnClickListener mPenClickListener = new OnClickListener() {
        public void onClick(View v) {
            mCanvasView.changeModeTo(CanvasView.PEN_MODE);
            mPenBtn.setSelected(true);
            mEraserBtn.setSelected(false);
            textBtn.setSelected(false);
            if (mPenBtn.isSelected()) {
                if (mSettingView.isShown(AbstractSettingView.PEN_SETTING_VIEW)) {
                    mSettingView.closeView();
                } else {
                    mSettingView.showView(AbstractSettingView.PEN_SETTING_VIEW);
                }
            } else if (mSettingView.isShown(AbstractSettingView.ERASER_SETTING_VIEW)) {
                mSettingView.closeView();
            }
        }
    };
    OnClickListener mEraserClickListener = new OnClickListener() {
        public void onClick(View v) {
            mCanvasView.changeModeTo(CanvasView.ERASER_MODE);
            mEraserBtn.setSelected(true);
            mPenBtn.setSelected(false);
            textBtn.setSelected(false);
            if (mEraserBtn.isSelected()) {
                if (mSettingView.isShown(AbstractSettingView.ERASER_SETTING_VIEW)) {
                    mSettingView.closeView();
                } else {
                    mSettingView.showView(AbstractSettingView.ERASER_SETTING_VIEW);
                }
            } else {
                if (mSettingView.isShown(AbstractSettingView.PEN_SETTING_VIEW)) {
                    mSettingView.closeView();
                }
            }
        }
    };
    private HistoryUpdateListener historyUpdateListener = new HistoryUpdateListener() {
        public void onHistoryChanged(boolean bUndoable, boolean bRedoable) {
            mUndoBtn.setEnabled(bUndoable);
            mRedoBtn.setEnabled(bRedoable);
        }
    };

    private OnClickListener upBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!isSaved) {
                if (autoSaveOnExit) {
                    SaveCanvas();
                    GoUp();
                } else {
                    new AlertDialog.Builder(NoteActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(getString(R.string.save_dialog_title))
                            .setMessage(getString(R.string.save_dialog_info))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SaveCanvas();
                                    GoUp();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    GoUp();
                                }
                            })
                            .show();
                }
            } else {
                GoUp();
            }
        }
    };

    private OnClickListener saveBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
            SaveCanvas();
        }
    };

    private OnClickListener imageBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            builder.setTitle(getString(R.string.insert_image_title));
            builder.setItems(R.array.insert_image_array, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
                    } else if (item == 1) {
                        Intent galleryIntent;
                        galleryIntent = new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, REQUEST_CODE_SELECT_IMAGE_OBJECT);
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };
    private OnClickListener textBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v.equals(textBtn)) {
                mCanvasView.changeModeTo(CanvasView.TEXT_MODE);
                textBtn.setSelected(true);
                mPenBtn.setSelected(false);
                mEraserBtn.setSelected(false);
                if (mSettingView.isShown(AbstractSettingView.TEXT_SETTING_VIEW)) {
                    mSettingView.closeView();
                } else {
                    mSettingView.showView(AbstractSettingView.TEXT_SETTING_VIEW);
                }
            }
        }
    };

    public void SaveCanvas() {
        mSaveBtn.setEnabled(false);
        if (location.equals("")) {
            location = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
        }
        ContinueSave();
    }

    void GoUp() {
        ListViewLoader.addLocation = location;
        if (canGoBack) {
            this.finish();
        } else {
            Intent intent = new Intent(this, ListViewLoader.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void ContinueSave() {
        progressDialog.show();
        progressThread = new ProgressThread(handler);
        progressThread.start();
    }

    public void CompleteSave() {
        final byte[] buffer = mCanvasView.getData();
        if (buffer == null) {
            return;
        }
        if (!(mFolder.exists())) {
            mFolder.mkdirs();
        }
        FileUtilities.writeBytedata(location, buffer);
        mCanvasView.saveSAMMFile(location);
        isSaved = true;
        progressDialog.dismiss();
    }

    ProgressThread progressThread;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            CompleteSave();
        }
    };
    private class ProgressThread extends Thread {
        Handler mHandler;
        ProgressThread(Handler h) {
            mHandler = h;
        }
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            Message msg = mHandler.obtainMessage();
            mHandler.sendMessage(msg);
        }
    }

}
