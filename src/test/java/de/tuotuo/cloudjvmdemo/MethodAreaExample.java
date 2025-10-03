package de.tuotuo.cloudjvmdemo;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

public class MethodAreaExample {

    public static void main(String[] args) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        MemoryPoolMXBean metaspaceMemoryPoolBean = null;

        for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if ("Metaspace".equals(memoryPoolMXBean.getName())) {
                metaspaceMemoryPoolBean = memoryPoolMXBean;
                break;
            }
        }

        System.out.println("方法区（元空间）信息：");
        System.out.println("方法区（元空间）初始大小: " + nonHeapMemoryUsage.getInit() / 1024 + "KB");
        System.out.println("方法区（元空间）最大大小: " + (nonHeapMemoryUsage.getMax() == -1 ? "无限制"
            : nonHeapMemoryUsage.getMax() / 1024 + "KB"));
        System.out.println("方法区（元空间）已用大小: " + nonHeapMemoryUsage.getUsed() / 1024 + "KB");
        if (metaspaceMemoryPoolBean != null) {
            System.out.println("\nMetaspace 信息：");
            MemoryUsage metaspaceMemoryUsage = metaspaceMemoryPoolBean.getUsage();
            System.out.println("Metaspace 初始大小: " + metaspaceMemoryUsage.getInit() / 1024 + "KB");
            System.out.println("Metaspace 最大大小: " + (metaspaceMemoryUsage.getMax() == -1 ? "无限制"
                : metaspaceMemoryUsage.getMax() / 1024 + "KB"));
            System.out.println("Metaspace 已用大小: " + metaspaceMemoryUsage.getUsed() / 1024 + "KB");
        }
    }
}
