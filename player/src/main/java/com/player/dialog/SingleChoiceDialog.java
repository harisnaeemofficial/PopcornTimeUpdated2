package com.player.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.player.MobilePlayerActivity;
import com.player.R;
import com.player.subtitles.format.SRTFormat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SingleChoiceDialog extends DialogFragment {

    private class ChoiceAdapter extends BaseAdapter {

        private List<? extends ListItemEntity> items;
        private LayoutInflater inflater;

        public ChoiceAdapter(List<? extends ListItemEntity> items) {
            this.items = items;
            this.inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return items != null ? items.size() : 0;
        }

        @Override
        public ListItemEntity getItem(int position) {
            return items != null ? items.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.single_choice_item_layout, parent, false);
            }
            ((CheckedTextView) convertView).setText(getItem(position).getName());
            return convertView;
        }
    }

    private String title;
    private List<? extends ListItemEntity> items;
    private int checkedItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setSingleChoiceItems(new ChoiceAdapter(items), checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                items.get(which).onItemChosen();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        if (title.equals(getString(R.string.subtitle_file))) {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, ((MobilePlayerActivity) getActivity()).videoFile.getName(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String url = ((MobilePlayerActivity.VariantSubtitleItem) items.get(position)).getValue();
                            try {
                                URLConnection connection = new URL(url).openConnection();
                                connection.setConnectTimeout(1000);
                                connection.connect();
                                ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(connection.getInputStream()));
                                ZipEntry zipEntry;
                                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                                    int index = zipEntry.getName().lastIndexOf('.') + 1;
                                    String ext = zipEntry.getName().substring(index).toLowerCase();
                                    if (SRTFormat.EXTENSION.equals(ext)) {
                                        Log.d("POPCorn", "Subtitles file name: " + zipEntry.getName());
                                        final ZipEntry finalZipEntry = zipEntry;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((CheckedTextView) view).setText(finalZipEntry.getName());
                                            }
                                        });
                                        break;
                                    }
                                }
                                zipInputStream.closeEntry();
                                zipInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    return false;
                }
            });
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void show(FragmentManager fm, String title, List<? extends ListItemEntity> items, ListItemEntity checkedItem) {
        if (!isAdded() && !fm.isDestroyed()) {
            this.title = title;
            this.items = items;
            this.checkedItem = checkedItem != null ? checkedItem.getPosition() : -1;
            this.show(fm, "single_choice_dialog_" + hashCode());
        }
    }
}