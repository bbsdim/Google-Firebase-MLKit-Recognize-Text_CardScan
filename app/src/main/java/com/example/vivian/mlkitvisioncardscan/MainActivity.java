package com.example.vivian.mlkitvisioncardscan;
/**
 *前置作業：連結Google Firebase ML Kit步驟
 * (一)  建立專案：至Firebase 控制台 (https://console.firebase.google.com/)並登入Google帳戶 ->請在歡迎畫面點擊「新增專案」並為專案命名。
 * (二)  新增專案內的應用程式：開啟如上新增專案及點擊「新增應用程式」，並選取適用平台 (本專案選擇Android)。
 *(三)  連結Firebase到Android應用程式：
        1. 註冊應用程式：依序填入Android套件名稱、應用程式暱稱(選填)、偵錯簽署憑證SHA-1(選填)。
        2.下載設定檔：點擊「下載google-services.json」, 並依說明將檔案加入應用程式的模組根目錄中。
        3.新增Firebase SDK
        4.	新增Text Recognition SDK
        PS：ML Kit 為本機端(On-device)及雲端(Cloud)提供了通用的使用介面。本機端的 API 能夠快速的處理數據；
                而雲端的 API 則使用 Google Cloud Platform 機器學習技術，提供更細緻、更高準確度的資料（Cloud API Pricing Plans請參考https://firebase.google.com/pricing/）。
        5.	執行應用程式以驗證是否連結成功：成功將Firebase新增至應用程式後，接下來即可開始編寫所需功能的程式碼。
 *  */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudTextDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CameraKitView cameraKitView;
    private Bitmap mBitmap;
    private Button btn_onDevice, btn_cloud;
    private TextView tv_result1, tv_result2;
    private FrameLayout framePreview;
    private ImageView imagePreview;
    private ImageButton btnRetry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraKitView = findViewById(R.id.camera);
        tv_result1 = findViewById(R.id.tv_result1);
        tv_result2 = findViewById(R.id.tv_result2);
        btn_onDevice = findViewById(R.id.btn_onDevice);
        btn_cloud = findViewById(R.id.btn_cloud);
        framePreview = findViewById(R.id.framePreview);
        imagePreview = findViewById(R.id.imagePreview);
        btnRetry = findViewById(R.id.btnRetry);

        //initialize FirebaseApp
        FirebaseApp.initializeApp(this);

        showCloud();//顯示信用卡號及到期日
        showOnDevice();//顯示完整信用卡掃描資訊
    }


    private void showOnDevice() {
        btn_onDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                        // capturedImage contains the image from the CameraKitView
                        Bitmap bitmap = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length);
                        mBitmap = bitmap;
                        //1.To create a FirebaseVisionImage object from a Bitmap object:
                        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                        //for on-device
                        //FirebaseVisionDocumentText
                        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
                        Task<FirebaseVisionText> deviceResult = detector.detectInImage(image)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();

                                        List<FirebaseVisionText.Block> blocks = firebaseVisionText.getBlocks();
                                        StringBuilder onDeviceText = new StringBuilder("");
                                        for (int i = 0; i < blocks.size(); i++) {
                                            onDeviceText.append(blocks.get(i).getText() + "\n");
                                        }
                                        String str = onDeviceText.toString();
                                        //if on-device 顯示完整信用卡掃描資訊
                                        tv_result2.setText(str);
                                        tv_result1.setText("信用卡資訊：");

                                        //if on-device 顯示信用卡號及到期日
//                                        String[] words = str.split("\n");
//                                        for (String num : words) {
//                                            //信用卡卡號REGEX
//                                            if (num.replace(" ", "").matches("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})$")) {
//                                                String cardnumber = num;
//                                                tv_result1.setText("CardNumber：" + cardnumber.replace(" ", "－"));
//                                            }
//                                            //信用卡到期日
//                                            if (num.contains("/")) {
//                                                String[] words1 = num.split(" ");
//                                                for (String data : words1) {
//                                                    if (data.contains("/")) {
//                                                        String exp = data;
//                                                        tv_result2.setText("CardExpired：" + exp);
//                                                    }
//                                                }
//                                            }
//                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                                        tv_result2.setText(e.getMessage());
                                        tv_result1.setText("信用卡資訊：");
                                    }
                                });
                        runOnUiThread();//image preview
                    }
                });
            }
        });
    }

    private void showCloud() {
        btn_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(final CameraKitView cameraKitView, final byte[] capturedImage) {

                        // capturedImage contains the image from the CameraKitView
                        Bitmap bitmap = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length);
                        mBitmap = bitmap;
                        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                        //for cloud
                        FirebaseVisionCloudDetectorOptions options =
                                new FirebaseVisionCloudDetectorOptions.Builder()
                                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                                        .setMaxResults(10)//get檢測最大結果數
                                        .build();

                        FirebaseVisionCloudTextDetector detector = FirebaseVision.getInstance().getVisionCloudTextDetector(options);

                        //Start image detection and wait for results
                        Task<FirebaseVisionCloudText> result = detector.detectInImage(image)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionCloudText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionCloudText firebaseVisionCloudText) {
//                                        tv_result2.setText(firebaseVisionCloudText.getText());
                                        //辨識成功
                                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();

                                        String str = firebaseVisionCloudText.getText();
                                        String[] words = str.split("\n");
                                        for (String num : words) {
                                            //信用卡卡號 REGEX
                                            if (num.replace(" ", "").matches(
                                                    "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})$")) {
                                                String cardnumber = num;
                                                tv_result1.setText("CardNumber：" + cardnumber.replace(" ", "－"));
                                            }
                                            //信用卡到期日
                                            if (num.contains("/")) {
                                                String[] words1 = num.split(" ");
                                                for (String data : words1) {
                                                    if (data.contains("/")) {
                                                        String exp = data;
                                                        tv_result2.setText("CardExpired：" + exp);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //辨識失敗
                                        Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                                        tv_result2.setText(e.getMessage());
                                    }
                                });
                        runOnUiThread();//image preview
                    }
                });
            }
        });
    }

    //image preview
    private void runOnUiThread() {
        showPreview();
        imagePreview.setImageBitmap(mBitmap);
        dealRetry();//重新拍照
    }

    private void showPreview() {
        framePreview.setVisibility(View.VISIBLE);
        cameraKitView.setVisibility(View.GONE);
    }

    private void hidePreview() {
        framePreview.setVisibility(View.GONE);
        cameraKitView.setVisibility(View.VISIBLE);
    }

    private void dealRetry() {
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraKitView.getVisibility() == View.GONE) {
                    hidePreview();
                } else {
                    showPreview();
                }
            }
        });
    }

    //for CameraKit
    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraKitView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraKitView.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


