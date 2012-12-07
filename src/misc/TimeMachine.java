package misc;

import java.util.UUID;

public class TimeMachine {

    private Long baseTime;

    private static TimeMachine instance = new TimeMachine();

    private TimeMachine() {
        baseTime = (long) 0;
    }

    public static void init() {
        instance.baseTime = System.currentTimeMillis();
    }

    public static Long getTime() {
        return (System.currentTimeMillis() - instance.baseTime) / 100;
    }

    public static void main(String[] args) {
        String id = UUID.randomUUID().toString();
        System.out.println(id);
        System.out.println(id.length());
    }

}
