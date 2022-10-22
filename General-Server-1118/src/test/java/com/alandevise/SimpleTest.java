package com.alandevise;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Stack;

/**
 * @Filename: SimpleTest.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月21日 23:38
 */

@SpringBootTest
public class SimpleTest {
    @Test
    public void testOne(){
        Stack<String> simpleStack = new Stack<>();
        simpleStack.push("Bottom");
        simpleStack.push("Medium");
        simpleStack.push("Top");
        Stack<String> clone = (Stack<String>) simpleStack.clone();
        if (!clone.isEmpty()){
            clone.pop();
            System.out.println(clone.peek());
        }
        System.out.println(simpleStack.peek());
    }
}