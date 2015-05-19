package com.pasedelista.app.pasedelista.Loader;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import java.util.ArrayList;

/**
 * This class handles execution of batch mOperations on a provider.
 */
public class BatchOperation {

    private final ContentResolver mResolver;
    ArrayList<ContentProviderOperation> mOperations;
    private final String mAuthority;

    public BatchOperation(String authority, ContentResolver resolver) {
        mResolver = resolver;
        mOperations = new ArrayList<ContentProviderOperation>();
        mAuthority = authority;
    }

    public int size() {
        return mOperations.size();
    }

    public void add(ContentProviderOperation cpo) {
        mOperations.add(cpo);
    }

    public void execute() {
        if (mOperations.size() == 0) {
            return;
        }
        // Apply the mOperations to the content provider
        try {
            mResolver.applyBatch(mAuthority, mOperations);
        } catch (final OperationApplicationException e1) {
            /* ignore, we just can't do anything about it. */
        } catch (final RemoteException e2) {
            /* ignore, we just can't do anything about it. */
        }
        mOperations.clear();
    }

}