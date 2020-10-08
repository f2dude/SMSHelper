package com.sp.smshelper.readmms;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ActivityReadMmsBinding;

public class ReadMmsActivity extends AppCompatActivity {

    private ActivityReadMmsBinding mBinding;
    private MmsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_read_mms);

        mViewModel = new ViewModelProvider(this).get(MmsViewModel.class);
        mViewModel.setContext(getBaseContext());

        startReadMmsFragment();
    }

    private void startReadMmsFragment() {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                        ReadMmsFragment.newInstance(),
                        ReadMmsFragment.TAG)
                .commit();
    }

    public MmsViewModel getViewModel() {
        return mViewModel;
    }
}
