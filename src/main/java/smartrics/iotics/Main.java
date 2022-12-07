package smartrics.iotics;

import smartrics.iotics.space.HttpServiceRegistry;
import smartrics.iotics.space.IoticSpace;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServiceRegistry sr = new HttpServiceRegistry("demo.iotics.space");
        IoticSpace ioticSpace = new IoticSpace(sr);
        ioticSpace.initialise();
        System.out.println(ioticSpace);
    }
}
