package de.tuotuo.cloudjvmdemo;

public class ThreadStateDemo implements Runnable {
    public void run() {
        System.out.println("当前线程：" + Thread.currentThread().getName() + " 运行");
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("当前线程：" + Thread.currentThread().getName() + " 终止");
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadStateDemo tsd = new ThreadStateDemo();
        Thread t1 = new Thread(tsd, "线程1");
        Thread t2 = new Thread(tsd, "线程2");
        Thread t3 = new Thread(tsd, "线程3");
        Thread t4 = new Thread(tsd, "线程4");
        Thread t5 = new Thread(tsd, "线程5");

        System.out.println("新建线程后，线程1状态：" + t1.getState());
        t1.start();
        t2.start();
        t3.start();
        System.out.println("调用start()后，线程1状态：" + t1.getState());
        Thread.sleep(2000);
        System.out.println("等待2秒后，线程1状态：" + t1.getState());

        synchronized (tsd){
            tsd.notify();
        }
        Thread.sleep(2000);
        System.out.println("等待2秒后，线程1状态：" + t1.getState());

        t4.start();
        t5.start();
        Thread.sleep(2000);
        System.out.println("等待2秒后，线程1状态：" + t1.getState());

        synchronized (tsd){
            tsd.notifyAll();
        }
        Thread.sleep(2000);
        System.out.println("等待2秒后，线程1状态：" + t1.getState());

    }
}
