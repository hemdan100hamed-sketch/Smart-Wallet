package com.smartwallet.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int RED = Color.rgb(200, 16, 46);
    private static final int DARK_RED = Color.rgb(143, 11, 33);
    private static final int INK = Color.rgb(35, 39, 47);
    private static final int MUTED = Color.rgb(112, 119, 130);
    private static final int PAGE = Color.rgb(247, 248, 250);

    private EditText phoneInput;
    private EditText amountInput;

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

        TextView brand = text("SMART WALLET", 13, Color.WHITE, true);
        brand.setLetterSpacing(0.08f);
        header.addView(brand);

        TextView title = text("محفظتك في متناول إيدك", 25, Color.WHITE, true);
        title.setPadding(0, dp(12), 0, dp(5));
        header.addView(title);

        TextView subtitle = text("اختصارات سهلة لخدمات فودافون كاش", 14,
                Color.rgb(255, 224, 230), false);
        header.addView(subtitle);

        TextView section = text("الخدمات", 19, INK, true);
        section.setPadding(0, dp(24), 0, dp(12));
        page.addView(section);

        LinearLayout serviceCard = card(page);
        TextView serviceTitle = text("تحويل الأموال", 20, INK, true);
        serviceCard.addView(serviceTitle);

        TextView serviceHint = text("فودافون كاش • الخدمة الرسمية", 13, MUTED, false);
        serviceHint.setPadding(0, dp(5), 0, dp(18));
        serviceCard.addView(serviceHint);

        addFieldLabel(serviceCard, "رقم هاتف المستلم");
        phoneInput = editText("مثال: 01012345678", InputType.TYPE_CLASS_PHONE);
        serviceCard.addView(phoneInput, matchWrap());

        addFieldLabel(serviceCard, "المبلغ بالجنيه المصري");
        amountInput = editText("مثال: 150", InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams amountParams = matchWrap();
        amountParams.topMargin = dp(2);
        serviceCard.addView(amountInput, amountParams);

        TextView note = text(
                "تنبيه: أدخل الرقم والمبلغ مرة أخرى داخل قائمة فودافون بعد فتح الكود. "
                + "لا يمكن للتطبيق تعبئة خطوات USSD تلقائيًا بصورة موثوقة.",
                12, MUTED, false);
        note.setPadding(0, dp(14), 0, dp(16));
        serviceCard.addView(note);

        Button transfer = button("متابعة إلى فودافون كاش");
        serviceCard.addView(transfer, matchWrap());
        transfer.setOnClickListener(v -> prepareTransfer());

        TextView safetyTitle = text("أمانك أولًا", 16, INK, true);
        safetyTitle.setPadding(0, dp(20), 0, dp(8));
        page.addView(safetyTitle);

        TextView safety = text(
                "لن نطلب الرقم السري ولن نحفظه. راجع اسم المستلم والمبلغ داخل قائمة فودافون "
                + "قبل تأكيد أي تحويل. اختيار الشريحة يتم من شاشة الاتصال إذا كان هاتفك يدعم ذلك.",
                13, MUTED, false);
        page.addView(safety);

        TextView footer = text("SMART WALLET  •  الإصدار 1.0", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(28), 0, 0);
        page.addView(footer);

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

        try {
            double value = Double.parseDouble(amount);
            if ((Double.isNaN(value) || Double.isInfinite(value)) || value <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            amountInput.setError("أدخل مبلغًا صحيحًا أكبر من صفر");
            amountInput.requestFocus();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("مراجعة بيانات التحويل")
                .setMessage("رقم المستلم: " + phone
                        + "\nالمبلغ: " + amount + " جنيه"
                        + "\n\nسنفتح كود فودافون كاش الرسمي *9*7#."
                        + "\nاختر شريحة فودافون من شاشة الاتصال، ثم أدخل رقم المستلم والمبلغ "
                        + "والرقم السري بنفسك داخل قائمة فودافون."
                        + "\n\nلن يتم تنفيذ التحويل إلا بعد تأكيدك لدى فودافون.")
                .setNegativeButton("مراجعة البيانات", null)
                .setPositiveButton("فتح كود الخدمة", (dialog, which) -> openVodafoneCode())
                .show();
    }

    private void openVodafoneCode() {
        // ACTION_DIAL only opens the dialer; the user must press Call and complete USSD.
        Uri ussdUri = Uri.parse("tel:*9*7%23");
        Intent intent = new Intent(Intent.ACTION_DIAL, ussdUri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "لم يتم العثور على تطبيق اتصال على الهاتف.",
                    Toast.LENGTH_LONG).show();
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
