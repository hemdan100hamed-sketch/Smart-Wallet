package com.smartwallet.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int RED = Color.rgb(210, 0, 30);
    private static final int DARK_RED = Color.rgb(145, 0, 25);
    private static final int GREEN = Color.rgb(22, 163, 74);
    private static final int INK = Color.rgb(28, 37, 51);
    private static final int MUTED = Color.rgb(91, 103, 120);
    private static final int PAGE = Color.rgb(250, 244, 233);
    private static final int CONTACT_PICKER = 410;
    private static final String PREFS = "smart_wallet_preferences";
    private static final String FAVORITES_KEY = "favorite_recipients";

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

        // ترتيب الخدمات: الاستعلام عن الرصيد أولاً ثم تحويل الأموال.
        addService(page, "الاستعلام عن الرصيد", "معرفة رصيد محفظتك عبر خدمة فودافون الرسمية", "▣", BLUE(), v -> openUssd("*9*13#"));
        addService(page, "تحويل الأموال", "إرسال الأموال إلى رقم محمول أو جهة اتصال", "⇄", RED, v -> showTransfer());
        addService(page, "خدمات أخرى", "ستتوفر خدمات إضافية في تحديثات قادمة", "＋", Color.rgb(110, 75, 190), v ->
                Toast.makeText(this, "ستتوفر هذه الخدمة في تحديث قادم.", Toast.LENGTH_LONG).show());

        LinearLayout safety = compactCard(page);
        safety.setBackground(round(Color.rgb(255, 249, 225), 16));
        TextView safeText = label("الأمان: لا يطلب التطبيق الرقم السري ولا يحفظه. أكمل التأكيد داخل واجهة فودافون الرسمية.", 14, INK, false);
        safety.addView(safeText);
        TextView version = label("SMART WALLET  •  3.2 TEST", 12, MUTED, true);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, dp(2), 0, 0);
        page.addView(version);
        // الصفحة الرئيسية ثابتة بلا تمرير؛ كل الخدمات ظاهرة مباشرة.
        LinearLayout frame = new LinearLayout(this);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setPadding(dp(5), dp(5), dp(5), dp(5));
        frame.setBackground(round(DARK_RED, 26));
        page.setBackground(round(PAGE, 22));
        frame.addView(page, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(frame);
    }

    private void showTransfer() {
        LinearLayout page = basePage();
        Button back = button("رجوع إلى الرئيسية", Color.rgb(235, 238, 243), INK);
        back.setOnClickListener(v -> showHome());
        page.addView(back, matchWrap());

        LinearLayout form = card(page);
        form.setPadding(dp(8), dp(12), dp(8), dp(12));
        LinearLayout.LayoutParams formParams = (LinearLayout.LayoutParams) form.getLayoutParams();
        formParams.topMargin = dp(10);
        form.setLayoutParams(formParams);
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
        phoneInput.setHint("");
        phoneInput.setInputType(3);
        phoneInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        phoneInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        phoneInput.setPadding(dp(10), dp(10), dp(8), dp(10));
        phoneInput.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        phoneInput.setBackgroundColor(Color.TRANSPARENT);
        phoneRow.setPadding(dp(5), dp(3), dp(5), dp(3));
        phoneRow.setBackground(inputBorder(RED));
        LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        phoneParams.rightMargin = dp(3);
        phoneRow.addView(phoneInput, phoneParams);

        // الأيقونتان داخل نفس إطار حقل الرقم، مع مساحة كافية لعرض الرقم كاملًا.
        Button contacts = button("👤", Color.rgb(225, 239, 255), Color.rgb(0, 105, 200));
        styleInlineIcon(contacts, "اختيار رقم من جهات الاتصال");
        phoneRow.addView(contacts, new LinearLayout.LayoutParams(dp(46), dp(58)));
        contacts.setOnClickListener(v -> chooseContact());

        Button favorites = button("★", Color.rgb(255, 242, 205), Color.rgb(150, 105, 0));
        styleInlineIcon(favorites, "الأرقام المفضلة");
        LinearLayout.LayoutParams favoriteParams = new LinearLayout.LayoutParams(dp(46), dp(58));
        favoriteParams.leftMargin = dp(3);
        phoneRow.addView(favorites, favoriteParams);
        favorites.setOnClickListener(v -> showFavorites());

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
        amountInput.setHint("");
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

    private void styleInlineIcon(Button button, String description) {
        button.setTextSize(21);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(0, 0, 0, 0);
        button.setContentDescription(description);
        button.setBackground(round(Color.rgb(235, 241, 248), 12));
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
        int fill;
        if (accent == RED) fill = Color.rgb(35, 112, 75);                 // أخضر زمردي للتحويل
        else if (accent == BLUE()) fill = Color.rgb(20, 48, 105);         // أزرق غامق للاستعلام
        else fill = Color.rgb(77, 91, 135);                               // أزرق هادئ للخدمات الأخرى
        box.setBackground(round(fill, 18));
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
        GradientDrawable serviceBorder = round(fill, 18);
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
