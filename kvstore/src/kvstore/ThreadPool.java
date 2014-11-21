package kvstore;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ThreadPool {

    /* Array of threads in the threadpool */
    public Thread threads[];
    public LinkedList<Runnable> job_list;
    final Lock lock;
    final Condition hasElements;

    /**
     * Constructs a Threadpool with a certain number of threads.
     *
     * @param size number of threads in the thread pool
     */
    public ThreadPool(int size) {
        job_list = new LinkedList<Runnable>();
        lock = new ReentrantLock();
        hasElements = lock.newCondition();

        threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new WorkerThread(this);
            threads[i].start();
        }
        // implement me
    }

    /**
     * Add a job to the queue of jobs that have to be executed. As soon as a
     * thread is available, the thread will retrieve a job from this queue if
     * if one exists and start processing it.
     *
     * @param r job that has to be executed
     * @throws InterruptedException if thread is interrupted while in blocked
     *         state. Your implementation may or may not actually throw this.
     */
    public void addJob(Runnable r) throws InterruptedException {
        // implement me
        lock.lock();
        job_list.add(r);
        hasElements.signal();
        lock.unlock();
    }

    /**
     * Block until a job is present in the queue and retrieve the job
     * @return A runnable task that has to be executed
     * @throws InterruptedException if thread is interrupted while in blocked
     *         state. Your implementation may or may not actually throw this.
     */
    public Runnable getJob() throws InterruptedException {
        // implement me
        lock.lock();
        while (job_list.isEmpty()){
            hasElements.await();
        }
        Runnable job = job_list.removeFirst();
        lock.unlock();
        return job;
    }

    /**
     * A thread in the thread pool.
     */
    public class WorkerThread extends Thread {

        public ThreadPool threadPool;

        /**
         * Constructs a thread for this particular ThreadPool.
         *
         * @param pool the ThreadPool containing this thread
         */
        public WorkerThread(ThreadPool pool) {
            threadPool = pool;
        }

        /**
         * Scan for and execute tasks.
         */
        @Override
        public void run() {
           while(true) {
                try {
                    Runnable job = threadPool.getJob();
                    job.run();
                } catch (InterruptedException ite) {

                }
           }
            // implement me
        }
    }
}
