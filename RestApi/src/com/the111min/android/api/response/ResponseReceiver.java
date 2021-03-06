package com.the111min.android.api.response;

import android.app.Activity;
import android.os.Bundle;

import com.the111min.android.api.request.Request;

/**
 * Listening {@link Activity} must implements {@link ResponseReceiver} for
 * getting result from {@link ResponseHandler}
 */
public interface ResponseReceiver {

    /**
     * Notifies about successfull {@link Request} execution
     */
    public void onRequestSuccess(int token, Bundle result);

    /**
     * Notifies about failed {@link Request} execution
     */
    public void onRequestFailure(int token, Bundle result);

    /**
     * @param token
     * @param e
     */
    public void onError(int token, Exception e);

}
