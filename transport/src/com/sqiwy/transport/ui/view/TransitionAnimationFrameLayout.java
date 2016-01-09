/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.sqiwy.transport.BuildConfig;

/**
 * A container that places a masking view on top of all other views.  The masking view can be
 * faded in and out.  Currently, the masking view is solid color white.
 */
public class TransitionAnimationFrameLayout extends FrameLayout {
	
	private static final String TAG = TransitionAnimationFrameLayout.class.getName();
    private static final long TRANSITION_TIME = 250;
	
	private View mMaskingView;
    private ObjectAnimator mAnimator;

    public TransitionAnimationFrameLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public TransitionAnimationFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TransitionAnimationFrameLayout(Context context) {
		super(context);
	}

	@Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mMaskingView = new View(getContext());
        mMaskingView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mMaskingView.setBackgroundColor(Color.BLACK);
        mMaskingView.setVisibility(View.INVISIBLE);
        addView(mMaskingView);
    }

    public void setMaskVisibility(boolean flag) {
    	
    	if (BuildConfig.DEBUG) {
    		Log.d(TAG, "setMaskVisibility " + flag + " alpha: " + mMaskingView.getAlpha());
    	}
    	
    	// Stop any animation that may still be running.
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
            mAnimator = null;
        }
    	
        if (flag) {
            mMaskingView.setVisibility(View.VISIBLE);
        } else {
            mMaskingView.setVisibility(View.INVISIBLE);
        }
        
        setMaskAlpha(1.0f);
    }

    /**
     * Starts the transition of showing or hiding the mask.
     * If showMask is true, the mask will be set to be invisible then fade into hide the other
     * views in this container.  If showMask is false, the mask will be set to be hide other views
     * initially.  Then, the other views in this container will be revealed.
     */
    public void startMaskTransition(boolean showMask) {
    	
    	if (BuildConfig.DEBUG) {
    		Log.d(TAG, "startMaskTransition " + showMask + " alpha: " + mMaskingView.getAlpha());
    	}
    	
        // Stop any animation that may still be running.
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
            mAnimator = null;
        }

        mMaskingView.setVisibility(View.VISIBLE);
        
        if (showMask) {
            mAnimator = ObjectAnimator.ofFloat(this, "maskAlpha", 0.1f, 0.0f);
            mAnimator.setDuration(TRANSITION_TIME);
            mAnimator.start();
        } else {
            // asked to hide the view
            mAnimator = ObjectAnimator.ofFloat(this, "maskAlpha", 1.0f, 0.0f);
            mAnimator.setDuration(TRANSITION_TIME);
            mAnimator.start();
        }
    }
    
    public void setMaskAlpha(float alpha) {
    	mMaskingView.setAlpha(alpha);
    }
}
