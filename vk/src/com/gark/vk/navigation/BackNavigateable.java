package com.gark.vk.navigation;

public interface BackNavigateable
{
    /**
     * 
     * @return true, if you handled the back for yourself, false if you want the parent to handle the back
     */
    public boolean goBack();
}
