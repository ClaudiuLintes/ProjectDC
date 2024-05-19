import javax.swing.*;
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

class HuffmanNode implements Comparable<HuffmanNode>{
    protected HuffmanNode leftNode,rightNode;
    protected final int freq;

    HuffmanNode(int freq , HuffmanNode leftNode, HuffmanNode rightNode){
        this.freq=freq;
        this.leftNode=leftNode;
        this.rightNode=rightNode;
    }

    HuffmanNode(int freq){
        this.freq=freq;
    }

    public int compareTo(HuffmanNode that){
        return this.freq-that.freq;
    }

}

class HuffmanLeafNode extends HuffmanNode{
    protected int character;
    HuffmanLeafNode(int character,int freq){
        super(freq);
        this.character=character;
    }
}

public class FileCompresserBenchmark {

    private static final int mainAlphabetSize = 256; //alphabet size of extended ASCII

    private static final int numberOfRunsToFillCache = 5; //2-3 runs needed to fill the cache from what i've tested

    private static final int huffmanCodeMaxLength = 4096; //How big can a outputCode string can get in huffmanEncoding [Default 4096]

    private static boolean runStress;

    private static boolean runBenchmark1;

    private static boolean runBenchmark2;

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
                /*String formattedProgress = String.format("%.2f", currentProgress * 100);
                System.out.println("Progress: " + formattedProgress + "%");*/
                System.out.println((int)(currentProgress*100));
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

    private static void buildCodeTable(String[] st, HuffmanNode x, String s) {
        if (!(x instanceof HuffmanLeafNode)) {
            buildCodeTable(st, x.leftNode, s + '0');
            buildCodeTable(st, x.rightNode, s + '1');
        } else {
            st[((HuffmanLeafNode) x).character] = s;
        }
    }

    private static HuffmanNode buildHuffmanTree(int[] freq) { //We use the frequency array to build our huffman tree [~]
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        int alphabetSize = mainAlphabetSize;
        for (int c = 0; c < alphabetSize; c++) { //We try each character [Int 4 Bytes] and if it has a frequency bigger than 0 we add it to the tree
            if (freq[c] > 0) {
                pq.add(new HuffmanLeafNode(c, freq[c]));
            }
        }
        while (pq.size() > 1) {
            HuffmanNode leftNode = pq.remove();
            HuffmanNode rightNode = pq.remove();
            HuffmanNode parent = new HuffmanNode(leftNode.freq + rightNode.freq, leftNode, rightNode);
            pq.add(parent);
        }
        return pq.remove();
    }

    private static int[] getFrequencyTable(String inputFile) throws IOException {
        int alphabetSize = mainAlphabetSize;//we consider the full alphabet
        DataInputStream DataInputReader=new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile))); //we initialize the input
        int[] freq = new int[alphabetSize]; //stores the frequencies
        while (DataInputReader.available()>=1) { //we go through all the characters increasing the frequency array
            byte intCharacterCode=DataInputReader.readByte();
            freq[intCharacterCode]++;
        }
        DataInputReader.close();
        return freq;
    }

    private static void encodeHuffman(String inputFile,String outputFile) throws IOException{ //File : 1 [int] alphabet size | 256+ [ints] frequency of characters | 1 [int] LastRightShift number | Bytes
        int[] frequency=getFrequencyTable(inputFile);
        DataOutputStream OutputByteWriter=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        int alphabetSize = mainAlphabetSize;
        OutputByteWriter.writeInt(alphabetSize);//We first write the size of the encoded alphabet
        for(int i=0;i<alphabetSize;i++){
            OutputByteWriter.writeInt(frequency[i]);//Write all frequencies int by int
        }
        HuffmanNode root = buildHuffmanTree(frequency);
        String[] codesArray = new String[alphabetSize];
        buildCodeTable(codesArray, root, "");
        DataInputStream InputDataReader=new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        byte c;
        int rightShiftDecodingNumber=0;
        byte[] byteArray;
        String currentCodes;
        StringBuilder outputCode= new StringBuilder();
        while(InputDataReader.available()>=1) {
            c = InputDataReader.readByte();
            String saveLast="";
            outputCode.append(codesArray[c]); //we create the output code
            if(outputCode.length()>=huffmanCodeMaxLength||InputDataReader.available()<1) {
                if(outputCode.length()>=huffmanCodeMaxLength) {
                    currentCodes = outputCode.substring(0, huffmanCodeMaxLength);
                    saveLast=outputCode.substring(huffmanCodeMaxLength); //We save the last incomplete byte for each buffer of size >huffmanCodeMaxLength
                }
                else{
                    currentCodes=outputCode.substring(0,outputCode.length());
                }
                byteArray = new byte[(currentCodes.length() + 7) / 8]; //we have 1 byte at least
                rightShiftDecodingNumber = currentCodes.length() % 8;//The last byte will need to be right shifted by it when decoding
                for (int i = 0; i < currentCodes.length(); i++) {
                    if (outputCode.charAt(i) == '1') {
                        int bitNumber = (i % 8);//the bit number can be between 0-7 (i%8)
                        int byteNumber = i / 8; //the byte number is i/8 , i being the character position in output code
                        int leftShiftNumber = 7 - bitNumber; //the number we have to left shift to put the bit on the correct position
                        byteArray[byteNumber] |= (byte) (1 << leftShiftNumber); //We create the byte
                    }
                }
                OutputByteWriter.write(byteArray);
                outputCode.setLength(0);
                outputCode.append(saveLast); //We the saved last incomplete byte append to the next one
            }
        }
        InputDataReader.close();
        OutputByteWriter.writeInt(rightShiftDecodingNumber); //We write as the last Int (4 Bytes) the number we need to right shift the last byte of content
        OutputByteWriter.close();
    }

    private static void decodeHuffman(String inputFile,String outputFile) throws IOException{
        DataInputStream InputDataReader=new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        DataOutputStream OutputDataWriter=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        int alphabetSize=InputDataReader.readInt(); //We read the alphabet size
        int[] frequency=new int[alphabetSize];
        for(int i=0;i<alphabetSize;i++){
            frequency[i]=InputDataReader.readInt();//We read the frequency for all the characters
        }
        int rightShiftDecodingNumber;
        HuffmanNode root=buildHuffmanTree(frequency);
        int currentByteCode;
        int lastShift=8; //Becomes the right shift amount for last byte
        HuffmanNode currentNode=root;
        while(InputDataReader.available()>=5){
            currentByteCode=InputDataReader.read();
            if(InputDataReader.available()==4){
                rightShiftDecodingNumber=InputDataReader.readInt();//We read the number that we right shift by the last byte
                if(rightShiftDecodingNumber!=0) {
                    lastShift = rightShiftDecodingNumber; //We "Right Shift" (remove) the last bits from the last byte
                }
            }
            for(int i=7;i>=8-lastShift;i--){
                int currentBit=(currentByteCode>>i)&1;
                if(currentBit==0){
                    currentNode=currentNode.leftNode;
                }
                else{
                    currentNode=currentNode.rightNode;
                }
                if(currentNode instanceof HuffmanLeafNode){
                    OutputDataWriter.writeByte(((HuffmanLeafNode) currentNode).character);
                    currentNode=root;
                }
            }
        }
        InputDataReader.close();
        OutputDataWriter.close();
    }

    private static void compress(String inputFile,String outputFile){
        try{
            if(runBenchmark1){
                encodeLZW(inputFile,outputFile);
            }
            else if(runBenchmark2) {
                encodeHuffman(inputFile, outputFile);
            }
        }
        catch(IOException e){
            System.out.println(e);
        }
    }

    private static void decompress(String inputFile,String outputFile){
        try{
            if(runBenchmark1){
                decodeLZW(inputFile,outputFile);
            }
            else if(runBenchmark2) {
                decodeHuffman(inputFile, outputFile);
            }
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
        String testType="Failed";
        if(runBenchmark1){
            testType="LZW";
        }
        if(runBenchmark2){
            testType="Huffman";
        }
        BufferedWriter databaseOutput=new BufferedWriter(fileWriter);
        databaseOutput.write(getSpecs(specs)+","+numberOfRuns+","+singleScore+","+multiScore+","+getCurrentDate()+","+testType);
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

    public static void main(String []args){
        if(args.length>0){
            if (Objects.equals(args[0], "-stress")) {
                runStress = true;
                runBenchmark1 = true;
                System.out.println("Running in stress mode.");
            }
            else if (Objects.equals(args[0], "-benchmark1")) {
                runBenchmark1 = true;
                System.out.println("Running benchmark1 mode.");
            }
            else if (Objects.equals(args[0], "-benchmark2")) {
                runBenchmark2 = true;
                System.out.println("Running benchmark2 mode.");
            }
        }
        String SettingsFileName="settings.txt";
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
            /*String formattedProgress=String.format("%.2f",progressPercentage);
            System.out.println("Progress: "+formattedProgress+"%");*/
            System.out.println(0);
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
            System.out.println(SingleFinalScore+","+MultiFinalScore);
            //System.out.println("Single-Thread Score: "+SingleFinalScore+" | Multi-Thread Score:"+MultiFinalScore);
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
