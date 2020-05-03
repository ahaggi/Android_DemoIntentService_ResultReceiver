package com.example.demointentservice;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;


import com.example.demointentservice.receivers.BankResultReceiver;
import com.example.demointentservice.service.BankService;


public class MainActivity extends AppCompatActivity {
    TextView label;
    Button depositButton;
    Button withdrawButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label = (TextView) findViewById(R.id.label);
        depositButton = (Button) findViewById(R.id.depositButton);
        withdrawButton = (Button) findViewById(R.id.withdrawButton);

        depositButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServiceToDeposit();
            }
        });
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServiceToWithdraw();
            }
        });
        startServiceForBalance();
    }

    private void startServiceToWithdraw() {
        int amount = 10;

        BankResultReceiver bankResultReceiver = new BankResultReceiver(new Handler(this.getMainLooper()));
        bankResultReceiver.setReceiver(new TransferMoneyResultReceiver(this, false));

        Intent intent = new Intent(this, BankService.class);
        intent.setAction(BankService.Actions.WITHDRAW.name());
        intent.putExtra(BankService.PARAM.AMOUNT.name(), amount);
        intent.putExtra(BankService.PARAM.RESULT_RECEIVER.name(), bankResultReceiver);
        startService(intent);

        startServiceForBalance();
    }

    private void startServiceToDeposit() {
        int amount = 10;

        BankResultReceiver bankResultReceiver = new BankResultReceiver(new Handler(this.getMainLooper()));
        bankResultReceiver.setReceiver(new TransferMoneyResultReceiver(this, true));

        Intent intent = new Intent(this, BankService.class);
        intent.setAction(BankService.Actions.DEPOSIT.name());
        intent.putExtra(BankService.PARAM.AMOUNT.name(), amount);
        intent.putExtra(BankService.PARAM.RESULT_RECEIVER.name(), bankResultReceiver);
        startService(intent);

        startServiceForBalance();
    }

    private void startServiceForBalance() {

        BankResultReceiver bankResultReceiver = new BankResultReceiver(new Handler(this.getMainLooper()));
        bankResultReceiver.setReceiver(new AccountInfoResultReceiver(this));

        Intent intent = new Intent(this, BankService.class);
        intent.setAction(BankService.Actions.BALANCE.name());
        intent.putExtra(BankService.PARAM.RESULT_RECEIVER.name(), bankResultReceiver);
        startService(intent);
    }











    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private static class TransferMoneyResultReceiver implements BankResultReceiver.ResultReceiverCallBack<Boolean> {
        private WeakReference<MainActivity> activityRef;
        private boolean deposit;

        public TransferMoneyResultReceiver(MainActivity activity, boolean deposit) {
            activityRef = new WeakReference<MainActivity>(activity);
            this.deposit = deposit;
        }

        @Override
        public void onSuccess(Boolean data) {
            if (activityRef != null && activityRef.get() != null) {
                activityRef.get().showMessage(deposit ? "Deposited" : "Withdrew");
            }
        }

        @Override
        public void onError(Exception exception) {
            if (activityRef != null && activityRef.get() != null) {
                activityRef.get().showMessage(exception != null ? exception.getMessage() : "Error");
            }
        }
    }

    private static class AccountInfoResultReceiver implements BankResultReceiver.ResultReceiverCallBack<Integer> {
        private WeakReference<MainActivity> activityRef;

        public AccountInfoResultReceiver(MainActivity activity) {
            activityRef = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onSuccess(Integer data) {
            if (activityRef != null && activityRef.get() != null) {
                activityRef.get().label.setText("Your balance: " + data);
            }
        }

        @Override
        public void onError(Exception exception) {
            activityRef.get().showMessage("Account info failed");
        }
    }
}
