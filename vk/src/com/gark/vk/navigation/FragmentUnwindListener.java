package com.gark.vk.navigation;

public interface FragmentUnwindListener
{
    boolean isTargetFragment(ViewStackElement vse);

    public boolean popAllIfNotFound();
}