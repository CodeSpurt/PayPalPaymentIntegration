package com.codespurt.paypalpaymentintegrationexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "PayPalPayment";
    private Button payForProduct;

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    // get client_id from https://developer.paypal.com/
    private static final String CONFIG_CLIENT_ID = "YOUR_CLIENT_ID_HERE";
    private static final int REQUEST_CODE_PAYMENT = 1001;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 1002;
    private static final int REQUEST_CODE_PROFILE_SHARING = 1003;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID);
    // The following are only used in PayPalFuturePaymentActivity.
//            .merchantName("Example Merchant")
//            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
//            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payForProduct = (Button) findViewById(R.id.btn_pay_for_product);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        payForProduct.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pay_for_product:
                PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                break;
        }
    }

    private PayPalPayment getThingToBuy(String paymentIntent) {
        return new PayPalPayment(new BigDecimal("0.01"), "USD", "Sample Item", paymentIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.d(TAG, confirm.toJSONObject().toString(4));
                        Log.d(TAG, confirm.getPayment().toJSONObject().toString(4));
                        Toast.makeText(this, "PaymentConfirmation info received from PayPal", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Log.d(TAG, "An extremely unlikely failure occurred: ", e);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        payForProduct.setOnClickListener(null);
    }
}
