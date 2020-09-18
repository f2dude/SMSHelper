package com.sp.smshelper.main;

import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

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
     * @param appCompatActivity Activity
     * @param actionMenu Menu resource
     * @param title Title
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
                mActionMode = null;
            }
        });
    }

    protected boolean onActionItemClick(int itemId){
        return false;
    }
}
