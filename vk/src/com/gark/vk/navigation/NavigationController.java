package com.gark.vk.navigation;

import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.gark.vk.R;
import com.gark.vk.utils.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Stack;

/**
 * Handles FragmentTransactions in HomeActivity. The viewStack is needed to keep
 * track of the transition of each Fragment.
 *
 * @author "Artem  Garkusha"
 */
public final class NavigationController implements Serializable {
    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = -6343553667351612364L;
    /**
     * Keeps track of fragment stack
     */
    private final Stack<ViewStackElement> viewStack = new Stack<ViewStackElement>();

    // private NavigationControllerDelegate delegate = null;
    // private final int contentArea = 0;

    public enum Transition {
        /**
         * Fragment slides in from bottom.
         */
        VERTICAL,
        /**
         * Fragment slides in from bottom and hides the navigation bar.
         */
        VERTICAL_NO_NAVIBAR,
        /**
         * Fragment slides in from bottom, hides navigation bar and volume /
         * search buttons.
         */
        VERTICAL_FULLSCREEN,
        /**
         * Fragment slides in from right.
         */
        HORIZONTAL,
        /**
         * Fragments are replaced without any effect.
         */
        NO_EFFECT,
        /**
         * Fade In
         */
        FADE,
        /**
         * Launch like an application
         */
        LAUNCH,
        /**
         * Dummy value to initialize the root object.
         */
        ROOT
    }

    ;

    public enum Backstack {
        /**
         * Add fragment to backstack
         */
        ADD,
        /**
         * Don't add fragment to backstack
         */
        DO_NOT_ADD,
        /**
         * Add fragment to backstack, do not allow another fragment of the same
         * type on the viewstack
         */
        EXCLUSIVE,
        /**
         * Replace the current fragment with this fragment
         */
        REPLACE,
        /**
         * Replace the whole backstack with this fragment
         */
        REPLACE_ALL
    }

    /**
     * Default constructor.
     *
     * @param ctx   reference to the main activity
     * @param resId fragment resource of the main activity
     * @param del   delegate (main activity should implement
     *              NavigationControllerDelegate interface)
     */
    public NavigationController(final FragmentActivity context, final NavigationControllerDelegate del) {
        super();

        assert (null != context);
        // assert (null != del);
        // delegate = del;
        // contentArea = resId;
        //
        // context.getSupportFragmentManager().addOnBackStackChangedListener(new
        // FragmentManager.OnBackStackChangedListener() {
        //
        // @Override
        // public void onBackStackChanged() {
        // if (!viewStack.isEmpty()) {
        // Toast.makeText(context, "" + viewStack.size() + " " ,
        // Toast.LENGTH_SHORT).show();
        // // for (ViewStackElement item : viewStack) {
        // // Toast.makeText(context, "" + viewStack.size() + " " +
        // item.fragmentClass.getSimpleName(), Toast.LENGTH_SHORT).show();
        // // }
        // }
        // }
        // });
    }

    // public void setDelegate(NavigationControllerDelegate d) {
    // delegate = d;
    // }
    //
    // public NavigationControllerDelegate getDelegate() {
    // return delegate;
    // }

    /**
     * Pushes view on the view stack using the specified transition.
     *
     * @param pushMe     ViewController to push
     * @param transition navigation option
     */
    public void pushView(FragmentActivity context, final int contentArea, NavigationControllerFragment pushMe, Transition transition) {
        pushView(context, contentArea, pushMe, transition, Backstack.ADD, null);
    }

    /**
     * Pushes view on the view stack using the specified transition
     *
     * @param pushMe     ViewController to push
     * @param transition navigation option, @see Transition
     * @param tags       Key/Value HashMap, allows to attach arbitrary values to the
     *                   ViewStackElement
     */
    public void pushView(FragmentActivity context, final int contentArea, NavigationControllerFragment pushMe, Transition transition, HashMap<String, String> tags) {
        pushView(context, contentArea, pushMe, transition, Backstack.ADD, tags);
    }

    /**
     * Pushes view on the view stack, using the specified transition
     *
     * @param pushMe     ViewController to push
     * @param transition navigation option, @see Transition
     * @param backstack  backstack option, @see Backstack
     */
    public void pushView(FragmentActivity context, final int contentArea, Fragment pushMe, Transition transition, Backstack backstack) {
        pushView(context, contentArea, pushMe, transition, backstack, null);
    }

    /**
     * Pushes view on the view stack, using the specified transition.
     *
     * @param pushMe     ViewController to push
     * @param transition transition option, @see Transition
     * @param backstack  backstack option, @see Backstack
     * @param tags       Key/Value HashMap, allows to attach arbitrary values to the
     *                   ViewStackElement
     */
    public void pushView(FragmentActivity context, final int contentArea, Fragment pushMe, Transition transition, Backstack backstack, HashMap<String, String> tags) {
        assert Looper.getMainLooper().getThread() == Thread.currentThread() : "You must call this method from the UI thread!";

        Log.i(">>> push: " + pushMe.getClass().getSimpleName() + ", " + transition + ", " + backstack);

        // Sometimes, context is null. According to the automatic reports used
        // in version 1.7, the most likely cause
        // is the setup wizard. In the setup process, we must make sure that we
        // do not call push with a null context.
        if (null == context) {
            Log.e(new NullPointerException("Cannot pushView(), context is null!"));
            return;
        }

        // hide soft keyboard (fixed ticket Ticket #2346)
        // Window w = context.getWindow();
        // if (null != w) {
        // View v = w.getDecorView();
        // if (null != v)
        // HomeActivity.hideSoftKeyboard(context, v.getWindowToken());
        // }

        FragmentTransaction ft = context.getSupportFragmentManager().beginTransaction();

        switch (transition) {
            case HORIZONTAL:
                ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out);
                ft.replace(contentArea, pushMe, pushMe.getClass().getSimpleName());
                break;
            case FADE:
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                ft.replace(contentArea, pushMe, pushMe.getClass().getSimpleName());
                break;
            case NO_EFFECT:
                ft.replace(contentArea, pushMe, pushMe.getClass().getSimpleName());
                break;
            case VERTICAL:
                ft.setCustomAnimations(R.anim.slide_bottom_in, R.anim.fade_down, 0, R.anim.slide_bottom_out);
                ft.replace(contentArea, pushMe, pushMe.getClass().getSimpleName());
                break;
            case VERTICAL_NO_NAVIBAR:
                // if (null != delegate)
                // delegate.hideNavigation(true, true);

                ft.setCustomAnimations(R.anim.slide_bottom_in, R.anim.fade_down, 0, R.anim.slide_bottom_out);
                ft.replace(contentArea, pushMe, pushMe.getClass().getSimpleName());
                break;
            case VERTICAL_FULLSCREEN:
                // if (null != delegate)
                // delegate.setFullscreen(true, true);

                ft.setCustomAnimations(R.anim.slide_bottom_in, R.anim.fade_down, 0, R.anim.slide_bottom_out);
                ft.replace(contentArea, pushMe, pushMe.getClass().getSimpleName());
                break;
            case LAUNCH:
                ft.setCustomAnimations(R.anim.zoom_in, R.anim.fade_down, 0, R.anim.zoom_out);
                ft.replace(contentArea, pushMe, pushMe.getClass().getSimpleName());
                break;
            default:
                assert false : "unsupported Transition!";
        }

        switch (backstack) {
            case ADD:
                ft.addToBackStack(pushMe.getClass().getSimpleName());
                break;
            case DO_NOT_ADD:
                // if (0 < viewStack.size())
                // viewStack.pop();
                ft.commitAllowingStateLoss();
                return;
            // break;
            case EXCLUSIVE:
                // do not allow the same fragment type twice on the backstack
                if (viewStackContains(backstack))
                    unwindIncluding(context, backstack);
                ft.addToBackStack(pushMe.getClass().getSimpleName());
                break;
            case REPLACE:
                if (0 < viewStack.size())
                    pop(context, false);
                ft.addToBackStack(pushMe.getClass().getSimpleName());
                break;
            case REPLACE_ALL:
                unwindAllFragments(context);
                ft.addToBackStack(pushMe.getClass().getSimpleName());
                break;
            default:
                assert false : "unsupported Backstack option " + backstack;
        }

        // perform animation.
        int commitID = ft.commitAllowingStateLoss();

        // maintain view stack and home button status
        ViewStackElement stackElement = new ViewStackElement(pushMe, transition, backstack, commitID);
        if (null != tags)
            stackElement.tags = tags;

        viewStack.push(stackElement);
        // ((HomeActivity) context).enableHomeButton(true);
    }

    /**
     * Removes the topmost view from the stack, performs one step back in
     * navigation.
     */
    public Transition pop(FragmentActivity context) {
        return pop(context, true);
    }

    /**
     * Removes the topmost view from the stack, performs one step back in
     * navigation.
     */
    private Transition pop(FragmentActivity context, boolean maintainFullScreenState) {
        assert Looper.getMainLooper().getThread() == Thread.currentThread() : "You must call this method from the UI thread!";

        // java.util.EmptyStackException? Push and pop is not balanced!
        ViewStackElement e = viewStack.pop();
        Transition transition = e.transition;
        Log.i("<<< pop: " + e.fragmentClass.getSimpleName() + ", " + e.transition + ", " + e.option);

        switch (transition) {
            case HORIZONTAL:
            case VERTICAL:
            case FADE:
            case LAUNCH:
            case NO_EFFECT:
                context.getSupportFragmentManager().popBackStack();
                break;

            case VERTICAL_FULLSCREEN:
            case VERTICAL_NO_NAVIBAR:
                context.getSupportFragmentManager().popBackStack();
                if (maintainFullScreenState)
                    // maintainFullScreenState(true);
                    break;

            default:
                assert false : "unsupported Transition style " + transition;
        }
        return transition;
    }

    public void popOnBackPressed() {
        assert Looper.getMainLooper().getThread() == Thread.currentThread() : "You must call this method from the UI thread!";
        if (!viewStack.isEmpty()) {
            viewStack.pop();
        }
    }

    /**
     * @param animated true, if screen changes should be animated
     */
    // public void maintainFullScreenState(boolean animated) {
    // if (null != delegate) {
    // boolean navibarShown = true;
    // boolean fullScreen = false;
    //
    // for (ViewStackElement vse : viewStack) {
    // if (vse.transition == Transition.VERTICAL_NO_NAVIBAR) {
    // navibarShown = false;
    // }
    //
    // if (vse.transition == Transition.VERTICAL_FULLSCREEN) {
    // fullScreen = true;
    // }
    // }
    //
    // if (0 < viewStack.size() && !navibarShown)
    // delegate.hideNavigation(true, animated);
    // else if (0 < viewStack.size() && fullScreen)
    // delegate.setFullscreen(true, animated);
    // else {
    // // either the stack is empty or the current fragment is a
    // // fullscreen / nonavibar fragment too
    // delegate.hideNavigation(false, animated);
    // delegate.setFullscreen(false, animated);
    // }
    // }
    // }
    public void unwindAllFragments(FragmentActivity context) {
        assert Looper.getMainLooper().getThread() == Thread.currentThread() : "You must call this method from the UI thread!";

        Log.i("<<< unwindAllFragments (" + viewStack.size() + " elements)");
        context.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        viewStack.removeAllElements();
    }

    public boolean isViewStackEmpty() {
        return viewStack.isEmpty();
    }

    public int getViewStackCount() {
        return viewStack.size();
    }

    // public boolean isSetupOrSettingsVisible() {
    // if (viewStack.isEmpty())
    // return false;
    //
    // ViewStackElement el = viewStack.peek();
    // return (el.fragmentClass == SettingsFragment.class || el.fragmentClass ==
    // SetupWizardFragment.class || (el.fragmentClass ==
    // WizardPageFragment.class));
    // }
    //
    // public boolean isFullScreenFragmentVisible() {
    // if (viewStack.isEmpty())
    // return false;
    //
    // ViewStackElement el = viewStack.peek();
    // return (Transition.VERTICAL_FULLSCREEN == el.transition ||
    // Transition.VERTICAL_NO_NAVIBAR == el.transition);
    // }

    /**
     * Unwind until a fragment with the given BACKSTACK option is found.
     *
     * @param f type to search for
     */
    private synchronized void unwindIncluding(FragmentActivity context, Backstack option) {
        while (!viewStack.isEmpty()) {
            ViewStackElement e = viewStack.pop();

            if (e.option == option) {
                context.getSupportFragmentManager().popBackStack(e.commitID, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                // maintainFullScreenState(true);
                return;
            }
        }

        Log.e("did not find the requested fragment with option " + option + " to unwind.");
    }

    /**
     * Unwind until a fragment is found for which the listeners callback returns
     * true
     *
     * @param listener object to be asked for all found fragments
     */
    public synchronized void unwindExcluding(FragmentActivity context, FragmentUnwindListener listener) {
        assert Looper.getMainLooper().getThread() == Thread.currentThread() : "You must call this method from the UI thread!";

        ViewStackElement last = null;

        while (!viewStack.isEmpty()) {
            ViewStackElement e = viewStack.peek();

            if (listener.isTargetFragment(e)) {
                if (last != null) {
                    context.getSupportFragmentManager().popBackStack(last.commitID, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    // maintainFullScreenState(true);
                }
                return;
            }

            viewStack.pop();
            last = e;
        }

        if (listener.popAllIfNotFound() && last != null) {
            context.getSupportFragmentManager().popBackStack(last.commitID, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (last.transition == Transition.VERTICAL_NO_NAVIBAR || last.transition == Transition.VERTICAL_FULLSCREEN) {
                // maintainFullScreenState(true);
            }
        } else {
            Log.e("did not find the requested fragment to unwind", new Throwable());
        }
    }

    /**
     * @param type type to search for
     * @return true, if the viewStack contains a view of the given BACKSTACK
     *         type <br/>
     *         false else
     */
    private boolean viewStackContains(Backstack type) {
        for (ViewStackElement element : viewStack)
            if (element.option == type)
                return true;

        return false;
    }

    /**
     * @param nc Class to search for
     * @return true, if the viewStack contains a NavigationControllerFragment of
     *         the given Class <br/>
     *         false else
     */
    // public boolean viewStackContains(Class<? extends
    // NavigationControllerFragment> nc) {
    public boolean viewStackContains(Class<? extends Fragment> nc) {
        for (ViewStackElement element : viewStack)
            if (element.fragmentClass.equals(nc))
                return true;

        return false;
    }

    public synchronized void popVertical(FragmentActivity context) {
        assert Looper.getMainLooper().getThread() == Thread.currentThread() : "You must call this method from the UI thread!";

        while (!viewStack.isEmpty()) {
            ViewStackElement e = viewStack.pop();

            if (e.transition == Transition.VERTICAL || e.transition == Transition.VERTICAL_NO_NAVIBAR || e.transition == Transition.VERTICAL_FULLSCREEN) {
                context.getSupportFragmentManager().popBackStack(e.commitID, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                // maintainFullScreenState(true);
                return;
            }
        }

        Log.e("did not find a vertically pushed fragment");
    }
}
