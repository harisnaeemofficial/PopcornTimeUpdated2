package com.movieflix.mobile.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

import com.movieflix.mobile.R;

public class CollapsableCardView extends CardView {
    private AppCompatButton collapser;
    private View toCollapse;

    public CollapsableCardView(@NonNull Context context) {
        this(context, null);
    }

    public CollapsableCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.support.v7.cardview.R.attr.cardViewStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        collapser = getChildAt(0).findViewWithTag("collapser");
        toCollapse = getChildAt(0).findViewWithTag("toCollapse");

        collapser.setOnClickListener(v -> {
            switchState();
        });
    }

    public CollapsableCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void collapse() {
        toCollapse.setVisibility(View.GONE);
        collapser.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_arrow_down), null, null, null);
    }

    public void expand() {
        toCollapse.setVisibility(View.VISIBLE);
        collapser.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_arrow_up), null, null, null);
    }

    public void switchState() {
        if (toCollapse.getVisibility() == View.GONE) {
            expand();
        } else {
            collapse();
        }
    }
}
