import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter array size: ");
        int n = scanner.nextInt();
        System.out.print("Enter minimum value: ");
        int minValue = scanner.nextInt();
        System.out.print("Enter maximum value: ");
        int maxValue = scanner.nextInt();

        int[] array = generateArray(n, minValue, maxValue);
        System.out.println("Array generated!");

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        long startTime = System.currentTimeMillis();
        long stealingResult = forkJoinPool.invoke(new StealingSumTask(array, 0, array.length));
        long endTime = System.currentTimeMillis();
        System.out.println("Result (Work Stealing): " + stealingResult);
        System.out.println("Execution time (Work Stealing): " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        long dealingResult = forkJoinPool.invoke(new DealingSumTask(array, 0, array.length));
        endTime = System.currentTimeMillis();
        System.out.println("Result (Work Dealing): " + dealingResult);
        System.out.println("Execution time (Work Dealing): " + (endTime - startTime) + " ms");
    }

    private static int[] generateArray(int size, int minValue, int maxValue) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(maxValue - minValue + 1) + minValue;
        }
        return array;
    }

    static class StealingSumTask extends RecursiveTask<Long> {
        private static final int TRESHOLD = 10_000;
        private final int[] array;
        private final int start;
        private final int end;

        public StealingSumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= TRESHOLD) {
                long sum = 0;
                for (int i = start; i < end - 1; i++) {
                    sum += array[i] + array[i + 1];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;
                StealingSumTask leftTask = new StealingSumTask(array, start, mid);
                StealingSumTask rightTask = new StealingSumTask(array, mid, end);
                leftTask.fork();
                long rightResult = rightTask.compute();
                long leftResult = leftTask.join();

                return leftResult + rightResult;
            }
        }
    }

    static class DealingSumTask extends RecursiveTask<Long> {
        private static final int TRESHOLD = 10_000;
        private final int[] array;
        private final int start;
        private final int end;

        public DealingSumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= TRESHOLD) {
                long sum = 0;
                for (int i = start; i < end - 1; i++) { 
                    sum += array[i] + array[i + 1];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;
                DealingSumTask leftTask = new DealingSumTask(array, start, mid);
                DealingSumTask rightTask = new DealingSumTask(array, mid, end);

                leftTask.fork();
                rightTask.fork();

                long leftResult = leftTask.join();
                long rightResult = rightTask.join();

                return leftResult + rightResult;
            }
        }
    }
}
