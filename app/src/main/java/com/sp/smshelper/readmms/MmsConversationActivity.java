package com.sp.smshelper.readmms;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ActivityReadMmsBinding;
import com.sp.smshelper.mmsmessages.MmsDetailsFragment;
import com.sp.smshelper.mmsmessages.MmsMediaFragment;
import com.sp.smshelper.mmsmessages.MmsMessagesFragment;

public class MmsConversationActivity extends AppCompatActivity {

    private ActivityReadMmsBinding mBinding;
    private MmsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_read_mms);

        mViewModel = new ViewModelProvider(this).get(MmsViewModel.class);
        mViewModel.setContext(getBaseContext());

        startMmsConversationFragment();
    }

    /**
     * Launches MMS conversation fragment
     */
    private void startMmsConversationFragment() {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                        MmsConversationFragment.newInstance(),
                        MmsConversationFragment.TAG)
                .commit();
    }

    /**
     * Launches MMS messages fragment
     * @param threadId Thread Id
     */
    public void startMmsMessagesFragment(String threadId) {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                        MmsMessagesFragment.newInstance(threadId),
                        MmsMessagesFragment.TAG)
                .addToBackStack(MmsMessagesFragment.TAG)
                .commit();
    }

    /**
     * Starts MMS details fragment
     * @param messageId
     */
    public void startMmsDetailsFragment(String messageId) {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                        MmsDetailsFragment.newInstance(messageId),
                        MmsDetailsFragment.TAG)
                .addToBackStack(MmsDetailsFragment.TAG)
                .commit();
    }

    /**
     * Displays MMS media fragment
     *
     * @param messageId Message Id
     */
    public void startMmsMediaFragment(String messageId) {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                        MmsMediaFragment.newInstance(messageId),
                        MmsMediaFragment.TAG)
                .addToBackStack(MmsMediaFragment.TAG)
                .commit();
    }

    /**
     * Returns view model object
     * @return View model object
     */
    public MmsViewModel getViewModel() {
        return mViewModel;
    }
}
