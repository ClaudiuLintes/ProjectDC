import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RAMStressTest implements Runnable {
    private final long totalDuration;

    public RAMStressTest(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public static void main(String[] args) {
        // Retrieve RAM information


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

    public static String getRAMInfo() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return getRAMInfoWindows();
        } else if (os.contains("mac")) {
            return getRAMInfoMac();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return getRAMInfoLinux();
        } else {
            return "Unsupported OS";
        }
    }
    private static String getRAMInfoWindows() {
        try {
            Process process = Runtime.getRuntime().exec("wmic memorychip get capacity");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            long totalRAM = 0;
            while ((line = reader.readLine()) != null) {
                if (line.matches("\\d+")) {
                    totalRAM += Long.parseLong(line.trim());
                }
            }
            reader.close();
            long memSizeGB = totalRAM / (1024 * 1024 * 1024);
            return memSizeGB + " GB";
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return "Failed to retrieve RAM information";
        }
    }

    private static String getRAMInfoMac() {
        try {
            Process process = Runtime.getRuntime().exec("sysctl -n hw.memsize");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            long memSizeBytes = Long.parseLong(reader.readLine());
            reader.close();
            long memSizeGB = memSizeBytes / (1024 * 1024 * 1024);
            return memSizeGB + " GB";
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return "Failed to retrieve RAM information";
        }
    }

    private static String getRAMInfoLinux() {
        try {
            Process process = Runtime.getRuntime().exec("free -m");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            long totalRAM = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Mem:")) {
                    String[] parts = line.split("\\s+");
                    totalRAM = Long.parseLong(parts[1]);
                }
            }
            reader.close();
            long memSizeGB = totalRAM / 1024;
            return memSizeGB + " GB";
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return "Failed to retrieve RAM information";
        }
    }

    public static String getRAMType(String os) {
        if (os.contains("win")) {
            return getRAMTypeWindows();
        } else if (os.contains("mac")) {
            return getRAMTypeMac();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return getRAMTypeLinux();
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

    public static String getRAMTypeWindows() {
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("wmic memorychip get MemoryType");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("\\d+")) {
                    int type = Integer.parseInt(line.trim());
                    String typeName = getMemoryTypeName(type);
                    result.append(typeName).append("\n");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to retrieve RAM type information";
        }
        return result.toString();
    }

    private static String getMemoryTypeName(int type) {
        switch (type) {
            case 20:
                return "DDR";
            case 21:
                return "DDR2";
            case 22:
                return "DDR2 FB-DIMM";
            case 24:
                return "DDR3";
            case 26:
                return "DDR4";
            case 27:
                return "DDR4";
            default:
                return "Unknown";
        }
    }

    public static String getRAMTypeLinux() {
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("sudo dmidecode --type memory");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("Type:")) {
                    String type = line.split(":")[1].trim();
                    if (!type.equals("Unknown") && !type.equals("RAM")) {
                        result.append(type).append("\n");
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to retrieve RAM type information";
        }
        return result.toString();
    }

    @Override
    public void run() {
        System.out.println("Starting RAM stress test...");

        long startTime = System.currentTimeMillis();
        int totalAlgorithmIterations = 0;
        double lastPrintedProgress = -1.0;


        String ramInfo = getRAMInfo();
        String os = System.getProperty("os.name").toLowerCase();
        String ramType = getRAMType(os);

        System.out.println("RAM Info: " + ramInfo);
        System.out.println("RAM Type: " + ramType);


        while (System.currentTimeMillis() - startTime < totalDuration) {
            System.out.println("Iteration " + (totalAlgorithmIterations + 1) + ":");

            totalAlgorithmIterations += performRAMStressTest(startTime, totalDuration, lastPrintedProgress);

            if (System.currentTimeMillis() - startTime >= totalDuration) {
                System.out.println("RAM stress test reached 100%. Stopping...");
                break;
            }
        }

        System.out.println("Total algorithm iterations: " + totalAlgorithmIterations);
        System.out.println("RAM stress test completed.");

        double score = calculateScore(totalAlgorithmIterations);
        System.out.println("Score: " + score);
    }

    private int performRAMStressTest(long globalStartTime, long totalDuration, double lastPrintedProgress) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long startTime = System.nanoTime();
        boolean timerElapsed = false;
        boolean memoryAllocationPhase = true;

        List<Object[]> memoryList = new ArrayList<>();
        int operationsCount = 0;
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
                        operationsCount++;

                        long elapsedTime = System.currentTimeMillis() - globalStartTime;
                        timerElapsed = elapsedTime >= totalDuration;

                        double progress = (double) elapsedTime / totalDuration * 100;
                        if (progress > lastPrintedProgress + 1.0) {
                            System.out.printf("%.2f%% time elapsed\n", progress);
                            lastPrintedProgress = progress;
                        }

                        if (allocatedMemoryPhase >= maxMemory * 0.95) {
                            System.out.println("Warning: Approaching maximum heap memory limit. Stopping memory allocation.");
                            memoryAllocationPhase = false;
                            break;
                        }
                    }

                    if (allocatedMemoryPhase >= maxMemory * 0.95) {
                        memoryAllocationPhase = false;
                        System.out.println("Memory allocation phase completed.");
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
                            System.out.printf("%.2f%% time elapsed\n", progress);
                            lastPrintedProgress = progress;
                        }

                        if (Runtime.getRuntime().totalMemory() >= maxMemory * 0.95) {
                            System.out.println("Memory limit reached. Restarting memory allocation phase.");
                            memoryList.clear();
                            memoryAllocationPhase = true;
                            break;
                        }
                    }
                    System.out.println("Memory-intensive algorithms completed.");
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

    public static double calculateScore(long elapsedTime) {
        long baselineTime = 1_000_000_000; // 1 second in nanoseconds
        double score = (double) baselineTime / elapsedTime;
        score = score * Math.pow(10, 4);
        score = Math.round(score);
        return score;
    }
}