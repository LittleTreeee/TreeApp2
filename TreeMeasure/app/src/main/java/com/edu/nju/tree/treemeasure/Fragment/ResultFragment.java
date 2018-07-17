package com.edu.nju.tree.treemeasure.Fragment;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.graphics.*;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.edu.nju.tree.treemeasure.R;
import com.edu.nju.tree.treemeasure.utils.ImageProcess;
import org.opencv.android.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResultFragment extends Fragment {
    private ImageView imageView;
    private Button btnSave,btnCancel;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, null);
        imageView = view.findViewById(R.id.result_imageview);
        btnSave = view.findViewById(R.id.result_save);
        btnCancel = view.findViewById(R.id.result_cancel);

//        imageView.setImageBitmap(finalBitmap);

        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                //todo 保存
//                saveImage(photo);
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //todo 给图片打上标签后保存

                //todo 返回到拍照界面
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance(), null)
                        .commit();
//                getActivity().getSupportFragmentManager().popBackStack();
                Toast.makeText(getContext(), "取消", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    private void saveImage(Bitmap bitmap){
        FileOutputStream output = null;

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US);

        String fname = "IMG_" +
                sdf.format(new Date())
                + ".jpg";
        File mFile = new File(getActivity().getApplication().getExternalFilesDir(null), fname);


        Log.d("ImagePath", mFile.toString());

        try {
            output = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (null != output) {
                try {
                    output.close();
                    Log.d("outputSuccess", mFile.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
