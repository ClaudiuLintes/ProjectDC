import javax.swing.text.StyledEditorKit;
import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class BenchmarkSettings{
    protected boolean addResultsToDatabase;
    protected int numberOfRuns;

    protected boolean writeEveryRun;

    protected boolean fillCacheFirst;

}

class Specs{
    protected String nameOS;
    protected String manufacturerOfCPU;
    protected String nameOfCPU;
    protected int numberOfThreads;
    public Specs(String nameOS,String manufacturerOfCPU,String nameOfCPU,int numberOfThreads){
        this.nameOS=nameOS;
        this.manufacturerOfCPU=manufacturerOfCPU;
        this.nameOfCPU=nameOfCPU;
        this.numberOfThreads=numberOfThreads;
    }
}
class Results{
    long singleThreadScore;
    long multiThreadScore;
    public Results(long singleThreadScore,long multiThreadScore){
        this.singleThreadScore=singleThreadScore;
        this.multiThreadScore=multiThreadScore;
    }
}
public class FileCompresserBenchmark {

    private static final int mainAlphabetSize = 256; //alphabet size of extended ASCII

    private static final int numberOfRunsToFillCache = 5; //2-3 runs needed to fill the cache from what i've tested

    private static boolean runStress;

    public FileCompresserBenchmark(boolean runStress){
        FileCompresserBenchmark.runStress=runStress;
    }

    static class MainTask implements Runnable{
        protected int id;
        protected int numberOfTests;

        protected int numberOfRuns;

        protected double currentProgress;
        protected long finalTaskScore=0;
        public MainTask(int id,int numberOfTests,double currentProgress,int numberOfRuns){
            this.id=id;
            this.numberOfTests=numberOfTests;
            this.currentProgress=currentProgress;
            this.numberOfRuns=numberOfRuns;
        }

        private void incrementProgress(){
            if(id==0) {
                currentProgress = currentProgress + (double) 1 / (numberOfRuns * numberOfTests);
                String formattedProgress = String.format("%.2f", currentProgress * 100);
                updateProgressBar(currentProgress);
                System.out.println("Progress: " + formattedProgress + "%");
            }
        }
        @Override
        public void run(){
            long[] time=new long[numberOfTests];
            double[] finish=new double[numberOfTests];
            double[] sumFinish=new double[numberOfTests];
            Arrays.fill(sumFinish,0);
            int numberOfTests=6;
            int[] fileSizes={1,10,100,1000,5000,5000}; //in kb
            double finalScore=0;
            long finalScoreInteger;
            int scoreModifier=4; //4-default 10^scoreModifier*(score)
            int timeUnit=6; //0-ns 3-ps 6- ms, 9-s
            int i=0;
            long start=System.nanoTime();
            compress("test1.txt","test1compressed.txt"); //1 KB
            decompress("test1compressed.txt","test1decompressed.txt");
            time[i]=System.nanoTime();
            incrementProgress();
            i++;
            compress("test2.txt","test2compressed.txt"); //10 KB
            decompress("test2compressed.txt","test2decompressed.txt");
            time[i]=System.nanoTime();
            incrementProgress();
            i++;
            compress("test3.txt","test3compressed.txt"); //100 KB
            decompress("test3compressed.txt","test3decompressed.txt");
            time[i]=System.nanoTime();
            incrementProgress();
            i++;
            compress("test4.txt","test4compressed.txt"); //1 MB
            decompress("test4compressed.txt","test4decompressed.txt");
            time[i]=System.nanoTime();
            incrementProgress();
            i++;
            compress("test5.txt","test5compressed.txt"); //5 MB
            decompress("test5compressed.txt","test5decompressed.txt");
            time[i]=System.nanoTime();
            incrementProgress();
            i++;
            compress("test6.txt","test6compressed.txt"); //5 MB (RandomFile)
            decompress("test6compressed.txt","test6decompressed.txt");
            incrementProgress();
            time[i]=System.nanoTime();
            finish[0]=(time[0]-start)/Math.pow(10,timeUnit);
            for(i=1;i<numberOfTests;i++){
                finish[i]=(time[i]-time[i-1])/Math.pow(10,timeUnit);
            }
            for(i=0;i<numberOfTests;i++){
                sumFinish[i]=finish[i]+sumFinish[i];
            }
            //printTimeTable(finish,numberOfTests);
            //printScoreTable(finish,fileSizes,numberOfTests,scoreModifier);
            for(i=0;i<numberOfTests;i++){
                finalScore=(fileSizes[i]/sumFinish[i])+finalScore;
            }
            //printFinalScore(sumFinish,fileSizes,numberOfRuns,numberOfTests,scoreModifier);
            finalScore=finalScore*Math.pow(10,scoreModifier);
            finalScoreInteger=Math.round(finalScore);
            //System.out.println(finalScoreInteger);
            finalTaskScore=finalScoreInteger;
        }
    }
    private static void encodeLZW(String inputFile, String outputFile) throws IOException {
        int alphabetSize = mainAlphabetSize; //The size of the alphabet will increase , as we find new groups of characters
        HashMap<String, Integer> LZWdictionary = new HashMap<>(); //having the keys being strings
        for (int i = 0; i < alphabetSize; i++) {
            LZWdictionary.put(String.valueOf((char) i), i); //we put the ASCII table in our LZW dictionary
        }
        DataInputStream DataInputReader = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile))); //we initialize both the input and output file data streams
        DataOutputStream DataOutputWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        String charactersFound = "";
        while (DataInputReader.available()>=1) { //we read character by character [256 ASCII] (byte by byte)
            int character=DataInputReader.read();
            String addCharacters = charactersFound + (char)character;//we select the characters to add in the dictionary
            if (LZWdictionary.containsKey(addCharacters)) {
                charactersFound = addCharacters; //if the selected character is already in the dictionary it will be added to the Found string and the next one will be concatanated to it
            } else {//if the found character is not in our dictionary it's time to add the group of characters to it and output the group before it EX: [AB]CD | Out: A | AB is selected and added but A is outputed
                DataOutputWriter.writeInt(LZWdictionary.get(charactersFound));
                if(alphabetSize<Integer.MAX_VALUE-1) {
                    LZWdictionary.put(addCharacters, alphabetSize++); //We only put characters in the dictionary till MAX_INT to avoid going past the integer limit in our encoding
                }
                charactersFound = String.valueOf((char)character);//we also make sure to replace the found string , keeping only the last found character so not to skip it EX: [AB]CD | Found:AB -> Found:B
            }
        }
        if(!charactersFound.isEmpty()) {
            DataOutputWriter.writeInt(LZWdictionary.get(charactersFound)); //this makes sure that we output the last group of characters even if they are already in the dictionary
        }
        DataInputReader.close();
        DataOutputWriter.close();
    }
    private static void decodeLZW(String inputFile,String outputFile) throws IOException{
        int alphabetSize=mainAlphabetSize; //The size of the alphabet will increase , as we find new groups of characters
        HashMap<Integer,String> LZWdictionary=new HashMap<>();
        for (int i=0;i<alphabetSize;i++){
            LZWdictionary.put(i,String.valueOf((char)i)); //we put the ASCII table in our LZW dictionary having the keys being integers
        }
        DataInputStream DataInputReader = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile))); //we initialize both the input and output file data streams
        DataOutputStream DataOutputWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        String charactersFound="";
        int intCharacterCode;
        if(DataInputReader.available()>=4){
            intCharacterCode=DataInputReader.readInt();
            charactersFound=LZWdictionary.get(intCharacterCode); //we extract the first character , knowing that it will always already be in the dictionary so we can use it later [!]
            DataOutputWriter.write(charactersFound.charAt(0)); //we also output it
        }
        while(DataInputReader.available()>=4){ //1.we read int by int (4 bytes) the file
            intCharacterCode=DataInputReader.readInt();
            String outputCharacters;
            if (LZWdictionary.containsKey(intCharacterCode)) {
                outputCharacters = LZWdictionary.get(intCharacterCode); //we find the output characters in the dictionary using the code
            } else {
                outputCharacters = charactersFound + charactersFound.charAt(0); //if the output characters are not in the dictionary we will output the found characters appended to the first found character
            }                                                               //so we can avoid the only case when we couldn't compute the dictonary in time [!]
            DataOutputWriter.write(outputCharacters.getBytes());
            if(alphabetSize<Integer.MAX_VALUE-1) {
                LZWdictionary.put(alphabetSize++, charactersFound + outputCharacters.charAt(0)); //we add to the dictionary the found characters + the output characters (Until we reach MAX_INT)
            }
            charactersFound = outputCharacters; //we keep the characters we found
        }
        DataInputReader.close();
        DataOutputWriter.close();
    }

    private static void compress(String inputFile,String outputFile){
        try{
            encodeLZW(inputFile,outputFile);
        }
        catch(IOException e){
            System.out.println(e);
        }
    }

    private static void decompress(String inputFile,String outputFile){
        try{
            decodeLZW(inputFile,outputFile);
        }
        catch(IOException e){
            System.out.println(e);
        }
    }

    private static BenchmarkSettings readSettings(String SettingsFileName) throws IOException{
        BufferedReader InputReader=new BufferedReader(new FileReader(SettingsFileName));
        String line;
        ArrayList<String> values=new ArrayList<>();
        BenchmarkSettings results=new BenchmarkSettings();
        while((line=InputReader.readLine())!=null){
            StringTokenizer stringTokenizer=new StringTokenizer(line,"=");
            stringTokenizer.nextToken();
            values.add(stringTokenizer.nextToken());
        }
        results.addResultsToDatabase=Boolean.parseBoolean(values.getFirst());
        results.numberOfRuns = Integer.parseInt(values.get(1));
        results.writeEveryRun = Boolean.parseBoolean(values.get(2));
        results.fillCacheFirst = Boolean.parseBoolean(values.get(3));
        return results;
    }

    public static String getCpuInfo(int selector) { //selector 0- manufacturer 1- cpu model
        try {
            Process process = Runtime.getRuntime().exec("wmic cpu get name");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.trim().split("\\s+", 2);
                    if (parts.length > 1) {
                        return parts[selector];
                    }
                }
            }

            // Close the reader
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Failed to get the name["+selector+"] of the processor";
    }

    public static String getCurrentDate(){
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return currentDate.format(dateFormatter);
    }

    private static Specs initializeSpecs(){
        String osName=System.getProperty("os.name");
        String cpuManufacturer=getCpuInfo(0);
        String cpuName=getCpuInfo(1);
        int cpuThreads = Runtime.getRuntime().availableProcessors();
        return new Specs(osName,cpuManufacturer,cpuName,cpuThreads);
    }

    private static String getSpecs(Specs specs){
        return specs.nameOS+","+specs.manufacturerOfCPU+","+specs.nameOfCPU+","+specs.numberOfThreads;
    }

    private static void addToDatabase(String databaseFileName,Specs specs,int numberOfRuns,long singleScore,long multiScore)throws IOException{
        FileWriter fileWriter=new FileWriter(databaseFileName,true);
        BufferedWriter databaseOutput=new BufferedWriter(fileWriter);
        databaseOutput.write(getSpecs(specs)+","+numberOfRuns+","+singleScore+","+multiScore+","+getCurrentDate());
        databaseOutput.newLine();
        databaseOutput.close();
    }


    private static void printTimeTable(double[] finish,int numberOfTests){
        int i;
        System.out.print("RunTimes: ");
        for(i=0;i<numberOfTests;i++) {
            System.out.print(finish[i]);
            if (i != numberOfTests - 1) {
                System.out.print(" | ");
            }
        }
        System.out.print("\n");
    }

    private static void printScoreTable(double[] finish,int[] fileSizes,int numberOfTests,int scoreModifier){
        int i;
        System.out.print("RunScore: ");
        for(i=0;i<numberOfTests;i++) {
            System.out.print(fileSizes[i]/finish[i]*Math.pow(10,scoreModifier));;
            if (i != numberOfTests - 1) {
                System.out.print(" | ");
            }
        }
        System.out.print("\n");
    }

    private static void printFinalScore(double[] sumFinish,int[] fileSizes,int numberOfRuns,int numberOfTests,int scoreModifier){
        System.out.print("Exec Times: ");
        for(int i=0;i<numberOfTests;i++) {
            System.out.print("Exec Times: " + sumFinish[i] / numberOfRuns);
            if(i!=numberOfTests-1){
                System.out.print(" | ");
            }
        }
        System.out.print("\n");
        for(int i=0;i<numberOfTests;i++) {
            System.out.print("Scores: " + fileSizes[i] / sumFinish[i]*Math.pow(10,scoreModifier));
            if(i!=numberOfTests-1){
                System.out.print(" | ");
            }
        }
        System.out.print("\n");
    }

    private static void updateResults(Results results){
        //updateResults
    }

    private static void updateProgressBar(double progress){
        //update the progress bar
    }

    public static void main(String []args){
        if (args.length>0 && Objects.equals(args[0], "-stress")) {
            runStress = true;
            System.out.println("Running in stress mode.");
        }
        String SettingsFileName="settingsCPU.txt";
        String DatabaseFileName="databaseCPU.csv";
        String SecondaryDatabaseFileName="databaseCPUSecondary.csv";
        int numberOfTests=6;
        long scoresSum=0;
        long maxScore=0;
        long SingleFinalScoreSum=0;
        long MultiFinalScoreAbs=0;
        long SingleFinalScore=-1;
        long MultiFinalScore=-1;
        int numberOfThreads=Runtime.getRuntime().availableProcessors();
        MainTask[] tasksArray=new MainTask[numberOfThreads];
        Specs currentSpecs=initializeSpecs();
        //System.out.println(getSpecs(currentSpecs));
        try {
            BenchmarkSettings settings = readSettings(SettingsFileName);
            int runs= settings.numberOfRuns;
            int progressCacheRuns=0;
            if(settings.fillCacheFirst){
                runs=runs+numberOfRunsToFillCache;
                progressCacheRuns=numberOfRunsToFillCache;
            }
            System.out.println("number of runs: "+runs);
            double progressPercentage=0;
            String formattedProgress=String.format("%.2f",progressPercentage);
            System.out.println("Progress: "+formattedProgress+"%");
            while(runs!=0) {
                ExecutorService executor= Executors.newFixedThreadPool(numberOfThreads);
                for (int i = 0; i < numberOfThreads; i++) {
                    if (runStress) {
                        settings.numberOfRuns = -1;
                        settings.addResultsToDatabase = false;
                        settings.writeEveryRun = false;
                        settings.fillCacheFirst = false;
                    }
                    tasksArray[i] = new MainTask(i, numberOfTests,progressPercentage,settings.numberOfRuns+progressCacheRuns);
                    executor.submit(tasksArray[i]);
                }
                executor.shutdown();
                if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    System.out.println("[Task Closed]: -took too long to execute");
                }
                if(!(settings.fillCacheFirst&&runs>settings.numberOfRuns)) { //we only keep score after filling the cache
                    for (int i = 0; i < numberOfThreads; i++) {
                        if (maxScore < tasksArray[i].finalTaskScore) {
                            maxScore = tasksArray[i].finalTaskScore;
                        }
                        scoresSum = tasksArray[i].finalTaskScore + scoresSum;
                    }
                    SingleFinalScoreSum=maxScore+SingleFinalScoreSum;
                    MultiFinalScoreAbs=Math.abs(MultiFinalScoreAbs-scoresSum); //multi thread score for each run
                    if (settings.writeEveryRun) {
                        addToDatabase(SecondaryDatabaseFileName, currentSpecs, settings.numberOfRuns, maxScore, MultiFinalScoreAbs);
                    }
                }
                progressPercentage=tasksArray[0].currentProgress;
                runs--;
            }
            //SingleFinalScore=(scoresSum/numberOfThreads)/settings.numberOfRuns; //average thread
            SingleFinalScore = SingleFinalScoreSum/settings.numberOfRuns; //best thread
            MultiFinalScore = scoresSum/settings.numberOfRuns;
            Results benchmarkResults=new Results(SingleFinalScore,MultiFinalScore);
            updateResults(benchmarkResults);
            System.out.println("Single-Thread Score: "+SingleFinalScore+" | Multi-Thread Score:"+MultiFinalScore);
            if(settings.addResultsToDatabase){
                addToDatabase(DatabaseFileName,currentSpecs,settings.numberOfRuns,SingleFinalScore,MultiFinalScore);
            }
        }
        catch (IOException e){
            e.getStackTrace();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
