package com.smartwallet.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int RED = Color.rgb(198, 66, 75);
    private static final int DARK_RED = Color.rgb(23, 105, 224);
    private static final int GREEN = Color.rgb(22, 163, 74);
    private static final int INK = Color.rgb(28, 37, 51);
    private static final int MUTED = Color.rgb(91, 103, 120);
    private static final int PAGE = Color.rgb(244, 247, 252);
    private static final int ROYAL_BLUE = Color.rgb(23, 105, 224);
    private static final int TEAL = Color.rgb(8, 127, 140);
    private static final int NAVY = Color.rgb(39, 54, 83);
    private static final int FIELD_BORDER = Color.rgb(211, 219, 230);
    private static final int CONTACT_PICKER = 410;
    private static final String PREFS = "smart_wallet_preferences";
    private static final String FAVORITES_KEY = "favorite_recipients";

    private EditText phoneInput;
    private EditText amountInput;
    private EditText pinInput;
    private Button etisalatRecordButton;
    private String lastEtisalatPhone;
    private String lastEtisalatAmount;
    private final Handler pinMaskHandler = new Handler(Looper.getMainLooper());
    private Runnable hidePinRunnable;
    private AsteriskPasswordTransformationMethod pinMaskMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(RED);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        showHome();
    }

    private void showHome() {
        LinearLayout page = basePage();
        page.setPadding(dp(16), dp(18), dp(16), dp(12));

        TextView title = label("Smart Wallet", 30, NAVY, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(12), 0, dp(8));
        page.addView(title);
        TextView subtitle = label("اختر المحفظة التي تريد استخدامها", 19, MUTED, true);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, dp(22));
        page.addView(subtitle);

        LinearLayout vodafone = compactCard(page);
        vodafone.setPadding(dp(18), dp(20), dp(18), dp(20));
        vodafone.setBackground(gradient(Color.rgb(220, 20, 60), Color.rgb(170, 10, 45), 22));
        TextView vLogo = label("◉  vodafone cash", 25, Color.WHITE, true);
        vLogo.setGravity(Gravity.CENTER);
        vodafone.addView(vLogo, matchWrap());
        TextView vHint = label("خدمات محفظة فودافون كاش", 16, Color.WHITE, false);
        vHint.setGravity(Gravity.CENTER);
        vHint.setPadding(0, dp(8), 0, 0);
        vodafone.addView(vHint);
        vodafone.setOnClickListener(v -> showVodafoneHome());

        LinearLayout etisalat = compactCard(page);
        etisalat.setPadding(dp(18), dp(20), dp(18), dp(20));
        etisalat.setBackground(gradient(Color.rgb(65, 25, 125), Color.rgb(35, 15, 85), 22));
        TextView eLogo = label("e&  CASH", 27, Color.WHITE, true);
        eLogo.setGravity(Gravity.CENTER);
        etisalat.addView(eLogo, matchWrap());
        TextView eHint = label("خدمات محفظة e& كاش", 16, Color.WHITE, false);
        eHint.setGravity(Gravity.CENTER);
        eHint.setPadding(0, dp(8), 0, 0);
        etisalat.addView(eHint);
        etisalat.setOnClickListener(v -> showEtisalatHome());

        TextView offline = label("يعمل التطبيق دون اتصال بالإنترنت", 17, GREEN, true);
        offline.setGravity(Gravity.CENTER);
        offline.setPadding(0, dp(22), 0, dp(8));
        page.addView(offline);
        TextView version = label("SMART WALLET  •  3.10", 12, MUTED, true);
        version.setGravity(Gravity.CENTER);
        page.addView(version);
        setContentView(redFrame(page));
    }

    private void showVodafoneHome() {
        LinearLayout page = basePage();
        page.setPadding(dp(16), dp(14), dp(16), dp(8));
        Button back = button("رجوع لاختيار المحفظة", Color.rgb(235, 238, 243), INK);
        back.setOnClickListener(v -> showHome());
        page.addView(back, matchWrap());
        TextView section = label("خدمات فودافون كاش", 22, NAVY, true);
        section.setPadding(dp(2), dp(14), 0, dp(10));
        page.addView(section);
        addService(page, "الاستعلام عن الرصيد", "اعرف رصيد محفظتك بسهولة", "◉", ROYAL_BLUE, v -> openUssd("*9*13#"));
        addService(page, "تحويل الأموال", "حوّل الأموال إلى رقم محمول أو جهة اتصال", "⇄", TEAL, v -> showTransfer());
        TextView version = label("SMART WALLET  •  3.10", 12, MUTED, true);
        version.setGravity(Gravity.CENTER);
        page.addView(version);
        setContentView(redFrame(page));
    }

    private void showEtisalatHome() {
        LinearLayout page = basePage();
        Button back = button("رجوع لاختيار المحفظة", Color.rgb(235, 238, 243), INK);
        back.setOnClickListener(v -> showHome());
        page.addView(back, matchWrap());
        TextView section = label("خدمات e& كاش", 23, NAVY, true);
        section.setPadding(0, dp(18), 0, dp(12));
        page.addView(section);
        addService(page, "الاستعلام عن الرصيد", "استعلم عن رصيد محفظتك", "◉", ROYAL_BLUE, v -> openUssd("*777*4#"));
        addService(page, "تحويل الأموال", "إدخال بيانات التحويل ومراجعتها قبل الاتصال", "⇄", TEAL, v -> showEtisalatTransfer());
        TextView offline = label("لا يحتاج التطبيق إلى الإنترنت", 16, GREEN, true);
        offline.setGravity(Gravity.CENTER);
        offline.setPadding(0, dp(18), 0, 0);
        page.addView(offline);
        setContentView(redFrame(page));
    }

    private void showEtisalatTransfer() {
        lastEtisalatPhone = null;
        lastEtisalatAmount = null;
        etisalatRecordButton = null;
        LinearLayout page = basePage();
        Button back = button("رجوع إلى خدمات e& كاش", Color.rgb(235, 238, 243), INK);
        back.setOnClickListener(v -> showEtisalatHome());
        page.addView(back, matchWrap());

        LinearLayout form = card(page);
        form.setPadding(dp(8), dp(12), dp(8), dp(12));
        TextView heading = label("تحويل الأموال", 25, NAVY, true);
        heading.setPadding(0, 0, 0, dp(12));
        form.addView(heading);

        TextView phoneLabel = label("1- رقم المحول إليه", 20, INK, true);
        phoneLabel.setPadding(0, 0, 0, dp(6));
        form.addView(phoneLabel);

        LinearLayout phoneRow = new LinearLayout(this);
        phoneRow.setOrientation(LinearLayout.HORIZONTAL);
        phoneRow.setGravity(Gravity.CENTER_VERTICAL);
        phoneRow.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        phoneRow.setPadding(dp(4), dp(4), dp(4), dp(4));
        phoneRow.setBackground(inputBorder(FIELD_BORDER));
        form.addView(phoneRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));

        Button favorites = button("♡", Color.TRANSPARENT, RED);
        styleInlineIcon(favorites, "الأرقام المفضلة");
        favorites.setTextSize(25);
        phoneRow.addView(favorites, new LinearLayout.LayoutParams(dp(34), dp(52)));
        favorites.setOnClickListener(v -> showFavorites());

        Button contacts = button("♙", Color.TRANSPARENT, RED);
        styleInlineIcon(contacts, "اختيار رقم من جهات الاتصال");
        contacts.setTextSize(24);
        phoneRow.addView(contacts, new LinearLayout.LayoutParams(dp(34), dp(52)));
        contacts.setOnClickListener(v -> chooseContact());

        phoneInput = new EditText(this);
        phoneInput.setSingleLine(true);
        phoneInput.setMaxLines(1);
        phoneInput.setHorizontallyScrolling(true);
        phoneInput.setMinWidth(0);
        phoneInput.setMinimumWidth(0);
        phoneInput.setTextSize(19);
        phoneInput.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        phoneInput.setTextColor(INK);
        phoneInput.setHintTextColor(Color.rgb(145, 153, 165));
        phoneInput.setHint("01xxxxxxxxx");
        phoneInput.setInputType(3);
        phoneInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        phoneInput.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        phoneInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        phoneInput.setPadding(dp(4), dp(8), dp(4), dp(8));
        phoneInput.setBackgroundColor(Color.TRANSPARENT);
        phoneRow.addView(phoneInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        TextView phoneValidation = label("أدخل رقمًا مصريًا من 11 رقمًا", 15, MUTED, false);
        phoneValidation.setPadding(dp(2), dp(7), 0, dp(2));
        form.addView(phoneValidation);
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString().trim();
                boolean valid = value.matches("01[0125][0-9]{8}");
                phoneRow.setBackground(inputBorder(valid ? GREEN : FIELD_BORDER));
                if (value.isEmpty()) {
                    phoneValidation.setText("أدخل رقمًا مصريًا من 11 رقمًا");
                    phoneValidation.setTextColor(MUTED);
                } else if (valid) {
                    phoneValidation.setText("✓ رقم صحيح ومكتمل");
                    phoneValidation.setTextColor(GREEN);
                } else {
                    phoneValidation.setText(value.length() >= 11
                            ? "رقم غير صحيح، راجع الرقم"
                            : "الرقم غير مكتمل — يجب أن يكون 11 رقمًا");
                    phoneValidation.setTextColor(RED);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextView amountLabel = label("2- مبلغ التحويل بالجنيه", 19, INK, true);
        amountLabel.setPadding(0, dp(13), 0, dp(5));
        form.addView(amountLabel);
        amountInput = new EditText(this);
        amountInput.setSingleLine(true);
        amountInput.setTextSize(21);
        amountInput.setInputType(8194);
        amountInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        amountInput.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        amountInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        amountInput.setHint("أدخل المبلغ");
        amountInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        amountInput.setBackground(inputBorder(FIELD_BORDER));
        form.addView(amountInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(66)));

        TextView pinLabel = label("3- الرقم السري للمحفظة", 19, INK, true);
        pinLabel.setPadding(0, dp(13), 0, dp(5));
        form.addView(pinLabel);
        pinInput = new EditText(this);
        pinInput.setSingleLine(true);
        pinInput.setTextSize(22);
        pinInput.setInputType(2);
        pinInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        pinInput.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        pinInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        pinInput.setHint("أدخل الرقم السري");
        pinMaskMethod = new AsteriskPasswordTransformationMethod();
        pinInput.setTransformationMethod(pinMaskMethod);
        pinInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        pinInput.setBackground(inputBorder(FIELD_BORDER));
        form.addView(pinInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(66)));
        pinInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (hidePinRunnable != null) pinMaskHandler.removeCallbacks(hidePinRunnable);
                pinMaskMethod.setRevealLastDigit(true);
                pinInput.setTransformationMethod(pinMaskMethod);
                int cursor = pinInput.getSelectionStart();
                hidePinRunnable = () -> {
                    if (pinInput != null && pinMaskMethod != null) {
                        pinMaskMethod.setRevealLastDigit(false);
                        pinInput.setTransformationMethod(pinMaskMethod);
                        pinInput.setSelection(Math.max(0, Math.min(cursor, pinInput.length())));
                    }
                };
                pinMaskHandler.postDelayed(hidePinRunnable, 900);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextView warning = label("ملحوظة مهمة: بعد الضغط على تنفيذ التحويل، اضغط رقم 1 ثم اضغط إرسال لإتمام عملية التحويل.", 15, RED, true);
        warning.setPadding(0, dp(12), 0, dp(12));
        form.addView(warning);
        Button execute = button("تنفيذ التحويل", RED, Color.WHITE);
        form.addView(execute, matchWrap());
        execute.setOnClickListener(v -> prepareEtisalatTransfer());

        etisalatRecordButton = button("تأكيد نجاح التحويل وعرض السجل", TEAL, Color.WHITE);
        LinearLayout.LayoutParams recordParams = matchWrap();
        recordParams.topMargin = dp(10);
        form.addView(etisalatRecordButton, recordParams);
        etisalatRecordButton.setVisibility(View.GONE);
        etisalatRecordButton.setOnClickListener(v -> askForEtisalatManualConfirmation());

        TextView privacy = label("الرقم السري لا يُحفظ في المفضلة أو في ملفات التطبيق.", 14, GREEN, true);
        privacy.setPadding(0, dp(12), 0, 0);
        form.addView(privacy);
        setContentView(wrapScroll(redFrame(page)));
    }

    private static class AsteriskPasswordTransformationMethod implements TransformationMethod {
        private boolean revealLastDigit;
        void setRevealLastDigit(boolean reveal) { revealLastDigit = reveal; }
        @Override public CharSequence getTransformation(CharSequence source, View view) {
            return new CharSequence() {
                @Override public int length() { return source.length(); }
                @Override public char charAt(int index) { return revealLastDigit && index == source.length() - 1 ? source.charAt(index) : '*'; }
                @Override public CharSequence subSequence(int start, int end) {
                    StringBuilder out = new StringBuilder();
                    for (int i = start; i < end; i++) out.append(charAt(i));
                    return out.toString();
                }
                @Override public String toString() {
                    StringBuilder out = new StringBuilder();
                    for (int i = 0; i < source.length(); i++) out.append(charAt(i));
                    return out.toString();
                }
            };
        }
        @Override public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, android.graphics.Rect previouslyFocusedRect) {}
    }

    private void prepareEtisalatTransfer() {
        String phone = phoneInput.getText().toString().trim();
        String amount = amountInput.getText().toString().trim();
        String pin = pinInput.getText().toString();
        if (!phone.matches("01[0125][0-9]{8}")) {
            phoneInput.setError("أدخل رقمًا مصريًا صحيحًا من 11 رقمًا");
            phoneInput.requestFocus();
            return;
        }
        if (!amount.matches("[0-9]+")) {
            amountInput.setError("أدخل مبلغًا صحيحًا بالأرقام");
            amountInput.requestFocus();
            return;
        }
        try {
            if (Long.parseLong(amount) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            amountInput.setError("يجب أن يكون المبلغ أكبر من صفر");
            amountInput.requestFocus();
            return;
        }
        if (!pin.matches("[0-9]+")) {
            pinInput.setError("أدخل الرقم السري بالأرقام");
            pinInput.requestFocus();
            return;
        }
        lastEtisalatPhone = phone;
        lastEtisalatAmount = amount;
        if (etisalatRecordButton != null) etisalatRecordButton.setVisibility(View.VISIBLE);
        String ussd = "*777*1*" + pin + "*" + amount + "*" + phone + "*" + phone + "#";
        if (hidePinRunnable != null) pinMaskHandler.removeCallbacks(hidePinRunnable);
        if (pinMaskMethod != null) pinMaskMethod.setRevealLastDigit(false);
        pinInput.setText("");
        if (hidePinRunnable != null) pinMaskHandler.removeCallbacks(hidePinRunnable);
        hidePinRunnable = null;
        pin = null;
        openUssd(ussd);
        ussd = null;
    }

    private void askForEtisalatManualConfirmation() {
        if (lastEtisalatPhone == null || lastEtisalatAmount == null) return;
        new AlertDialog.Builder(this)
                .setTitle("تأكيد نجاح التحويل")
                .setMessage("هل شاهدت رسالة نجاح التحويل داخل تطبيق e& كاش الرسمي؟ سيُنشأ سجل محلي بناءً على تأكيدك.")
                .setNegativeButton("رجوع", null)
                .setPositiveButton("نعم، عرض السجل", (dialog, which) -> showEtisalatLocalRecord())
                .show();
    }

    private void showEtisalatLocalRecord() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(PAGE);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(18), dp(28), dp(18), dp(28));
        scroll.addView(page);

        LinearLayout receipt = new LinearLayout(this);
        receipt.setOrientation(LinearLayout.VERTICAL);
        receipt.setGravity(Gravity.CENTER_HORIZONTAL);
        receipt.setPadding(dp(22), dp(26), dp(22), dp(24));
        receipt.setBackground(round(Color.WHITE, 20));
        receipt.setElevation(dp(2));
        page.addView(receipt, matchWrap());

        TextView brand = label("Smart Wallet", 17, NAVY, true);
        brand.setGravity(Gravity.CENTER);
        receipt.addView(brand, matchWrap());

        TextView check = new TextView(this);
        check.setText("✓");
        check.setTextSize(36);
        check.setTextColor(Color.WHITE);
        check.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        check.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(GREEN);
        check.setBackground(circle);
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(dp(70), dp(70));
        checkParams.topMargin = dp(14);
        receipt.addView(check, checkParams);

        TextView done = label("تم التحويل بنجاح", 27, GREEN, true);
        done.setGravity(Gravity.CENTER);
        done.setPadding(0, dp(16), 0, dp(18));
        receipt.addView(done, matchWrap());

        TextView amountCaption = label("المبلغ المحوّل", 16, MUTED, true);
        amountCaption.setGravity(Gravity.CENTER);
        receipt.addView(amountCaption, matchWrap());

        TextView amountLine = label(formatEtisalatAmount(lastEtisalatAmount) + " جنيه", 27, INK, true);
        amountLine.setGravity(Gravity.CENTER);
        amountLine.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        amountLine.setPadding(0, dp(4), 0, dp(18));
        receipt.addView(amountLine, matchWrap());

        View separator = new View(this);
        separator.setBackgroundColor(Color.rgb(230, 235, 241));
        receipt.addView(separator, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(0, dp(12), 0, dp(10));
        receipt.addView(details, matchWrap());

        addReceiptDetailRow(details, "المحفظة", "e& Cash", false);
        addReceiptDetailRow(details, "رقم المستلم", lastEtisalatPhone, true);
        String receiptDate = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new java.util.Date());
        String receiptTime = new java.text.SimpleDateFormat("hh:mm a", Locale.US).format(new java.util.Date());
        String localRecordId = String.format(Locale.US, "SW-%013d", System.currentTimeMillis());
        addReceiptDetailRow(details, "التاريخ", receiptDate, true);
        addReceiptDetailRow(details, "الوقت", receiptTime, true);
        addReceiptDetailRow(details, "رقم السجل", localRecordId, true);

        LinearLayout noteBox = new LinearLayout(this);
        noteBox.setOrientation(LinearLayout.VERTICAL);
        noteBox.setPadding(dp(12), dp(12), dp(12), dp(12));
        noteBox.setBackground(round(Color.rgb(237, 248, 240), 12));
        TextView disclosure = label("سجل محلي محفوظ بواسطة Smart Wallet", 14, Color.rgb(28, 112, 62), true);
        disclosure.setGravity(Gravity.CENTER);
        disclosure.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noteBox.addView(disclosure, matchWrap());
        receipt.addView(noteBox, matchWrap());

        Button share = button("مشاركة الإيصال", GREEN, Color.WHITE);
        LinearLayout.LayoutParams shareParams = matchWrap();
        shareParams.topMargin = dp(18);
        page.addView(share, shareParams);
        share.setOnClickListener(v -> shareEtisalatReceiptImage(receipt));
        setContentView(scroll);
    }

    private void addReceiptDetailRow(LinearLayout parent, String title, String value, boolean ltrValue) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView key = label(title, 15, MUTED, false);
        key.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        row.addView(key, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView val = label(value, 16, INK, true);
        val.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        if (ltrValue) val.setTextDirection(View.TEXT_DIRECTION_LTR);
        row.addView(val, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        parent.addView(row, matchWrap());

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(239, 242, 246));
        parent.addView(divider, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
    }

    private String formatEtisalatAmount(String raw) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat format = new DecimalFormat("#,##0.##", symbols);
            return format.format(Double.parseDouble(raw));
        } catch (Exception ex) {
            return raw;
        }
    }

    private void shareEtisalatReceiptImage(View receipt) {
        try {
            int width = receipt.getWidth();
            int height = receipt.getHeight();
            if (width <= 0 || height <= 0) {
                Toast.makeText(this, "تعذر تجهيز صورة السجل. حاول مرة أخرى.", Toast.LENGTH_LONG).show();
                return;
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            receipt.draw(canvas);

            File dir = new File(getCacheDir(), "shared_records");
            if (!dir.exists() && !dir.mkdirs()) throw new Exception("cache");
            File imageFile = new File(dir, "etisalat_cash_record.png");
            try (FileOutputStream output = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                output.flush();
            }
            bitmap.recycle();

            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", imageFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "مشاركة سجل العملية"));
        } catch (Exception ex) {
            Toast.makeText(this, "تعذر إنشاء صورة المشاركة.", Toast.LENGTH_LONG).show();
        }
    }

    private void showTransfer() {
        LinearLayout page = basePage();
        Button back = button("رجوع إلى خدمات فودافون", Color.rgb(235, 238, 243), INK);
        back.setOnClickListener(v -> showVodafoneHome());
        page.addView(back, matchWrap());

        LinearLayout form = card(page);
        form.setPadding(dp(8), dp(12), dp(8), dp(12));
        LinearLayout.LayoutParams formParams = (LinearLayout.LayoutParams) form.getLayoutParams();
        formParams.topMargin = dp(10);
        form.setLayoutParams(formParams);
        TextView phoneLabel = label("تحويل إلى", 27, NAVY, true);
        phoneLabel.setPadding(0, 0, 0, dp(10));
        form.addView(phoneLabel);

        LinearLayout phoneRow = new LinearLayout(this);
        phoneRow.setOrientation(LinearLayout.HORIZONTAL);
        phoneRow.setGravity(Gravity.CENTER_VERTICAL);
        // ترتيب مادي ثابت: المفضلة أقصى اليسار، ثم جهات الاتصال، ثم حقل الرقم الواسع.
        phoneRow.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        form.addView(phoneRow, matchWrap());

        phoneInput = new EditText(this);
        phoneInput.setSingleLine(true);
        phoneInput.setTextSize(19);
        phoneInput.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        phoneInput.setHorizontallyScrolling(true);
        phoneInput.setMinWidth(0);
        phoneInput.setMinimumWidth(0);
        phoneInput.setMaxLines(1);
        phoneInput.setSelectAllOnFocus(false);
        phoneInput.setTextColor(INK);
        phoneInput.setHintTextColor(Color.rgb(145, 153, 165));
        phoneInput.setHint("");
        phoneInput.setInputType(3);
        phoneInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        phoneInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        phoneInput.setPadding(dp(3), dp(8), dp(3), dp(8));
        phoneInput.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        phoneInput.setBackgroundColor(Color.TRANSPARENT);

        Button favorites = button("♡", Color.TRANSPARENT, RED);
        styleInlineIcon(favorites, "الأرقام المفضلة");
        phoneRow.addView(favorites, new LinearLayout.LayoutParams(dp(40), dp(58)));
        favorites.setOnClickListener(v -> showFavorites());

        Button contacts = button("👤", Color.TRANSPARENT, RED);
        styleInlineIcon(contacts, "اختيار رقم من جهات الاتصال");
        phoneRow.addView(contacts, new LinearLayout.LayoutParams(dp(40), dp(58)));
        contacts.setOnClickListener(v -> chooseContact());

        LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        phoneParams.leftMargin = dp(2);
        phoneRow.addView(phoneInput, phoneParams);

        phoneRow.setPadding(dp(5), dp(5), dp(5), dp(5));
        phoneRow.setBackground(inputBorder(FIELD_BORDER));

        TextView validation = label("أدخل رقمًا مصريًا من 11 رقمًا", 16, MUTED, false);
        validation.setPadding(0, dp(9), 0, dp(16));
        form.addView(validation);
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString().trim();
                boolean valid = value.matches("01[0125][0-9]{8}");
                phoneInput.setBackgroundColor(Color.TRANSPARENT);
                if (valid) {
                    validation.setText("✓ رقم صحيح ومكتمل");
                    validation.setTextColor(GREEN);
                } else {
                    validation.setText(value.length() >= 11 ? "رقم غير صحيح، راجع الرقم" : "الرقم غير مكتمل — يجب أن يكون 11 رقمًا");
                    validation.setTextColor(RED);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextView amountLabel = label("المبلغ بالجنيه المصري", 21, INK, true);
        amountLabel.setPadding(0, dp(8), 0, dp(10));
        form.addView(amountLabel);
        amountInput = new EditText(this);
        amountInput.setSingleLine(true);
        amountInput.setTextSize(23);
        amountInput.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        amountInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        amountInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        amountInput.setTextColor(INK);
        amountInput.setHintTextColor(Color.rgb(145, 153, 165));
        amountInput.setHint("");
        amountInput.setInputType(8194);
        amountInput.setPadding(dp(14), dp(16), dp(14), dp(16));
        amountInput.setBackground(inputBorder(FIELD_BORDER));
        form.addView(amountInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(76)));

        TextView note = label("بعد التنفيذ ستفتح شاشة الاتصال بالكود جاهزًا. اضغط اتصال، ثم أكمل خطوات فودافون بنفسك.", 16, MUTED, false);
        note.setPadding(0, dp(14), 0, dp(16));
        form.addView(note);
        Button execute = button("تنفيذ التحويل", RED, Color.WHITE);
        form.addView(execute, matchWrap());
        execute.setOnClickListener(v -> prepareTransfer());

        TextView safe = label("لن نطلب الرقم السري أو نحفظه داخل التطبيق.", 16, GREEN, true);
        safe.setPadding(0, dp(18), 0, dp(8));
        page.addView(safe);
        setContentView(wrapScroll(redFrame(page)));
    }

    private void styleInlineIcon(Button button, String description) {
        button.setTextSize(29);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(0, 0, 0, 0);
        button.setContentDescription(description);
        button.setBackgroundColor(Color.TRANSPARENT);
    }

    private JSONArray readFavorites() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(FAVORITES_KEY, "[]");
        try {
            return new JSONArray(raw);
        } catch (Exception ex) {
            return new JSONArray();
        }
    }

    private void saveFavorites(JSONArray favorites) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(FAVORITES_KEY, favorites.toString()).apply();
    }

    private void showFavorites() {
        JSONArray favorites = readFavorites();
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(10), dp(18), dp(8));

        Button saveCurrent = button("＋ حفظ الرقم المكتوب في المفضلة", Color.rgb(35, 112, 75), Color.WHITE);
        content.addView(saveCurrent, matchWrap());
        saveCurrent.setOnClickListener(v -> {
            String current = phoneInput.getText().toString().trim();
            if (!current.matches("01[0125][0-9]{8}")) {
                Toast.makeText(this, "اكتب رقمًا مصريًا صحيحًا أولاً لحفظه.", Toast.LENGTH_LONG).show();
                return;
            }
            showSaveFavoriteDialog(current);
        });

        if (favorites.length() == 0) {
            TextView empty = label("لا توجد أرقام محفوظة حتى الآن.", 17, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(22), 0, dp(18));
            content.addView(empty);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("الأرقام المفضلة")
                .setView(content)
                .setNegativeButton("إغلاق", null)
                .create();

        for (int i = 0; i < favorites.length(); i++) {
            JSONObject item = favorites.optJSONObject(i);
            if (item == null) continue;
            final String number = item.optString("phone", "");
            final String name = item.optString("name", "رقم محفوظ");
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(8), 0, dp(8));
            TextView details = label(name + "\\n" + number, 17, INK, true);
            row.addView(details, new LinearLayout.LayoutParams(0, dp(62), 1f));
            Button choose = button("اختيار", Color.rgb(35, 112, 75), Color.WHITE);
            choose.setTextSize(14);
            choose.setMinHeight(dp(46));
            row.addView(choose, new LinearLayout.LayoutParams(dp(82), dp(48)));
            choose.setOnClickListener(v -> {
                phoneInput.setText(number);
                dialog.dismiss();
            });
            Button remove = button("×", Color.rgb(255, 230, 230), RED);
            remove.setTextSize(22);
            remove.setMinHeight(dp(46));
            LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(dp(46), dp(48));
            removeParams.leftMargin = dp(5);
            row.addView(remove, removeParams);
            remove.setOnClickListener(v -> {
                removeFavorite(number);
                dialog.dismiss();
                showFavorites();
            });
            content.addView(row);
            View line = new View(this);
            line.setBackgroundColor(Color.rgb(230, 230, 230));
            content.addView(line, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        }
        dialog.show();
    }

    private void showSaveFavoriteDialog(String number) {
        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setTextSize(18);
        nameInput.setHint("اسم صاحب الرقم (اختياري)");
        nameInput.setText("رقم " + number);
        nameInput.setSelectAllOnFocus(true);
        LinearLayout holder = new LinearLayout(this);
        holder.setPadding(dp(20), dp(8), dp(20), 0);
        holder.addView(nameInput, matchWrap());
        new AlertDialog.Builder(this)
                .setTitle("حفظ في المفضلة")
                .setMessage("يمكنك تعديل الاسم الذي سيظهر بجوار الرقم.")
                .setView(holder)
                .setNegativeButton("إلغاء", null)
                .setPositiveButton("حفظ", (d, which) -> {
                    JSONArray list = readFavorites();
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject item = list.optJSONObject(i);
                        if (item != null && number.equals(item.optString("phone"))) {
                            Toast.makeText(this, "هذا الرقم محفوظ بالفعل في المفضلة.", Toast.LENGTH_LONG).show();
                            showFavorites();
                            return;
                        }
                    }
                    try {
                        JSONObject item = new JSONObject();
                        String name = nameInput.getText().toString().trim();
                        item.put("name", name.isEmpty() ? "رقم " + number : name);
                        item.put("phone", number);
                        list.put(item);
                        saveFavorites(list);
                        Toast.makeText(this, "تم حفظ الرقم في المفضلة.", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(this, "تعذر حفظ الرقم.", Toast.LENGTH_LONG).show();
                    }
                    showFavorites();
                }).show();
    }

    private void removeFavorite(String number) {
        JSONArray oldList = readFavorites();
        JSONArray newList = new JSONArray();
        for (int i = 0; i < oldList.length(); i++) {
            JSONObject item = oldList.optJSONObject(i);
            if (item != null && !number.equals(item.optString("phone"))) newList.put(item);
        }
        saveFavorites(newList);
        Toast.makeText(this, "تم حذف الرقم من المفضلة.", Toast.LENGTH_SHORT).show();
    }

    private void prepareTransfer() {
        String phone = phoneInput.getText().toString().trim();
        String amount = amountInput.getText().toString().trim().replace(',', '.');
        if (!phone.matches("01[0125][0-9]{8}")) {
            phoneInput.setError("أدخل رقمًا مصريًا صحيحًا من 11 رقمًا");
            phoneInput.requestFocus();
            return;
        }
        if (!amount.matches("[0-9]+(\\.[0-9]{1,2})?")) {
            amountInput.setError("أدخل مبلغًا صحيحًا");
            amountInput.requestFocus();
            return;
        }
        try {
            double value = Double.parseDouble(amount);
            if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            amountInput.setError("يجب أن يكون المبلغ أكبر من صفر");
            amountInput.requestFocus();
            return;
        }
        openUssd("*9*7*" + phone + "*" + amount + "#");
    }

    private void chooseContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        try {
            startActivityForResult(intent, CONTACT_PICKER);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "لا يوجد تطبيق جهات اتصال متاح.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_PICKER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String number = cursor.getString(0);
                    if (number != null) {
                        String digits = number.replaceAll("[^0-9]", "");
                        if (digits.startsWith("0020")) digits = "0" + digits.substring(4);
                        else if (digits.startsWith("20") && digits.length() == 12) digits = "0" + digits.substring(2);
                        phoneInput.setText(digits);
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(this, "تعذر قراءة رقم جهة الاتصال المختارة.", Toast.LENGTH_LONG).show();
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }

    private void openUssd(String code) {
        Uri uri = Uri.fromParts("tel", code, null);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "لم يتم العثور على تطبيق اتصال على الهاتف.", Toast.LENGTH_LONG).show();
        }
    }

    private void addService(LinearLayout parent, String title, String description, String symbol,
                            int accent, View.OnClickListener action) {
        LinearLayout box = compactCard(parent);
        int fill = accent;
        box.setBackground(gradient(fill, accent == ROYAL_BLUE ? Color.rgb(48, 145, 255) : (accent == TEAL ? Color.rgb(16, 166, 173) : Color.rgb(65, 83, 119)), 20));
        box.setPadding(dp(13), dp(11), dp(13), dp(11));
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        box.addView(row, matchWrap());
        TextView icon = label(symbol, 34, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(round(Color.argb(48, 255, 255, 255), 16));
        row.addView(icon, new LinearLayout.LayoutParams(dp(58), dp(58)));
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(14), 0, 0, 0);
        row.addView(details, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        details.addView(label(title, 21, Color.WHITE, true));
        TextView desc = label(description, 15, Color.rgb(246, 246, 246), false);
        desc.setPadding(0, dp(5), 0, 0);
        details.addView(desc);
        GradientDrawable serviceBorder = gradient(fill, accent == ROYAL_BLUE ? Color.rgb(48, 145, 255) : (accent == TEAL ? Color.rgb(16, 166, 173) : Color.rgb(65, 83, 119)), 20);
        serviceBorder.setStroke(dp(1), Color.argb(90, 255, 255, 255));
        box.setBackground(serviceBorder);
        box.setOnClickListener(action);
        box.setFocusable(true);
        box.setClickable(true);
    }

    private LinearLayout basePage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        page.setPadding(dp(18), dp(18), dp(18), dp(26));
        page.setBackgroundColor(PAGE);
        return page;
    }

    private View redFrame(View child) {
        LinearLayout frame = new LinearLayout(this);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setPadding(dp(5), dp(5), dp(5), dp(5));
        frame.setBackground(round(RED, 22));
        frame.addView(child, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return frame;
    }

    private ScrollView wrapScroll(View child) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(PAGE);
        scroll.addView(child);
        return scroll;
    }

    private LinearLayout compactCard(LinearLayout parent) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(13), dp(11), dp(13), dp(11));
        box.setBackground(round(Color.WHITE, 18));
        box.setElevation(dp(2));
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(9);
        parent.addView(box, params);
        return box;
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        box.setBackground(round(Color.WHITE, 20));
        box.setElevation(dp(3));
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(14);
        parent.addView(box, params);
        return box;
    }

    private Button button(String title, int background, int foreground) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(18);
        b.setTextColor(foreground);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setMinHeight(dp(60));
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        b.setBackground(round(background, 16));
        return b;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return t;
    }

    private GradientDrawable inputBorder(int stroke) {
        GradientDrawable d = round(Color.WHITE, 15);
        d.setStroke(dp(1), stroke);
        return d;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable gradient(int start, int end, int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{start, end});
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int BLUE() {
        return Color.rgb(0, 112, 210);
    }
}
