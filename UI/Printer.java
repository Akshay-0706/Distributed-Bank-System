package UI;

public class Printer {

    public static void boxPrinter(String text) {
        int length = text.length() + 2;
        System.out.print(Ascii.Top_left_corner());
        for (int i = 0; i < length; i++) {
            System.out.print(Ascii.horizontal_line());
        }
        System.out.println(Ascii.Top_right_corner());
        System.out.print(Ascii.vertical_line());
        for (int i = 0; i < length; i++) {
            System.out.print(" ");
        }
        System.out.println(Ascii.vertical_line());
        System.out.println(Ascii.vertical_line() + " " + text + " " + Ascii.vertical_line());
        System.out.print(Ascii.vertical_line());
        for (int i = 0; i < length; i++) {
            System.out.print(" ");
        }
        System.out.println(Ascii.vertical_line());
        System.out.print(Ascii.Bottom_left_corner());
        for (int i = 0; i < length; i++) {
            System.out.print(Ascii.horizontal_line());
        }
        System.out.println(Ascii.Bottom_right_corner());
    }
}
