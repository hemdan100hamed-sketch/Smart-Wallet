package com.smartwallet.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int RED = Color.rgb(200, 16, 46);
    private static final int DARK_RED = Color.rgb(143, 11, 33);
    private static final int INK = Color.rgb(35, 39, 47);
    private static final int MUTED = Color.rgb(112, 119, 130);
    private static final int PAGE = Color.rgb(247, 248, 250);

    private EditText phoneInput;
    private EditText amountInput;
    private Button showRecordButton;
    private String lastPhone;
    private String lastAmount;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(DARK_RED);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        buildScreen();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(PAGE);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(22), dp(22), dp(22), dp(24));
        header.setBackground(roundGradient(DARK_RED, RED, 24));
        root.addView(header, matchWrap());

        TextView brand = text("SMART WALLET", 13, Color.WHITE, true);
        brand.setLetterSpacing(0.08f);
        header.addView(brand);

        TextView title = text("محفظتك في متناول إيدك", 25, Color.WHITE, true);
        title.setPadding(0, dp(12), 0, dp(5));
        header.addView(title);
        header.addView(text("اختصارات سهلة لخدمات فودافون كاش", 14,
                Color.rgb(255, 224, 230), false));

        TextView section = text("الخدمات", 19, INK, true);
        section.setPadding(0, dp(24), 0, dp(12));
        root.addView(section);

        LinearLayout serviceCard = card(root);
        serviceCard.addView(text("تحويل الأموال", 20, INK, true));
        TextView serviceHint = text("فودافون كاش • افتح الخدمة الرسمية بنفسك", 13, MUTED, false);
        serviceHint.setPadding(0, dp(5), 0, dp(18));
        serviceCard.addView(serviceHint);

        addFieldLabel(serviceCard, "رقم هاتف المستلم");
        phoneInput = editText("مثال: 01012345678", android.text.InputType.TYPE_CLASS_PHONE);
        serviceCard.addView(phoneInput, matchWrap());

        addFieldLabel(serviceCard, "المبلغ بالجنيه المصري");
        amountInput = editText("مثال: 150",
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        serviceCard.addView(amountInput, matchWrap());

        TextView note = text("بعد الضغط على تنفيذ ستفتح شاشة الاتصال وكود التحويل مكتوبًا. اضغط اتصال بنفسك وأكمل خطوات الخدمة الرسمية.",
                12, MUTED, false);
        note.setPadding(0, dp(14), 0, dp(16));
        serviceCard.addView(note);

        Button transfer = button("تنفيذ التحويل");
        serviceCard.addView(transfer, matchWrap());
        transfer.setOnClickListener(v -> prepareTransfer());

        showRecordButton = button("عرض سجل العملية");
        showRecordButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams recordParams = matchWrap();
        recordParams.topMargin = dp(12);
        serviceCard.addView(showRecordButton, recordParams);
        showRecordButton.setOnClickListener(v -> askForManualConfirmation());

        TextView safetyTitle = text("أمانك أولًا", 16, INK, true);
        safetyTitle.setPadding(0, dp(20), 0, dp(8));
        root.addView(safetyTitle);
        root.addView(text("لن نطلب الرقم السري ولن نحفظه. راجع بيانات المستلم والمبلغ داخل الخدمة الرسمية قبل تأكيد أي تحويل.",
                13, MUTED, false));

        TextView footer = text("SMART WALLET  •  الإصدار 3.9", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(28), 0, 0);
        root.addView(footer);

        setContentView(scroll);
    }

    private void prepareTransfer() {
        String phone = phoneInput.getText().toString().trim();
        String amount = amountInput.getText().toString().trim().replace(',', '.');

        if (!phone.matches("01[0125][0-9]{8}")) {
            phoneInput.setError("أدخل رقمًا مصريًا صحيحًا مكوّنًا من 11 رقمًا");
            phoneInput.requestFocus();
            return;
        }
        if (!amount.matches("[0-9]+(\\.[0-9]{1,2})?")) {
            amountInput.setError("أدخل مبلغًا صحيحًا، ويمكن استخدام منزلتين عشريتين");
            amountInput.requestFocus();
            return;
        }
        try {
            double value = Double.parseDouble(amount);
            if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            amountInput.setError("أدخل مبلغًا أكبر من صفر");
            amountInput.requestFocus();
            return;
        }

        lastPhone = phone;
        lastAmount = amount;
        showRecordButton.setVisibility(View.VISIBLE);
        String ussdCode = "*9*7*" + phone + "*" + amount + "#";
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", ussdCode, null));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "لم يتم العثور على تطبيق اتصال على الهاتف.", Toast.LENGTH_LONG).show();
        }
    }

    private void askForManualConfirmation() {
        if (lastPhone == null || lastAmount == null) return;
        new AlertDialog.Builder(this)
                .setTitle("تأكيد يدوي")
                .setMessage("هل شاهدت رسالة نجاح التحويل داخل خدمة المحفظة الرسمية؟ سيُنشأ سجل مشاركة محلي بناءً على تأكيدك.")
                .setNegativeButton("رجوع", null)
                .setPositiveButton("نعم، عرض السجل", (dialog, which) -> showLocalRecord())
                .show();
    }

    private void showLocalRecord() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(PAGE);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(18), dp(24), dp(18), dp(24));
        scroll.addView(page);

        LinearLayout receipt = new LinearLayout(this);
        receipt.setOrientation(LinearLayout.VERTICAL);
        receipt.setGravity(Gravity.CENTER_HORIZONTAL);
        receipt.setPadding(dp(22), dp(26), dp(22), dp(24));
        receipt.setBackground(round(Color.WHITE, 22));
        receipt.setElevation(dp(2));
        page.addView(receipt, matchWrap());

        TextView localLabel = text("SMART WALLET  •  سجل عملية مُنشأ محليًا", 12, RED, true);
        localLabel.setGravity(Gravity.CENTER);
        receipt.addView(localLabel);

        TextView walletIcon = text("▰", 48, DARK_RED, true);
        walletIcon.setGravity(Gravity.CENTER);
        walletIcon.setPadding(0, dp(14), 0, 0);
        receipt.addView(walletIcon);

        TextView check = text("✓", 34, Color.WHITE, true);
        check.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(RED);
        check.setBackground(circle);
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(dp(62), dp(62));
        checkParams.topMargin = dp(-8);
        receipt.addView(check, checkParams);

        TextView done = text("تم", 30, INK, true);
        done.setGravity(Gravity.CENTER);
        done.setPadding(0, dp(22), 0, dp(14));
        receipt.addView(done);

        TextView amountLine = text("تم تحويل " + formatAmount(lastAmount) + " جنيه إلى", 23, INK, false);
        amountLine.setGravity(Gravity.CENTER);
        receipt.addView(amountLine);

        TextView phone = text(lastPhone, 24, INK, true);
        phone.setGravity(Gravity.CENTER);
        phone.setPadding(0, dp(18), 0, dp(18));
        phone.setTextDirection(View.TEXT_DIRECTION_LTR);
        receipt.addView(phone);

        TextView provenance = text("هذا سجل مشاركة أنشأه Smart Wallet بناءً على تأكيد المستخدم، وليس إيصالًا صادرًا من شركة المحفظة.",
                12, MUTED, false);
        provenance.setGravity(Gravity.CENTER);
        provenance.setPadding(dp(4), dp(10), dp(4), 0);
        receipt.addView(provenance);

        Button share = button("مشاركة");
        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        shareParams.topMargin = dp(18);
        page.addView(share, shareParams);
        share.setOnClickListener(v -> shareReceiptImage(receipt));

        setContentView(scroll);
    }

    private void shareReceiptImage(View receipt) {
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
            File imageFile = new File(dir, "smart_wallet_record.png");
            FileOutputStream output = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
            output.close();
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

    private String formatAmount(String raw) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat format = new DecimalFormat("#,##0.##", symbols);
            return format.format(Double.parseDouble(raw));
        } catch (Exception ex) {
            return raw;
        }
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        box.setBackground(round(Color.WHITE, 20));
        box.setElevation(dp(2));
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(4);
        parent.addView(box, params);
        return box;
    }

    private void addFieldLabel(LinearLayout parent, String value) {
        TextView label = text(value, 14, INK, true);
        label.setPadding(0, dp(4), 0, dp(8));
        parent.addView(label);
    }

    private EditText editText(String hint, int inputType) {
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setTextSize(16);
        edit.setTextColor(INK);
        edit.setHintTextColor(Color.rgb(155, 160, 168));
        edit.setHint(hint);
        edit.setInputType(inputType);
        edit.setPadding(dp(14), dp(12), dp(14), dp(12));
        edit.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        edit.setTextDirection(View.TEXT_DIRECTION_LTR);
        edit.setBackground(round(Color.rgb(247, 248, 250), 12));
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(14);
        edit.setLayoutParams(params);
        return edit;
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
