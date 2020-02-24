/*
* Copyright (C) 2019 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.android.systemui.omni;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.settingslib.Utils;
import com.android.systemui.R;

public class NotificationLightsView extends RelativeLayout {
    private static final boolean DEBUG = false;
    private static final String TAG = "NotificationLightsView";
    private ValueAnimator mLightAnimator;

    public NotificationLightsView(Context context) {
        this(context, null);
    }

    public NotificationLightsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationLightsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationLightsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (DEBUG) Log.d(TAG, "new");
    }

    public void stopAnimateNotification() {
        if (mLightAnimator != null) {
            mLightAnimator.end();
            mLightAnimator = null;
        }
    }

    public void animateNotification() {
        animateNotificationWithColor(getNotificationLightsColor());
    }

    public int getNotificationLightsColor() {
        int color = getDefaultNotificationLightsColor();
        boolean useAccent = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_ACCENT,
                0, UserHandle.USER_CURRENT) != 0;
        if (useAccent) {
            color = Utils.getColorAccentDefaultColor(getContext());
        }
        return color;
    }

    public int getDefaultNotificationLightsColor() {
        int defaultColor = getResources().getInteger(
                com.android.internal.R.integer.config_ambientNotificationDefaultColor);
        return Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR, defaultColor,
                    UserHandle.USER_CURRENT);
    }

    public void animateNotificationWithColor(int color) {
        int duration = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.AMBIENT_LIGHT_DURATION, 2,
                UserHandle.USER_CURRENT) * 1000;
        int repeat = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, 0,
                UserHandle.USER_CURRENT);
        boolean directionIsRestart = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.AMBIENT_LIGHT_REPEAT_DIRECTION, 0,
                UserHandle.USER_CURRENT) != 1;
        int style = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.AMBIENT_LIGHT_LAYOUT, 0,
                UserHandle.USER_CURRENT);

        ImageView leftViewFaded = (ImageView) findViewById(R.id.notification_animation_left_faded);
        ImageView rightViewFaded = (ImageView) findViewById(R.id.notification_animation_right_faded);
        ImageView leftViewSolid = (ImageView) findViewById(R.id.notification_animation_left_solid);
        ImageView rightViewSolid = (ImageView) findViewById(R.id.notification_animation_right_solid);
        leftViewFaded.setColorFilter(color);
        rightViewFaded.setColorFilter(color);
        leftViewFaded.setVisibility(style == 0 ? View.VISIBLE : View.GONE);
        rightViewFaded.setVisibility(style == 0 ? View.VISIBLE : View.GONE);
        leftViewSolid.setColorFilter(color);
        rightViewSolid.setColorFilter(color);
        leftViewSolid.setVisibility(style == 1 ? View.VISIBLE : View.GONE);
        rightViewSolid.setVisibility(style == 1 ? View.VISIBLE : View.GONE);
        mLightAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 2.0f});
        mLightAnimator.setDuration(duration);
        mLightAnimator.setRepeatCount(repeat == 0 ? ValueAnimator.INFINITE : repeat);
        mLightAnimator.setRepeatMode(directionIsRestart ? ValueAnimator.RESTART : ValueAnimator.REVERSE);
        mLightAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (DEBUG) Log.d(TAG, "onAnimationUpdate");
                float progress = ((Float) animation.getAnimatedValue()).floatValue();
                leftViewFaded.setScaleY(progress);
                rightViewFaded.setScaleY(progress);
                leftViewSolid.setScaleY(progress);
                rightViewSolid.setScaleY(progress);
                float alpha = 1.0f;
                if (progress <= 0.3f) {
                    alpha = progress / 0.3f;
                } else if (progress >= 1.0f) {
                    alpha = 2.0f - progress;
                }
                leftViewFaded.setAlpha(alpha);
                rightViewFaded.setAlpha(alpha);
                leftViewSolid.setAlpha(alpha);
                rightViewSolid.setAlpha(alpha);
            }
        });
        if (DEBUG) Log.d(TAG, "start");
        mLightAnimator.start();
    }
}
