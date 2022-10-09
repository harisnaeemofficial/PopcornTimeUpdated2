package com.movieflix.mobile.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.movieflix.mvp.IViewRouter;

import com.movieflix.mobile.PopcornApplication;
import com.movieflix.mobile.R;
import com.movieflix.model.share.IShareData;
import com.movieflix.ui.ISystemShareView;
import com.movieflix.ui.share.ISharePresenter;
import com.movieflix.ui.share.IShareView;
import com.movieflix.ui.share.SharePresenter;

public final class ShareDialog extends AppCompatDialogFragment implements IShareView, DialogInterface.OnShowListener {

    private TextView tvTitle;
    private TextView tvText1;
    private TextView tvText2;
    private TextView tvText3;
    private TextView tvText4;
    private Button btnShare;

    private IShareData shareData;

    private ISharePresenter sharePresenter;

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.SecondLaunchDialogAnimation;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        final PopcornApplication app = (PopcornApplication) getActivity().getApplication();
        sharePresenter = new SharePresenter(app.getShareUseCase());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.SecondLaunchDialog);
        builder.setView(R.layout.dialog_on_second_launch);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(ShareDialog.this);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        sharePresenter.attach(ShareDialog.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        sharePresenter.detach(ShareDialog.this);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        sharePresenter.cancel();
    }

    @Override
    public void onShowShareData(@NonNull IShareData shareData) {
        this.shareData = shareData;
        onShowShareData();
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        AlertDialog dialog = (AlertDialog) dialogInterface;
        tvTitle = (TextView) dialog.findViewById(R.id.textView0);
        tvText1 = (TextView) dialog.findViewById(R.id.textView1);
        tvText2 = (TextView) dialog.findViewById(R.id.textView2);
        tvText3 = (TextView) dialog.findViewById(R.id.textView3);
        tvText4 = (TextView) dialog.findViewById(R.id.textView4);
        btnShare = (Button) dialog.findViewById(R.id.btn1);
        btnShare.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (shareData != null) {
                    ((IViewRouter) getActivity().getApplication()).onShowView(ISystemShareView.class, shareData.getText());
                    sharePresenter.share();
                }
                dismiss();
            }
        });
        onShowShareData();
    }

    private void onShowShareData() {
        if (tvTitle != null && shareData != null) {
            tvTitle.setText(shareData.getDialogTitle());
            tvText1.setText(shareData.getDialogText1());
            tvText2.setText(shareData.getDialogText2());
            tvText3.setText(shareData.getDialogText3());
            tvText4.setText(shareData.getDialogText4());
            btnShare.setText(shareData.getDialogButton());
        }
    }

    public static void open(@NonNull FragmentManager manager, @NonNull String tag) {
        ShareDialog fragment = (ShareDialog) manager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new ShareDialog();
        }
        if (fragment.isAdded()) {
            return;
        }
        fragment.show(manager, tag);
    }
}
