package com.gark.vk.navigation;

import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.util.HashMap;



/**
 * Represents one item on NavigationsController's viewStack.
 */
public final class ViewStackElement implements Serializable {
	/** Serialization ID. */
	private static final long serialVersionUID = 4249637221565805328L;
	public NavigationController.Transition transition;
	public NavigationController.Backstack option;
	public Class<? extends Fragment> fragmentClass;
	public int commitID;
	/**
	 * Allows to use arbitrary strings as tags. This is used in
	 * SettingsFragment.
	 */
	public HashMap<String, String> tags = null;

	public ViewStackElement(Fragment f, NavigationController.Transition d, NavigationController.Backstack o, int commitID) {
		transition = d;
		fragmentClass = f.getClass();
		option = o;
		this.commitID = commitID;
	}
}