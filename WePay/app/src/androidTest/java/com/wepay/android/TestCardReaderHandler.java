package com.wepay.android;

import com.wepay.android.enums.CardReaderStatus;
import com.wepay.android.enums.CurrencyCode;
import com.wepay.android.models.Error;
import com.wepay.android.models.PaymentInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class TestCardReaderHandler implements CardReaderHandler {
    public boolean onErrorCalled = false;
    public boolean onSuccessCalled = false;
    public boolean notConnectedStatusChangeCalled = false;
    public boolean onStoppedCalled = false;
    public boolean onConfiguringReaderStatusChangeCalled = false;
    public boolean onReaderResetRequestedCalled = false;
    public boolean onCardReaderSelectionCalled = false;
    public boolean onEMVApplicationSelectionRequestedCalled = false;
    public PaymentInfo paymentInfo;
    public Error error = null;
    public CardReaderStatus mostRecentStatus = null;
    private CountDownLatch countDownLatch;

    public TestCardReaderHandler(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onSuccess(PaymentInfo paymentInfo) {
        onSuccessCalled = true;
        this.paymentInfo = paymentInfo;
    }

    @Override
    public void onError(Error error) {
        onErrorCalled = true;
        this.error = error;
    }

    @Override
    public void onStatusChange(CardReaderStatus status) {
        this.mostRecentStatus = status;
    }

    @Override
    public void onEMVApplicationSelectionRequested(ApplicationSelectionCallback callback, ArrayList<String> applications) {
        onEMVApplicationSelectionRequestedCalled = true;
        callback.useApplicationAtIndex(applications.size() - 1);
    }

    @Override
    public void onReaderResetRequested(CardReaderResetCallback callback) {
        onReaderResetRequestedCalled = true;
        callback.resetCardReader(false);
    }

    @Override
    public void onTransactionInfoRequested(CardReaderTransactionInfoCallback callback) {
        // 24.61 is the magic number that will lead to authorization success.
        callback.useTransactionInfo(new BigDecimal("24.61"), CurrencyCode.USD, 1170640190);
    }

    @Override
    public void onPayerEmailRequested(CardReaderEmailCallback callback) {
        callback.insertPayerEmail("a@b.com");
    }

    @Override
    public void onCardReaderSelection(CardReaderSelectionCallback callback, ArrayList<String> devices) {
        onCardReaderSelectionCalled = true;
        callback.useCardReaderAtIndex(0);
    }
}
