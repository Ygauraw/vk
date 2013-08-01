package com.gark.vk.navigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class NavigationControllerFragment extends Fragment {
	protected NavigationController navigationController;
//	protected int title;

//	public NavigationControllerFragment(int title) {
//		this.title = title;
//	}

	// ///////////////////////////////////////////////////////////////////////
	// Fragment life cycle
	// ///////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// if the instance is restored, load navigation controller from bundle
		// if (null != savedInstanceState)
		// {
		// navigationController = (NavigationController)
		// savedInstanceState.getSerializable("navigationController");
		// navigationController.setDelegate((HomeActivity) getActivity());
		// }
		// else
		// {
		// navigationController = ((HomeActivity)
		// getActivity()).getNavigationController();
		// }
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		((MainTabsFragmentActivity) getActivity()).setTitle(title);
//		((Test) getActivity()).setTitle(title);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// navigationController.setDelegate(null); // HomeActivity is not
		// serializable.
		// outState.putSerializable("navigationController",
		// navigationController);
	}

}
