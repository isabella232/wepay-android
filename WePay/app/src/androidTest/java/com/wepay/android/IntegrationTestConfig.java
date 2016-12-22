package com.wepay.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.wepay.android.enums.CardReaderStatus;
import com.wepay.android.enums.PaymentMethod;
import com.wepay.android.models.AuthorizationInfo;
import com.wepay.android.models.Config;
import com.wepay.android.models.Error;
import com.wepay.android.models.MockConfig;
import com.wepay.android.models.PaymentInfo;
import com.wepay.android.models.PaymentToken;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class IntegrationTestConfig {

    private static final String CLIENT_ID = "171482";
    private static final Context CONTEXT = InstrumentationRegistry.getTargetContext();
    private static final String ENVIRONMENT = Config.ENVIRONMENT_STAGE;
    private Handler handler = new Handler(Looper.getMainLooper());

    private void runTestOnUiThread(Runnable r) {
        handler.post(r);
    }

    private Config getConfig() {
        return new Config(CONTEXT, CLIENT_ID, ENVIRONMENT).setUseLocation(true).setMockConfig(new MockConfig()
                .setUseMockCardReader(true)
                .setUseMockWepayClient(true));
    }

    /*
    startTransactionForReading tests
     */

    @Test
    public void testRestartTransactionOnOtherErrorTrue_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterOtherErrors(true);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, false, true);

        Assert.assertTrue(statuses.size() >= 5); // There will be more because in mock the error-restart loop does not stop

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(4));
    }

    @Test
    public void testRestartTransactionOnOtherErrorFalse_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterOtherErrors(false);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, false, true);

        Assert.assertEquals(5, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(4));
    }

    @Test
    public void testRestartTransactionOnSuccessTrueSwipe_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(true);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, true, true);

        Assert.assertTrue(statuses.size() >= 5); // There will be more because in mock the success-restart loop does not stop

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(4));
    }

    @Test
    public void testRestartTransactionOnSuccessTrueDip_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(true);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, true, true);

        Assert.assertEquals(5, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(4));
    }

    @Test
    public void testRestartTransactionOnSuccessFalseSwipe_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, true, true);

        Assert.assertEquals(5, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(4));
    }

    @Test
    public void testRestartTransactionOnSuccessFalseDip_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, true, true);

        Assert.assertEquals(5, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(4));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseDipSuccess_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(5, statuses, config, true, true);

        Assert.assertEquals(4, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseDipError_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(5, statuses, config, false, true);

        Assert.assertEquals(4, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseSwipeSuccess_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(5, statuses, config, true, true);

        Assert.assertEquals(4, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseSwipeError_Reading() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(5, statuses, config, false, true);

        Assert.assertEquals(4, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
    }

    /*
    startTransactionForTokenizing tests
     */

    @Test
    public void testRestartTransactionOnOtherErrorTrue_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterOtherErrors(true);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, false, false);

        Assert.assertTrue(statuses.size() >= 5); // There will be more because in mock the error-restart loop does not stop

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(4));
    }

    @Test
    public void testRestartTransactionOnOtherErrorFalse_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterOtherErrors(false);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(6, statuses, config, false, false);

        Assert.assertEquals(5, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(4));
    }

    @Test
    public void testRestartTransactionOnSuccessTrueSwipe_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(true);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(8, statuses, config, true, false);

        Assert.assertTrue(statuses.size() >= 6); // There will be more because in mock the success-restart loop does not stop

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.TOKENIZING, statuses.get(4));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(5));
    }

    @Test
    public void testRestartTransactionOnSuccessTrueDip_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(true);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(8, statuses, config, true, false);

        Assert.assertEquals(6, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.AUTHORIZING, statuses.get(4));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(5));
    }

    @Test
    public void testRestartTransactionOnSuccessFalseSwipe_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(8, statuses, config, true, false);

        Assert.assertEquals(6, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.TOKENIZING, statuses.get(4));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(5));
    }

    @Test
    public void testRestartTransactionOnSuccessFalseDip_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setRestartTransactionAfterSuccess(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(8, statuses, config, true, false);

        Assert.assertEquals(6, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.AUTHORIZING, statuses.get(4));
        Assert.assertEquals(CardReaderStatus.STOPPED, statuses.get(5));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseDipSuccess_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(7, statuses, config, true, false);

        Assert.assertEquals(5, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.AUTHORIZING, statuses.get(4));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseDipError_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.DIP);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(5, statuses, config, false, false);

        Assert.assertEquals(4, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.CARD_DIPPED, statuses.get(3));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseSwipeSuccess_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(7, statuses, config, true, false);

        Assert.assertEquals(5, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
        Assert.assertEquals(CardReaderStatus.TOKENIZING, statuses.get(4));
    }

    @Test
    public void testStopCardReaderAfterTransactionFalseSwipeError_Tokenizing() throws InterruptedException {
        Config config = getConfig();
        config.setStopCardReaderAfterTransaction(false);
        config.getMockConfig().setCardReadFailure(true).setMockPaymentMethod(PaymentMethod.SWIPE);

        final List<CardReaderStatus> statuses = new ArrayList<>();

        this.restartTestHelper(5, statuses, config, false, false);

        Assert.assertEquals(4, statuses.size());

        Assert.assertEquals(CardReaderStatus.CONNECTED, statuses.get(0));
        Assert.assertEquals(CardReaderStatus.CHECKING_READER, statuses.get(1));
        Assert.assertEquals(CardReaderStatus.WAITING_FOR_CARD, statuses.get(2));
        Assert.assertEquals(CardReaderStatus.SWIPE_DETECTED, statuses.get(3));
    }

    /**
     * Helper for configuring handlers and starting transactions
     * @param latchCount number of latch ticks expected. Status change messages, success messages, and failure messages count as latch ticks.
     * @param statuses an empty list of status change messages that will be populated with statuses that are received.
     * @param config the wepay config
     * @param shouldSucceed whether or not the transaction is expected to succeed
     * @param readOnly if true, will start a read transaction, otherwise will start a tokenization transaction
     * @throws InterruptedException
     */
    private void restartTestHelper(int latchCount, final List<CardReaderStatus>statuses, Config config, Boolean shouldSucceed, Boolean readOnly) throws InterruptedException {
        final WePay wePay = new WePay(config);
        final CountDownLatch countDownLatch = new CountDownLatch(latchCount);
        List<CardReaderStatus> statusesCopy;

        final TestCardReaderHandler cardReaderHandler = new TestCardReaderHandler(countDownLatch) {
            @Override
            public void onSuccess(PaymentInfo paymentInfo) {
                super.onSuccess(paymentInfo);
                Log.d("restartTestHelper", paymentInfo.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onError(com.wepay.android.models.Error error) {
                super.onError(error);
                Log.d("restartTestHelper", error.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onStatusChange(CardReaderStatus status) {
                super.onStatusChange(status);
                statuses.add(status);
                Log.d("restartTestHelper", status.toString());
                countDownLatch.countDown();
            }
        };

        final TestTokenizationHandler tokenizationHandler = new TestTokenizationHandler(countDownLatch) {
            @Override
            public void onSuccess(PaymentInfo paymentInfo, PaymentToken token) {
                super.onSuccess(paymentInfo, token);
                Log.d("restartTestHelper", token.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onError(PaymentInfo paymentInfo, com.wepay.android.models.Error error) {
                super.onError(paymentInfo, error);
                Log.d("restartTestHelper", error.toString());
                countDownLatch.countDown();
            }
        };

        final TestAuthorizationHandler authorizationHandler = new TestAuthorizationHandler(null) {

            @Override
            public void onAuthorizationSuccess(PaymentInfo paymentInfo, AuthorizationInfo authorizationInfo) {
                super.onAuthorizationSuccess(paymentInfo, authorizationInfo);
                Log.d("restartTestHelper", authorizationInfo.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onAuthorizationError(PaymentInfo paymentInfo, Error error) {
                super.onAuthorizationError(paymentInfo, error);
                Log.d("restartTestHelper", error.toString());
                countDownLatch.countDown();
            }
        };

        if (readOnly) {
            // Start for reading
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wePay.startTransactionForReading(cardReaderHandler);
                }
            });

        } else {
            // start for tokenizing
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wePay.startTransactionForTokenizing(cardReaderHandler, tokenizationHandler, authorizationHandler);
                }
            });
        }

        countDownLatch.await(3000, TimeUnit.MILLISECONDS);

        // Copy the status list to avoid printing the status list at the same time it's modified.
        statusesCopy = new ArrayList<>(statuses);
        Log.d("restartTestHelper", "statuses: " + statusesCopy.toString());

        if (shouldSucceed) {
            Assert.assertTrue(cardReaderHandler.onSuccessCalled);
            if (!readOnly) {
                Assert.assertTrue(tokenizationHandler.onSuccessCalled || authorizationHandler.onAuthorizationSuccessCalled);
            }
        } else {
            Assert.assertTrue(cardReaderHandler.onErrorCalled);
            Assert.assertEquals(Error.getEmvTransactionErrorWithMessage("UnknownError"), cardReaderHandler.error);
        }
    }
}