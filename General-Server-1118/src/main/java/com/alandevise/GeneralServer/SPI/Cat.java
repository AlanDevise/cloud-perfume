package com.alandevise.GeneralServer.SPI;

/**
 * @Filename: Cat.java
 * @Package: com.alandevise.GeneralServer.SPI
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2024年07月16日 14:36
 */

public class Cat implements IShout {

    @Override
    public void shout() {
        System.out.println("Meow");
    }
}
