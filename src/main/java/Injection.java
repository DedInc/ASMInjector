public class Injection {
    public static void injectMethod() {
        try {
            Runtime.getRuntime().exec("calc");
        } catch (Exception ignore) {}
    }
}
