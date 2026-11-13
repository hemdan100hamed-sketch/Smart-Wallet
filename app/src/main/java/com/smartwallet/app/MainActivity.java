package com.smartwallet.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int RED = Color.rgb(210, 0, 30);
    private static final int DARK_RED = Color.rgb(145, 0, 25);
    private static final int GREEN = Color.rgb(22, 163, 74);
    private static final int INK = Color.rgb(28, 37, 51);
    private static final int MUTED = Color.rgb(91, 103, 120);
    private static final int PAGE = Color.rgb(247, 249, 252);
    private static final int CONTACT_PICKER = 410;

    private EditText phoneInput;
    private EditText amountInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(DARK_RED);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        showHome();
    }

    private void showHome() {
        LinearLayout page = basePage();
        page.setPadding(dp(16), dp(10), dp(16), dp(8));

        TextView section = label("خدمات فودافون كاش", 21, INK, true);
        section.setPadding(0, dp(2), 0, dp(10));
        page.addView(section);

        // الأهم أولاً: التحويل ثم الاستعلام عن الرصيد.
        addService(page, "تحويل الأموال", "إرسال الأموال إلى رقم محمول أو جهة اتصال", "⇄", RED, v -> showTransfer());
        addService(page, "الاستعلام عن الرصيد", "معرفة رصيد محفظتك عبر خدمة فودافون الرسمية", "▣", BLUE(), v -> openUssd("*9*13#"));
        addService(page, "خدمات أخرى", "ستتوفر خدمات إضافية في تحديثات قادمة", "＋", Color.rgb(110, 75, 190), v ->
                Toast.makeText(this, "ستتوفر هذه الخدمة في تحديث قادم.", Toast.LENGTH_LONG).show());

        LinearLayout safety = compactCard(page);
        TextView safeText = label("الأمان: لا يطلب التطبيق الرقم السري ولا يحفظه. أكمل التأكيد داخل واجهة فودافون الرسمية.", 14, MUTED, false);
        safety.addView(safeText);
        TextView version = label("SMART WALLET  •  3.0 TEST", 12, MUTED, true);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, dp(2), 0, 0);
        page.addView(version);
        // الصفحة الرئيسية ثابتة بلا تمرير؛ كل الخدمات ظاهرة مباشرة.
        setContentView(page);
    }

    private void showTransfer() {
        LinearLayout page = basePage();
        Button back = button("رجوع إلى الرئيسية", Color.rgb(235, 238, 243), INK);
        back.setOnClickListener(v -> showHome());
        page.addView(back, matchWrap());

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(20), dp(20), dp(22));
        header.setBackground(gradient(DARK_RED, RED, 24));
        LinearLayout.LayoutParams hp = matchWrap();
        hp.topMargin = dp(14);
        page.addView(header, hp);
        header.addView(label("تحويل أموال", 28, Color.WHITE, true));
        TextView hint = label("إلى أي رقم محمول داخل الشبكة", 18, Color.WHITE, false);
        hint.setPadding(0, dp(7), 0, 0);
        header.addView(hint);

        LinearLayout form = card(page);
        TextView phoneLabel = label("رقم هاتف المستلم", 21, INK, true);
        phoneLabel.setPadding(0, 0, 0, dp(10));
        form.addView(phoneLabel);

        LinearLayout phoneRow = new LinearLayout(this);
        phoneRow.setOrientation(LinearLayout.HORIZONTAL);
        phoneRow.setGravity(Gravity.CENTER_VERTICAL);
        form.addView(phoneRow, matchWrap());

        phoneInput = new EditText(this);
        phoneInput.setSingleLine(true);
        phoneInput.setTextSize(21);
        phoneInput.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        phoneInput.setHorizontallyScrolling(true);
        phoneInput.setMinWidth(0);
        phoneInput.setMinimumWidth(0);
        phoneInput.setMaxLines(1);
        phoneInput.setSelectAllOnFocus(false);
        phoneInput.setTextColor(INK);
        phoneInput.setHintTextColor(Color.rgb(145, 153, 165));
        phoneInput.setHint("01xxxxxxxxx");
        phoneInput.setInputType(3);
        phoneInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        phoneInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        phoneInput.setPadding(dp(14), dp(16), dp(14), dp(16));
        phoneInput.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        phoneInput.setBackground(inputBorder(RED));
        LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(0, dp(68), 1f);
        phoneParams.rightMargin = dp(8);
        phoneRow.addView(phoneInput, phoneParams);

        // زر جهات اتصال مدمج حتى لا يقتطع مساحة رقم الهاتف.
        Button contacts = button("👤", Color.rgb(225, 239, 255), Color.rgb(0, 105, 200));
        contacts.setTextSize(23);
        contacts.setMinHeight(0);
        contacts.setMinimumHeight(0);
        contacts.setPadding(0, 0, 0, 0);
        contacts.setContentDescription("اختيار رقم من جهات الاتصال");
        phoneRow.addView(contacts, new LinearLayout.LayoutParams(dp(56), dp(68)));
        contacts.setOnClickListener(v -> chooseContact());

        TextView validation = label("أدخل رقمًا مصريًا من 11 رقمًا", 16, MUTED, false);
        validation.setPadding(0, dp(9), 0, dp(16));
        form.addView(validation);
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString().trim();
                boolean valid = value.matches("01[0125][0-9]{8}");
                phoneInput.setBackground(inputBorder(valid ? GREEN : RED));
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
        amountInput.setTextColor(INK);
        amountInput.setHintTextColor(Color.rgb(145, 153, 165));
        amountInput.setHint("أدخل المبلغ");
        amountInput.setInputType(8194);
        amountInput.setPadding(dp(14), dp(16), dp(14), dp(16));
        amountInput.setBackground(inputBorder(Color.rgb(190, 198, 208)));
        form.addView(amountInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));

        TextView note = label("بعد التنفيذ ستفتح شاشة الاتصال بالكود جاهزًا. اضغط اتصال، ثم أكمل خطوات فودافون بنفسك.", 16, MUTED, false);
        note.setPadding(0, dp(14), 0, dp(16));
        form.addView(note);
        Button execute = button("تنفيذ التحويل", RED, Color.WHITE);
        form.addView(execute, matchWrap());
        execute.setOnClickListener(v -> prepareTransfer());

        TextView safe = label("لن نطلب الرقم السري أو نحفظه داخل التطبيق.", 16, GREEN, true);
        safe.setPadding(0, dp(18), 0, dp(8));
        page.addView(safe);
        setContentView(wrapScroll(page));
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
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        box.addView(row, matchWrap());
        TextView icon = label(symbol, 34, accent, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(round(Color.rgb(247, 239, 242), 18));
        row.addView(icon, new LinearLayout.LayoutParams(dp(58), dp(58)));
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(14), 0, 0, 0);
        row.addView(details, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        details.addView(label(title, 21, INK, true));
        TextView desc = label(description, 15, MUTED, false);
        desc.setPadding(0, dp(5), 0, 0);
        details.addView(desc);
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
        d.setStroke(dp(2), stroke);
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
