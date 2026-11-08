package com.smartwallet.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int RED = Color.rgb(200, 16, 46);
    private static final int DARK_RED = Color.rgb(143, 11, 33);
    private static final int INK = Color.rgb(35, 39, 47);
    private static final int MUTED = Color.rgb(112, 119, 130);
    private static final int PAGE = Color.rgb(247, 248, 250);
    private static final int REQUEST_PERMISSIONS = 71;
    // Vodafone Egypt publishes *#100 as a date/time inquiry. This is a non-financial test.
    private static final String TEST_USSD = "*#100";

    private Spinner simSpinner;
    private Button testButton;
    private TextView resultView;
    private TextView permissionView;
    private TextView diagnosticView;
    private final List<SubscriptionInfo> subscriptions = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean requestInProgress = false;
    private int requestId = 0;
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(DARK_RED);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        buildScreen();
        checkPermissionsAndLoadSims();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(PAGE);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        page.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(page);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(22), dp(22), dp(22), dp(24));
        header.setBackground(roundGradient(DARK_RED, RED, 24));
        page.addView(header, matchWrap());

        TextView brand = text("SMART WALLET TEST", 13, Color.WHITE, true);
        brand.setLetterSpacing(0.08f);
        header.addView(brand);
        TextView title = text("اختبار USSD في الخلفية", 24, Color.WHITE, true);
        title.setPadding(0, dp(12), 0, dp(5));
        header.addView(title);
        header.addView(text("تجربة تقنية آمنة دون تحويل أموال", 14,
                Color.rgb(255, 224, 230), false));

        TextView section = text("اختيار شريحة فودافون", 18, INK, true);
        section.setPadding(0, dp(24), 0, dp(10));
        page.addView(section);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(Color.WHITE, 20));
        page.addView(card, matchWrap());

        permissionView = text("جارٍ التحقق من أذونات الهاتف...", 13, MUTED, false);
        card.addView(permissionView);

        diagnosticView = text("لم يتم إجراء فحص التوافق بعد.", 13, INK, false);
        diagnosticView.setPadding(dp(12), dp(12), dp(12), dp(12));
        diagnosticView.setBackground(round(Color.rgb(240, 242, 245), 12));
        LinearLayout.LayoutParams diagnosticParams = matchWrap();
        diagnosticParams.topMargin = dp(10);
        card.addView(diagnosticView, diagnosticParams);

        simSpinner = new Spinner(this);
        LinearLayout.LayoutParams spinnerParams = matchWrap();
        spinnerParams.topMargin = dp(12);
        card.addView(simSpinner, spinnerParams);

        Button refresh = button("تحديث الشرائح");
        LinearLayout.LayoutParams refreshParams = matchWrap();
        refreshParams.topMargin = dp(12);
        card.addView(refresh, refreshParams);
        refresh.setOnClickListener(v -> checkPermissionsAndLoadSims());

        TextView testTitle = text("الاختبار الذي سنرسله", 16, INK, true);
        testTitle.setPadding(0, dp(22), 0, dp(8));
        page.addView(testTitle);
        TextView code = text(TEST_USSD, 22, RED, true);
        code.setGravity(Gravity.CENTER);
        code.setPadding(0, dp(12), 0, dp(12));
        code.setBackground(round(Color.WHITE, 12));
        page.addView(code, matchWrap());

        TextView explanation = text(
                "هذا كود استعلام التاريخ والوقت المنشور ضمن أكواد فودافون إكسبريس، وليس كود تحويل أو دفع. "
                + "قد تختلف الاستجابة حسب الخط أو الشبكة. لن نطلب رقمك السري ولن نرسل أي بيانات مالية.",
                13, MUTED, false);
        explanation.setPadding(0, dp(12), 0, dp(14));
        page.addView(explanation);

        testButton = button("إرسال الاختبار واستقبال الرد");
        page.addView(testButton, matchWrap());
        testButton.setOnClickListener(v -> sendTestRequest());

        TextView resultTitle = text("نتيجة الشبكة", 17, INK, true);
        resultTitle.setPadding(0, dp(22), 0, dp(8));
        page.addView(resultTitle);

        resultView = text("لم يتم إرسال الاختبار بعد.", 14, INK, false);
        resultView.setPadding(dp(14), dp(14), dp(14), dp(14));
        resultView.setBackground(round(Color.WHITE, 12));
        page.addView(resultView, matchWrap());

        TextView safety = text(
                "مهم: هذا الاختبار يقيس طلب USSD واحدًا ورد الشبكة فقط. لا يختبر تحويل الأموال، "
                + "ولا يثبت إمكانية متابعة قوائم USSD متعددة الخطوات أو تنفيذها في الخلفية.",
                12, MUTED, false);
        safety.setPadding(0, dp(18), 0, 0);
        page.addView(safety);

        setContentView(scroll);
    }

    private void checkPermissionsAndLoadSims() {
        if (Build.VERSION.SDK_INT < 26) {
            permissionView.setText("هذا الاختبار يحتاج Android 8 أو أحدث.");
            return;
        }
        List<String> needed = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.CALL_PHONE);
        }
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!needed.isEmpty()) {
            permissionView.setText("نحتاج إذن الاتصال وقراءة حالة الشرائح لاختيار شريحة Vodafone.");
            requestPermissions(needed.toArray(new String[0]), REQUEST_PERMISSIONS);
            return;
        }
        loadSims();
    }

    private void loadSims() {
        subscriptions.clear();
        try {
            SubscriptionManager manager = (SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> active = manager.getActiveSubscriptionInfoList();
            if (active != null) subscriptions.addAll(active);
        } catch (SecurityException ex) {
            permissionView.setText("لم يسمح الهاتف بقراءة بيانات الشرائح.");
            return;
        }

        List<String> labels = new ArrayList<>();
        int selected = 0;
        for (int i = 0; i < subscriptions.size(); i++) {
            SubscriptionInfo info = subscriptions.get(i);
            String carrier = String.valueOf(info.getCarrierName());
            String label = "SIM " + (info.getSimSlotIndex() + 1) + " — " + carrier;
            labels.add(label);
            String normalized = carrier.toLowerCase(Locale.ROOT);
            if (normalized.contains("vodafone") || normalized.contains("فودافون")) selected = i;
        }
        if (labels.isEmpty()) labels.add("لم يتم العثور على شريحة نشطة");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        simSpinner.setAdapter(adapter);
        if (!subscriptions.isEmpty()) simSpinner.setSelection(selected);
        permissionView.setText(subscriptions.isEmpty()
                ? "لم يعثر أندرويد على شرائح نشطة."
                : "تم العثور على " + subscriptions.size() + " شريحة. اختر شريحة Vodafone.");
    }

    private void sendTestRequest() {
        if (requestInProgress) return;
        if (Build.VERSION.SDK_INT < 26) {
            showResult("إصدار أندرويد لا يدعم واجهة إرسال USSD المطلوبة.");
            return;
        }
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndLoadSims();
            return;
        }
        int position = simSpinner.getSelectedItemPosition();
        if (position < 0 || position >= subscriptions.size()) {
            showResult("اختر شريحة نشطة أولًا، ثم اضغط تحديث الشرائح.");
            return;
        }

        SubscriptionInfo selected = subscriptions.get(position);
        TelephonyManager base = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        TelephonyManager forSim = base.createForSubscriptionId(selected.getSubscriptionId());
        String carrier = String.valueOf(selected.getCarrierName());
        int simState = forSim.getSimState();
        String simStateText = simState == TelephonyManager.SIM_STATE_READY ? "جاهزة" :
                simState == TelephonyManager.SIM_STATE_ABSENT ? "غير موجودة" :
                simState == TelephonyManager.SIM_STATE_PIN_REQUIRED ? "تحتاج PIN للشريحة" :
                simState == TelephonyManager.SIM_STATE_PUK_REQUIRED ? "تحتاج PUK للشريحة" :
                "حالة رقمها " + simState;
        boolean apiSupported = Build.VERSION.SDK_INT >= 29 && forSim.isUssdApiSupported();
        String supportText = Build.VERSION.SDK_INT >= 29
                ? (apiSupported ? "واجهة USSD مدعومة حسب أندرويد." : "واجهة USSD غير مدعومة حسب أندرويد لهذه الشريحة/الجهاز.")
                : "لا تتوفر قراءة حالة دعم واجهة USSD على إصدار أندرويد هذا.";
        diagnosticView.setText("نتيجة فحص التوافق:\nالشريحة: SIM " + (selected.getSimSlotIndex() + 1)
                + " — " + carrier + "\nحالة الشريحة: " + simStateText + "\n" + supportText);
        if (Build.VERSION.SDK_INT >= 29 && !apiSupported) {
            showResult("أوقفنا الإرسال لأن أندرويد أفاد بأن واجهة USSD غير مدعومة. لم يتم إرسال أي طلب.");
            return;
        }
        final int thisRequestId = ++requestId;
        requestInProgress = true;
        testButton.setEnabled(false);
        testButton.setText("جارٍ انتظار رد الشبكة...");
        showResult("جارٍ إرسال طلب الاختبار إلى SIM " + (selected.getSimSlotIndex() + 1)
                + ". سننتظر 30 ثانية كحد أقصى، وقد يصل الرد بعدها متأخرًا.");

        timeoutRunnable = () -> {
            if (thisRequestId != requestId || !requestInProgress) return;
            requestInProgress = false;
            testButton.setEnabled(true);
            testButton.setText("إعادة الاختبار");
            resultView.setText("لم يصل رد خلال 30 ثانية. انتهت مهلة الانتظار داخل التطبيق، "
                    + "لكن قد يصل رد الشبكة متأخرًا. لا يعني ذلك وحده أن الكود غير صحيح.");
        };
        mainHandler.postDelayed(timeoutRunnable, 30000);

        try {
            forSim.sendUssdRequest(TEST_USSD, new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager,
                                                  String request, CharSequence response) {
                    completeRequest(thisRequestId, "وصل رد من الشبكة:\n\n" + response);
                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager,
                                                        String request, int failureCode) {
                    completeRequest(thisRequestId, "لم يكتمل طلب USSD. رمز الخطأ: " + failureCode
                            + "\nقد يكون السبب عدم دعم الشبكة أو الهاتف لهذا الطلب.");
                }
            }, mainHandler);
        } catch (SecurityException ex) {
            completeRequest(thisRequestId, "رفض أندرويد الإذن اللازم لإرسال طلب USSD.");
        } catch (UnsupportedOperationException ex) {
            completeRequest(thisRequestId, "الهاتف لا يوفر دعمًا لميزة إرسال USSD عبر هذه الواجهة.");
        } catch (RuntimeException ex) {
            completeRequest(thisRequestId, "تعذر بدء الاختبار: " + ex.getClass().getSimpleName());
        }
    }

    private void completeRequest(int id, String message) {
        runOnUiThread(() -> {
            if (id != requestId) return;
            if (timeoutRunnable != null) {
                mainHandler.removeCallbacks(timeoutRunnable);
                timeoutRunnable = null;
            }
            requestInProgress = false;
            testButton.setEnabled(true);
            testButton.setText("إعادة الاختبار");
            resultView.setText(message);
        });
    }

    private void showResult(String value) {
        runOnUiThread(() -> resultView.setText(value));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = grantResults.length > 0;
            for (int result : grantResults) allGranted &= result == PackageManager.PERMISSION_GRANTED;
            if (allGranted) loadSims();
            else permissionView.setText("لم تُمنح كل الأذونات؛ لن يعمل اختبار USSD دونها.");
        }
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setPadding(dp(12), dp(12), dp(12), dp(12));
        b.setBackground(round(RED, 14));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable roundGradient(int start, int end, int radius) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, new int[]{start, end});
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
