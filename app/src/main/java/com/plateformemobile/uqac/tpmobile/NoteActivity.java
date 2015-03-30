package com.plateformemobile.uqac.tpmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.samsung.samm.common.SObjectImage;
import com.samsung.sdraw.AbstractSettingView;
import com.samsung.sdraw.CanvasView;
import com.samsung.sdraw.SettingView;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


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
    boolean preventScreenLock = false;
    boolean deleteTempOnExit = false;
    String location = "";
    String background = "";
    File mFolder = null;
    ProgressDialog progressDialog;
    SharedPreferences settings;

    String DEFAULT_APP_DIRECTORY = "";
    String DEFAULT_APP_IMAGEDATA_DIRECTORY = DEFAULT_APP_DIRECTORY;

    int REQUEST_CODE_SELECT_IMAGE_OBJECT = 103;
    int CAMERA_REQUEST = 1888;
    int SELECT_PICTURE_BACKGROUND = 1341;
    int INSERT_IMAGE_FROM_PAGE = 768;

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
        preventScreenLock = settings.getBoolean("preventScreenLock", false);
        deleteTempOnExit = settings.getBoolean("deleteTempOnExit", false);
        mFolder = new File(DEFAULT_APP_IMAGEDATA_DIRECTORY);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if (extras.getString("location")!=null) {
                location = extras.getString("location");
                
            }
        } else {
            location = "";
        }

        if (preventScreenLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
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
        mSaveBtn.setEnabled(isSaved);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == INSERT_IMAGE_FROM_PAGE) {
                String imagePath = (String) data.getExtras().get("file");
                if (imagePath.length() > 0) {
                    if(!FileUtilities.bIsValidImagePath(imagePath)) {
                        return;
                    }
                    SObjectImage sImageObject = null;
                    try {
                        sImageObject = getImageFromBitmap(BitmapFactory.decodeFile(imagePath), false, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCanvasView.insertSAMMImage(sImageObject, true);
                    isSaved = false;
                    mSaveBtn.setEnabled(true);
                }
            } else if (requestCode == SELECT_PICTURE_BACKGROUND) {
                Uri imageFileUri = data.getData();
                String path = FileUtilities.getRealPathFromURI(this, imageFileUri);
                if (path != null && path.length() > 0) {
                    File file = new File(path);
                    if (file.exists() ){
                        Bitmap bitmap2 = BitmapFactory.decodeFile(path);
                        BitmapDrawable bd2 = new BitmapDrawable(bitmap2);
                        bd2.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                        mCanvasView.setBackgroundDrawable(bd2);
                    }
                }
            } else if (requestCode == REQUEST_CODE_SELECT_IMAGE_OBJECT) {
                Uri imageFileUri = data.getData();
                String imagePath = FileUtilities.getRealPathFromURI(this, imageFileUri);
                if(!FileUtilities.bIsValidImagePath(imagePath)) {
                    return;
                }
                SObjectImage sImageObject = null;
                try {
                    sImageObject = getImageFromBitmap(BitmapFactory.decodeFile(imagePath), false, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCanvasView.insertSAMMImage(sImageObject, true);
                isSaved = false;
                mSaveBtn.setEnabled(true);
            } else if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                try {
                    File mFolder = new File(DEFAULT_APP_DIRECTORY + "/.temp");
                    mFolder.mkdirs();
                    String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "temp", "png");
                    FileOutputStream out = new FileOutputStream(filename);
                    photo.compress(Bitmap.CompressFormat.PNG, 100, out);
                    String imagePath = filename;
                    if(!FileUtilities.bIsValidImagePath(imagePath)) {
                        return;
                    }
                    SObjectImage sImageObject = null;
                    try {
                        sImageObject = getImageFromBitmap(photo, false, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCanvasView.insertSAMMImage(sImageObject, true);
                    isSaved = false;
                    mSaveBtn.setEnabled(true);
                    File a = new File(filename);
                    a.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                loadCanvas(data.getExtras().getString("filename"));
            }
        }
    }

    public boolean loadCanvas(String fileName) {
        String loadPath = mFolder.getPath() + '/' + fileName;
        byte[] buffer = FileUtilities.readBytedata(loadPath);
        if (buffer == null) {
            return false;
        }
        mCanvasView.setData(buffer);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deleteTempOnExit) {
            File temp = new File(DEFAULT_APP_DIRECTORY + "/.temp");
            if (temp.exists()) {
                for (File file : temp.listFiles()) {
                    file.delete();
                }
                temp.delete();
            }
        }
        if (preventScreenLock) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        mCanvasView.closeSAMMLibrary();
    }

    Bitmap generateBackgroundTile() {
        return generateBackgroundTile(settings.getInt("backgroundType", 0), settings.getInt("backgroundTileSize", 20), settings.getInt("backgroundColour", 0));
    }
    Bitmap generateBackgroundTile(int backgroundType, int backgroundTileSize, int backgroundColour) {
        int[] colours = new int[backgroundTileSize * backgroundTileSize];
        int i = 0;
        switch (backgroundType) {
            case 0:
                for (i = 0; i < colours.length; i++) {
                    colours[i] = backgroundColour;
                }
                break;
            case 1:
                i = 0;
                for (int x = 0; x < backgroundTileSize; x++) {
                    for (int y = 0; y < backgroundTileSize; y++) {
                        if (x < backgroundTileSize - 1 && y < backgroundTileSize - 1) {
                            colours[i] = backgroundColour;
                        } else {
                            int colour = Color.red(backgroundColour)+Color.green(backgroundColour)+Color.blue(backgroundColour);
                            if (colour < 384 && colour > 0) {
                                colours[i] = Color.WHITE;
                            } else {
                                colours[i] = Color.BLACK;
                            }
                        }
                        i++;
                    }
                }
                break;
            case 2:
                i = 0;
                for (int x = 0; x < backgroundTileSize; x++) {
                    for (int y = 0; y < backgroundTileSize; y++) {
                        if (x < backgroundTileSize - 1) {
                            colours[i] = backgroundColour;
                        } else {
                            int colour = Color.red(backgroundColour)+Color.green(backgroundColour)+Color.blue(backgroundColour);
                            if (colour < 384 && colour > 0) {
                                colours[i] = Color.WHITE;
                            } else {
                                colours[i] = Color.BLACK;
                            }
                        }
                        i++;
                    }
                }
                break;
        }
        return Bitmap.createBitmap(colours, backgroundTileSize, backgroundTileSize, Bitmap.Config.ARGB_8888);
    }

    private SObjectImage getImageFromBitmap(final Bitmap bitmap, final boolean compression, final boolean scaleToCanvas) throws IOException {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        final SObjectImage image = new SObjectImage();
        RectF rect;
        int canvasWidth = mCanvasView.getWidth();
        int canvasHeight = mCanvasView.getHeight();
        if (scaleToCanvas) {
            if (imageWidth > 600) {
                imageWidth = 600;
                imageHeight /= imageWidth / 600;
            }
            if (imageHeight > 800) {
                imageHeight = 800;
                imageWidth /= imageHeight / 800;
            }
        }
        rect = new RectF((canvasWidth - imageWidth) / 2, (canvasHeight - imageHeight) / 2, (canvasWidth - imageWidth) / 2 + imageWidth, (canvasHeight - imageHeight) / 2 + imageHeight);
        image.setRect(rect);
        if (compression) {
            image.setImagePath(compressBitmap(bitmap).getAbsolutePath());
        } else {
            image.setImageBitmap(bitmap);
        }
        return image;
    }

    private File compressBitmap(final Bitmap bitmap) throws IOException {
        File mFolder = new File(DEFAULT_APP_DIRECTORY + "/.temp");
        if (!(mFolder.exists())) {
            mFolder.mkdirs();
        }
        String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
        File file = new File(filename);
        OutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return file;
    }

    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!background.equals("")) {
            SObjectImage sImageObject = null;
            try {
                sImageObject = getImageFromBitmap(BitmapFactory.decodeFile(background), false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCanvasView.insertSAMMImage(sImageObject, true);
            background = "";
            isSaved = false;
            mSaveBtn.setEnabled(true);
        } else if (!loadSAMMFile(location)) {
            java.io.FileInputStream in = null;
            try {
                in = new java.io.FileInputStream(location);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mCanvasView.setBackgroundImage(BitmapFactory.decodeStream(in));
            isSaved = false;
            mSaveBtn.setEnabled(true);
        }

        //LoadBackground();
    }*/

    boolean loadSAMMFile(String strFileName){
        if (mCanvasView != null && !mCanvasView.isAnimationMode()) {
            return mCanvasView.loadSAMMFile(strFileName, true, true, false);
        }
        return true;
    }

    public void LoadBackground() {
        Bitmap bitmap = generateBackgroundTile();
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        bd.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mCanvasView.setBackgroundDrawable(bd);
    }

}
