package com.gark.vknew.navigation;

public interface FragmentUnwindListener
{
    boolean isTargetFragment(ViewStackElement vse);

    public boolean popAllIfNotFound();
}