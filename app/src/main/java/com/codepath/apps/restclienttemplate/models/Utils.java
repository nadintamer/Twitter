package com.codepath.apps.restclienttemplate.models;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

public class Utils {
    // helper method to format text that is partially bold
    public static SpannableString formatPartiallyBoldText(String boldText, String normalText) {
        SpannableString str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return str;
    }

    // helper method to format a number like Twitter does (e.g. 81562 -> 81.5K)
    public static String formatNumber(int num) {
        if (num < 1000) {
            return String.valueOf(num);
        } else if (num < 10000) {
            String thousands = String.valueOf(num / 1000);
            String rest = String.valueOf(num % 1000);
            if (rest.length() < 3) {
                rest = String.format("0%s", rest);
            }
            return String.format("%s.%s", thousands, rest);
        } else if (num < 1000000) {
            String thousands = String.valueOf(num / 1000);
            String hundreds = String.valueOf((num % 1000) / 100);
            return String.format("%s,%sK", thousands, hundreds);
        } else {
            String millions = String.valueOf(num / 1000000);
            String thousands = String.valueOf((num % 1000000) / 100000);
            return String.format("%s,%sM", millions, thousands);
        }
    }
}
