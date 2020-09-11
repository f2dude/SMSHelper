package com.sp.smshelper.conversation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ActivityConversationsBinding;
import com.sp.smshelper.messages.SmsMessageDetailsFragment;
import com.sp.smshelper.messages.SmsMessagesFragment;

public class ConversationsActivity extends AppCompatActivity {

    private ConversationsViewModel mViewModel;
    private ActivityConversationsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_conversations);

        mViewModel = new ViewModelProvider(this).get(ConversationsViewModel.class);
        mViewModel.setContext(getBaseContext());
        startConversationsFragment();
    }

    private void startConversationsFragment() {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                    ConversationsFragment.newInstance(),
                    ConversationsFragment.TAG)
                .commit();
    }

    protected void startSmsFragment(String threadId) {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                    SmsMessagesFragment.newInstance(threadId),
                    SmsMessagesFragment.TAG)
                .addToBackStack(SmsMessagesFragment.TAG)
                .commit();
    }

    public void startMessageDetailsFragment(String messageId) {
        //Fragment transaction
        getSupportFragmentManager().beginTransaction()
                .replace(mBinding.actionContainer.getId(),
                        SmsMessageDetailsFragment.newInstance(messageId),
                        SmsMessageDetailsFragment.TAG)
                .addToBackStack(SmsMessageDetailsFragment.TAG)
                .commit();
    }

    public ConversationsViewModel getViewModel() {
        return mViewModel;
    }
}
