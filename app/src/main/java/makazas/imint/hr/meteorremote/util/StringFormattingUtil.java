package makazas.imint.hr.meteorremote.util;

public class StringFormattingUtil {
    public static String attachOrdinalSuffix(int number){
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (number % 100) {
            case 11:
            case 12:
            case 13:
                return number + "th";
            default:
                return number + sufixes[number % 10];

        }
    }
}
