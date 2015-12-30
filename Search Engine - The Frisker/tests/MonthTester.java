
public class MonthTester {

    public static void main(String[] args) {

        Month month = Month.NOV;

        switch (month) {
            case JAN:
            case FEB:
            case MAR:
            case APR:
            case MAY:
                System.out.println("Spring Semester");
                break;
            case JUN:
            case JUL:
                System.out.println("Summer Semester");
                break;
            case AUG:
            case SEP:
            case OCT:
            case NOV:
            case DEC:
                System.out.println("Fall Semester");
        }

        Month[] months = Month.values();

        for (Month m : months) {
            System.out.println(m.name() + " " + m.ordinal());
        }
    }
}
