package com.softard.wow.screencapture.QRCode;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.softard.wow.screencapture.R;

/**
 * Created by wow on 5/8/18.
 */

public class ResultDialog extends Dialog {

    public ResultDialog(@NonNull Context context) {
        super(context);

    }

    public ResultDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ResultDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder {

        private Context context;
        private Bitmap bitmap;
        private String result;

        public Builder() {

        }

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setBitmap(Bitmap bmp) {
            this.bitmap = bmp;
            return this;
        }

        public Builder setResult(String str) {
            this.result = str;
            return this;
        }

        public ResultDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.dlg_scan_result, null);

            ImageView iv = layout.findViewById(R.id.dlg_result_imageView);
            iv.setImageBitmap(this.bitmap);

            TextView tv = layout.findViewById(R.id.dlg_result_content);
            tv.setText(this.result);

            final ResultDialog dialog = new ResultDialog(context);
//            dialog.addContentView(layout, new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            return dialog;


        }
    }


}
