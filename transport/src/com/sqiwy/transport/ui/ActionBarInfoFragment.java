/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sqiwy.transport.R;

/**
 * Fragment to display image & text on action bar.
 */
public class ActionBarInfoFragment extends Fragment {

	public static final String ARG_INFO_ICON_RESOURCE = "arg-info-icon-res";
	public static final String ARG_INFO_TEXT = "arg-info-text";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_action_bar_info, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		int icon = getArguments().getInt(ARG_INFO_ICON_RESOURCE);
		String text = getArguments().getString(ARG_INFO_TEXT);
		
		((ImageView) view.findViewById(R.id.action_bar_info_icon)).setImageResource(icon);
		((TextView) view.findViewById(R.id.action_bar_info_text)).setText(text);
	}
	
}
