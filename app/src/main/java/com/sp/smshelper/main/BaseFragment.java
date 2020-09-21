package com.sp.smshelper.main;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected ActionMode mActionMode;

    protected void addToCompositeDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != compositeDisposable) {
            compositeDisposable.clear();
        }
    }

    /**
     * Starts the action mode
     *
     * @param appCompatActivity Activity
     * @param actionMenu        Menu resource
     * @param title             Title
     */
    protected void startActionMode(AppCompatActivity appCompatActivity, int actionMenu, String title) {
        if (null != mActionMode) {
            return;
        }
        mActionMode = appCompatActivity.startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(actionMenu, menu);
                mode.setTitle(title);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                onActionItemClick(item.getItemId());
                mode.finish();
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //Context action mode back button & when action mode is destroyed using hard back button press
                Observable<Long> observable = Observable.timer(300, TimeUnit.MILLISECONDS, Schedulers.io());
                Disposable disposable = observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> onActionItemClick(-1));
                addToCompositeDisposable(disposable);
                mActionMode = null;
            }
        });
    }

    protected boolean onActionItemClick(int itemId) {
        Log.d(TAG, "Item id: " + itemId);
        return false;
    }
}
