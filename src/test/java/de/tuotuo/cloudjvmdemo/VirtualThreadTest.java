package de.tuotuo.cloudjvmdemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadTest {
    public static void main(String[] args) {
        Thread.ofVirtual().start(() -> System.out.println("虚拟线程执行中..."));

        Thread.startVirtualThread(() -> System.out.println("虚拟线程执行中..."));

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            executorService.execute(() -> System.out.println("虚拟线程执行中..."));
        }
    }
}
