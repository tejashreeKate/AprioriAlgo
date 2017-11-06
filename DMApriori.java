import java.util.*;
import java.util.function.Consumer;
import java.io.*;

public class DMApriori {
	//store itemsets at every level while generating frequent itemsets
	private List<int[]> itemsets ;
	
	//provide mapping between actual item names and a number
	private HashMap<Integer, String> hmItems = new HashMap<Integer, String>();
	
	//store support count for every freq itemset generated
	private HashMap<String,Integer> supportData = new HashMap<String, Integer>();
	
    private int numItems; 

    private int numTransactions; 

    private double support ; 
    
    private double minConf ;
        
    private String dataFile = "data2.txt";


    //constructor
    public DMApriori() throws Exception{
    	//map the data and read the data from file
		readFile();
	}
	public void readFile() throws Exception
    {        
    	hmItems.put(1,"Coffee");
    	hmItems.put(2,"Water");
    	hmItems.put(3,"Milk");
    	hmItems.put(4,"Bread");
    	hmItems.put(5,"Oil");
    	hmItems.put(6,"Ranch");
    	hmItems.put(7,"AppleCider");
    	hmItems.put(8,"Pasta");
    	hmItems.put(9,"Cheese");
    	hmItems.put(10,"Nuts");

		//read data file and calculate number of items and transactions
    	numItems = 0;
    	numTransactions=0;
		itemsets = new ArrayList<int[]>();
		
    	BufferedReader data_in = new BufferedReader(new FileReader(dataFile));
    	
    	while (data_in.ready()) {    		
    		String line=data_in.readLine();
    		if (line.matches("\\s*")) continue;
    		
    		numTransactions++;
    		
    		StringTokenizer st = new StringTokenizer(line," ");
    		while (st.hasMoreTokens()) {
    			boolean found = false;
    			int val = Integer.parseInt(st.nextToken());
    			
    			int[] cand = {val};
    			for(int[] obj:itemsets){
    				if(obj[0]==val){
    					found = true;
    					break;
    				}
    			}
    			if(!found){
    				itemsets.add(cand);
    				numItems++;
    			}
    		}    		
    	} 
		System.out.println("Data: Total Number of items are "+numItems+" items, and "+numTransactions+" transactions, ");
    }
		
	//print the frequent itemsets found
    private void printFreqItemSet(int[] itemset, int support) {
    	//System.out.println("Found frequent itemsets of size "+itemset.length);
    	float sup = ((support / (float) numTransactions));
    	System.out.println(Arrays.toString(itemset) + "  [sup = "+ sup +", numTrans = "+support+"]");
    }
	
    //assign true corr to val if its found in line
    public void scanLineForMatch(String line, boolean[] trans) {
	    Arrays.fill(trans, false);
	    StringTokenizer st = new StringTokenizer(line, " "); 
	    while (st.hasMoreTokens())
	    {	    	
	        int parsedVal = Integer.parseInt(st.nextToken());
			trans[parsedVal]=true; 
	    }
    }
    
    private void generateAssociationRules(int[] arr){
    	int loop = 1;
    	HashSet<Integer> main = new HashSet<Integer>();
    	HashSet<String> mainActual = new HashSet<String>();

    	for(int i=0;i<arr.length;i++){
    		main.add(arr[i]);
    		mainActual.add(hmItems.get(arr[i]));
    	}
    	while(loop<arr.length){
	    	for(int i=0;i<arr.length;i++){
	        	HashSet<Integer> leftset = new HashSet<Integer>();
	        	HashSet<Integer> rightset = new HashSet<Integer>(main);
	        	HashSet<String> leftActualItems = new HashSet<String>();
	        	HashSet<String> rightActualItems = new HashSet<String>(mainActual);

	        	int count = 0;
	    		for(int j=i ;count<loop && j<arr.length;j++){
	    			//System.out.println(j);
	    			leftset.add(arr[j]);
	    			leftActualItems.add(hmItems.get(arr[j]));
	    			count++;
	    		}
    			while(count<loop){
    				for(int j=0 ;count<loop && j<arr.length;j++){
    	    			leftset.add(arr[j]);
    	    			leftActualItems.add(hmItems.get(arr[j]));
    	    			count++;
    	    		}
    			}
    			//System.out.println(count);
	    		rightset.removeAll(leftset);
	    		rightActualItems.removeAll(leftActualItems);
	    		double conf = (double)supportData.get(main.toString())/supportData.get(leftset.toString());
	    		//System.out.println(conf);
	    		if(conf >= minConf){
		    		//System.out.println(leftset.toString()+"---->>>"+rightset.toString());
		    		System.out.println(leftActualItems.toString()+"---->"+rightActualItems.toString());

	    		};
	    		
	    	}
	    	loop++;
    	}
    }

	public void calcFrequentItemsets() throws Exception
    {
    	
        //System.out.println("Passing through the data to compute the frequency of " + itemsets.size()+ " itemsets of size "+itemsets.get(0).length);

        List<int[]> frequentCandidates = new ArrayList<int[]>(); 

        boolean match; 
        
        //count matches
        int count[] = new int[itemsets.size()]; 

		BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));

		boolean[] trans = new boolean[256];
		
		for (int i = 0; i < numTransactions; i++) {

			while (data_in.ready()) {    		
	    		String line=data_in.readLine();		    		
				scanLineForMatch(line, trans);			

				for (int j = 0; j < itemsets.size(); j++) {
					match = true; 
					int[] cand = itemsets.get(j);
					
					for (int k : cand) {
						if (trans[k] == false) {
							match = false;
							break;
						}
					}
					if (match) { 
						count[j]++;
					}
				}
			}

		}
		
		data_in.close();

		for (int i = 0; i < itemsets.size(); i++) {
			double itemSup = (count[i]/(double) numTransactions);
			if ( itemSup >= support) {
				printFreqItemSet(itemsets.get(i),count[i]);
				//add count to supportData
				supportData.put(Arrays.toString(itemsets.get(i)),count[i]);
				frequentCandidates.add(itemsets.get(i));
				//generate association rules
				generateAssociationRules(itemsets.get(i));
			}
			//else System.out.println("-- Remove candidate: "+ Arrays.toString(itemsets.get(i)) + "  is: "+ ((count[i] / (double) numTransactions)));
		}
		
        itemsets = frequentCandidates;
    }
	 private void generateNewItemSets()
	    {
	    	int currentSizeOfItemsets = itemsets.get(0).length;
	    	//System.out.println("Creating itemsets of size "+(currentSizeOfItemsets+1)+" based on "+itemsets.size()+" itemsets of size "+currentSizeOfItemsets);
	    		
	    	HashMap<String, int[]> tempCandidates = new HashMap<String, int[]>(); //temporary candidates
	    	
	        for(int i=0; i<itemsets.size(); i++)
	        {
	            for(int j=i+1; j<itemsets.size(); j++)
	            {
	                int[] temp1 = itemsets.get(i);
	                int[] temp2 = itemsets.get(j);

	                //assert (X.length==Y.length);
	                int [] newCand = new int[currentSizeOfItemsets+1];
	                for(int s=0; s<newCand.length-1; s++) {
	                	newCand[s] = temp1[s];
	                }
	                    
	                int diff = 0;
	                for(int s1=0; s1<temp2.length; s1++)
	                {
	                	boolean found = false;
	                    for(int s2=0; s2<temp1.length; s2++) {
	                    	if (temp1[s2]==temp2[s1]) { 
	                    		found = true;
	                    		break;
	                    	}
	                	}
	                	if (!found){
	                		diff++;
	                		newCand[newCand.length -1] = temp2[s1];
	                	}
	            	
	            	}
	                
	                //assert(diff>0);
	                	                
	                if (diff==1) {	                   
	                	Arrays.sort(newCand);
	                	tempCandidates.put(Arrays.toString(newCand),newCand);
	                }
	            }
	        }
	        
	        itemsets = new ArrayList<int[]>(tempCandidates.values());
	    	//System.out.println("Created "+itemsets.size()+" unique itemsets of size "+(currentSizeOfItemsets+1));

	    }

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DMApriori ap = new DMApriori();

		double sup;
		//get the minimum support and confidence from user
		double conf;
		Scanner in = new Scanner(System.in);
		System.out.println("Enter the minimum support(%):");
		sup = in.nextDouble();
		System.out.println("Enter the minimum confidence(%):");
		conf = in.nextDouble();
		
		ap.support = (double)sup/100;
		ap.minConf = (double)conf/100;
		
        int itemsetNumber=1; 
        int freqSets=0;
        
        while (ap.itemsets.size()>0)
        {
            ap.calcFrequentItemsets();

            if(ap.itemsets.size()!=0)
            {
                freqSets+=ap.itemsets.size();
                //System.out.println("Created "+ap.itemsets.size()+" frequent itemsets of size " + itemsetNumber + " [support "+(sup)+"%]");;
                ap.generateNewItemSets();
            }

            itemsetNumber++;
        }    

	}

}
