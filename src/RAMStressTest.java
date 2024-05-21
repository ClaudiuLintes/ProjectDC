import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RAMStressTest implements Runnable {
    private final long totalDuration;

    private static boolean benchmark;

    public RAMStressTest(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-stress"))
        {
            long testDuration = 60000000; // 600000 seconds
            RAMStressTest stressTest = new RAMStressTest(testDuration);
            Thread stressTestThread = new Thread(stressTest);
            stressTestThread.start();
            benchmark = false;
        }
        else
        {
            benchmark = true;
            // Retrieve RAM information
            String ramSize = getRAMSize();
            String os = System.getProperty("os.name").toLowerCase();
            String ramSpeed = getRAMSpeed(os);

            //System.out.println("RAM Size: " + ramSize);

            //System.out.println("RAM Speed: " + ramSpeed);

            // Stress test: Allocate memory and perform operations
            long testDuration = 60000; // 60 seconds
            RAMStressTest stressTest = new RAMStressTest(testDuration);
            Thread stressTestThread = new Thread(stressTest);
            stressTestThread.start();

            try {
                stressTestThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static String getRAMSize() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return getRAMSizeWindows();
        } else if (os.contains("mac")) {
            return getRAMSizeMac();
        } else {
            return "Unsupported OS";
        }
    }

    private static String getRAMSizeWindows() {
        try {
            ProcessBuilder builder = new ProcessBuilder("wmic", "memorychip", "get", "capacity");
            builder.redirectErrorStream(true);

            Process process = builder.start();

            // Read the output of the process
            String output = new String(process.getInputStream().readAllBytes());
            String[] lines = output.split(System.lineSeparator());

            if (lines.length >= 2) {
                String capacityLine = lines[1];
                long capacityBytes = Long.parseLong(capacityLine.trim());

                // Bytes to gigabytes
                long capacityGB = capacityBytes / (1024 * 1024 * 1024);

                return "Memory Capacity: " + capacityGB + " GB";
            } else {
                return "Failed to retrieve memory capacity.";
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private static String getRAMSizeMac() {
        try {
            // Read the output of the process
            Process process = Runtime.getRuntime().exec("sysctl -n hw.memsize");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            long memSizeBytes = Long.parseLong(reader.readLine());
            reader.close();
            // Bytes to gigabytes
            long memSizeGB = memSizeBytes / (1024 * 1024 * 1024);
            return memSizeGB + " GB";
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return "Failed to retrieve RAM size";
        }
    }

    public static String getRAMSpeed(String os) {
        if (os.contains("win")) {
            return getMemorySpeedWindows();
        } else if (os.contains("mac")) {
            return getRAMTypeMac();
        } else {
            return "Unsupported OS";
        }
    }

    public static String getRAMTypeMac() {
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("system_profiler SPMemoryDataType");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("Type:")) {
                    result.append(line.split(":")[1].trim()).append("\n");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to retrieve RAM type information";
        }
        return result.toString();
    }

    public static String getMemorySpeedWindows() {
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("wmic MEMORYCHIP get Speed");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // ignore empty lines and headers
                if (!line.trim().isEmpty() && !line.contains("Speed")) {
                    // Trim any spaces
                    result.append(line.trim()).append("MHz  ");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to retrieve memory speed information";
        }
        return result.toString();
    }

    @Override
    public void run() {
        //System.out.println("Starting RAM stress test...");

        long startTime = System.currentTimeMillis();
        int totalAlgorithmIterations = 0;
        double lastPrintedProgress = -1.0;

        while (System.currentTimeMillis() - startTime < totalDuration) {
            //System.out.println("Iteration " + (totalAlgorithmIterations + 1) + ":");

            totalAlgorithmIterations += performRAMStressTest(startTime, totalDuration, lastPrintedProgress);

            if (System.currentTimeMillis() - startTime >= totalDuration) {
                //System.out.println("RAM stress test reached 100%. Stopping...");
                break;
            }
        }

        //System.out.println("Total algorithm iterations: " + totalAlgorithmIterations);
        //System.out.println("RAM stress test completed.");

        int score = calculateScore(totalAlgorithmIterations, System.currentTimeMillis() - startTime);
        System.out.println("Score: " + score);

        if(benchmark == true) {
            String ramSize = getRAMSize();
            String os = System.getProperty("os.name").toLowerCase();
            String ramSpeed = getRAMSpeed(os);

            try {
                FileWriter myWriter = new FileWriter("databaseRAM.csv", true);
                myWriter.write(ramSize + ", RAM speed: " + ramSpeed + ", " + "score: " + score + "\n");
                myWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private int performRAMStressTest(long globalStartTime, long totalDuration, double lastPrintedProgress) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long startTime = System.nanoTime();
        boolean timerElapsed = false;
        boolean memoryAllocationPhase = true;

        List<Object[]> memoryList = new ArrayList<>();
        int algorithmIterations = 0;

        try {
            long remainingDuration = totalDuration - (System.currentTimeMillis() - globalStartTime);

            while (!timerElapsed) {
                if (memoryAllocationPhase) {
                    long allocatedMemoryPhase = 0;
                    while (allocatedMemoryPhase < maxMemory && System.nanoTime() - startTime < remainingDuration * 1_000_000) {
                        Object[] objects = new Object[1024];
                        memoryList.add(objects);
                        allocatedMemoryPhase += getObjectSize(objects);

                        long elapsedTime = System.currentTimeMillis() - globalStartTime;
                        timerElapsed = elapsedTime >= totalDuration;

                        double progress = (double) elapsedTime / totalDuration * 100;
                        if (progress > lastPrintedProgress + 1.0) {
                            System.out.println((int)progress);
                            lastPrintedProgress = progress;
                        }

                        if (allocatedMemoryPhase >= maxMemory * 0.95) {
                            //System.out.println("Warning: Approaching maximum heap memory limit. Stopping memory allocation.");
                            memoryAllocationPhase = false;
                            break;
                        }
                    }

                    if (allocatedMemoryPhase >= maxMemory * 0.95) {
                        memoryAllocationPhase = false;
                        //System.out.println("Memory allocation phase completed.");
                    }
                } else {
                    long algorithmsStartTime = System.nanoTime();
                    long algorithmsDuration = remainingDuration * 1_000_000;
                    while (System.nanoTime() - algorithmsStartTime < algorithmsDuration) {
                        matrixMultiplication(500);
                        algorithmIterations++;

                        fibonacciMemoization(100000);
                        algorithmIterations++;

                        sortLargeArray(1000000);
                        algorithmIterations++;

                        long elapsedTime = System.currentTimeMillis() - globalStartTime;
                        double progress = (double) elapsedTime / totalDuration * 100;
                        if ((int) progress >= 100) {
                            timerElapsed = true;
                            break;
                        }
                        if (progress > lastPrintedProgress + 1.0) {
                            System.out.println((int)progress);
                            lastPrintedProgress = progress;
                        }

                        if (Runtime.getRuntime().totalMemory() >= maxMemory * 0.95) {
                            //System.out.println("Memory limit reached. Restarting memory allocation phase.");
                            memoryList.clear();
                            memoryAllocationPhase = true;
                            break;
                        }
                    }
                    //System.out.println("Memory-intensive algorithms completed.");
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Out of memory error occurred.");
        }

        return algorithmIterations;
    }

    private long getObjectSize(Object[] objects) {
        return 8 * objects.length;
    }

    private void matrixMultiplication(int size) {
        int[][] matrixA = new int[size][size];
        int[][] matrixB = new int[size][size];
        int[][] result = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrixA[i][j] = i + j;
                matrixB[i][j] = i - j;
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = 0;
                for (int k = 0; k < size; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
    }

    private void fibonacciMemoization(int n) {
        long[] fib = new long[n + 1];
        fib[0] = 0;
        fib[1] = 1;

        for (int i = 2; i <= n; i++) {
            fib[i] = fib[i - 1] + fib[i - 2];
        }
    }

    private void sortLargeArray(int size) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = (int) (Math.random() * size);
        }

        java.util.Arrays.sort(array);
    }

    public static int calculateScore(int totalAlgorithmIterations, long timeToFillMemoryMillis) {
        return (int)((totalAlgorithmIterations * timeToFillMemoryMillis) / 1000.0);
    }
}
