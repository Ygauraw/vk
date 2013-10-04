package com.gark.vknew.navigation;

/**
 * The owner of the navigation controller should be able to show the Fragment's title, switch to full screen and show
 * some custom navigation buttons.
 * 
 * 
 */
public interface NavigationControllerDelegate
{
    /**
     * If a new Fragment is displayed, the NavigationController will set the new title using this method.
     * 
     * @param title
     */
    public void setTitle(String title);

    public void setTitle(String title, int icon);

    /**
     * Allows to set the navigation button text.
     * 
     * @param text
     */
    public void updateNavigationBarText();

    /**
     * Allows to set the navigation button text and icon using resource IDs
     * 
     * @param text
     */
    public void setNavigationBarText(int text, int icon);

    /**
     * The delegate should hide navigation UI elements.
     * 
     * @param hideNavigation
     *            true, to hide navigation elements, false to show navigation elements
     * @param animated
     *            true, if screen changes should be animated
     */
    public void hideNavigation(boolean hideNavigation, boolean animated);

    /**
     * The delegate should hide all surrounding UI elements to allow a fullscreen view.
     * 
     * @param fullscreen
     *            true, to enable fullscreen
     * @param animated
     *            true, if screen changes should be animated
     */
    public void setFullscreen(boolean fullscreen, boolean animated);
}
