/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sqiwy.transport.R;

/**
 * Fragment to display date & time.
 */
public class ActionBarDateFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_action_bar_date, container, false);
	}
	
}
