import java.util.*;
import java.security.MessageDigest;


public class Executor extends Thread{

	
	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};
	
	@Override
	public void run(){


	}	


	/*
 *
 *  helper fxn to calculate clockwise mod 2^32 distance from a->b
 *  */
	private static long cw_distance(long a, long b){
		if (b >= a){
			return b-a;
		}else{
			return (b + 4294967296L) - a; 
		}

	}

/*
	private static String get(String key){

	}

	private static String set(String key, String val){


	}
*/

	
	


	/*
 *  Big blackbox function to route, execute, and return output to user
 *	input line: line of input from user or batch file
 *	return: doc specified output from the execution of that command 
* */
	private static ArrayList<String> execute(String line){

		ArrayList<String> args = new ArrayList<String>(Arrays.asList(line.split(" ")));
		//System.out.println(args.toString());
		
		//in that array,args[1] is the key (if the command takes one)
		String key = "";
		if (args.size() > 1){
			key = args.get(1);
		}

		if (args.get(0).equals("SET")){

			//RPC business!			

		}else if (args.get(0).equals("GET")){

			//RPC business!


		}else if (args.get(0).equals("LIST_LOCAL")){
	
			
	
		}else if (args.get(0).equals("OWNERS")){
			//need to find the owners of this key
						
		}else if (args.get(0).equals("BATCH")){
			//need to execute a batch command
		}else{
			System.err.println("ERROR: Unsupported Command");	
		}
			


		//ROUTE(KEY) (SKIP THIS STEP IF LIST_LOCAL
		

		//SEND COMMAND TO APPROPRIATE LISTENER ON THE CORRECT VM	



		return args;
	}



	/*
 * param key: key to route
 * return: VM that the key belongs to 
 * */

	public static int route(String key){
		/*
 *		From my notes on routing for Chord:
 *			At each step attempt to minimize your clockwise distance to the key
 *			If you can't get any closer without going over, go to your successor, and stop!
 * 		*/

		//need to turn this key into a 32 bit number
			MessageDigest md = null;
			try{
				md = MessageDigest.getInstance("SHA-256");
			}catch (Exception ex){
				System.out.println("No such algorithm!");
			}
			
			md.update(key.getBytes());
			byte byteData[] = md.digest();
			//just take last 4 bytes
			byteData = Arrays.copyOfRange(byteData, 12, 16);

			StringBuffer sb = new StringBuffer();
        	for (int i = 0; i < byteData.length; i++) {
        		sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        	}	
		//	System.out.println(sb);	
			long k = Long.parseLong(sb.toString(),16);
			System.out.println(k);
		//done, k is that number!

		long found_id = binsearch(k); 
		
		return (int)main.ID_TO_INDEX.get(found_id); 

	}


	//wrapper function
	//replaces the routing function, takes a key and returns an int (1 to 10) of the thing it belongs to 
	public static long binsearch(long k){
		if (k < main.LIVE_IDS[0]) return main.LIVE_IDS[0]; 
		if (k >= main.LIVE_IDS[main.LIVE_IDS.length]) return main.LIVE_IDS[0]; 

		return binsearch(main.LIVE_IDS, k, 0, main.LIVE_IDS.length-1);

	}


	public static long binsearch(long[] arr, long k, int min, int max){

		if (max <= min){
			return arr[min]; //only one entry 
		}

		int mid = (max+min)/2;
		
		
		//both too low, go right
		if (arr[mid] <= k) {
			return binsearch(arr,k, mid + 1, max); 
		}

		//too high, go left
		if (arr[mid] > k){
			return binsearch(arr,k,min,mid); 	
		} 

		return 0L;
	}


	

	//for testing only
	public static void main(String [] args){
		

		String key = "Dog";	
		
		long [] test = {0L, 5L, 10L, 15L, 20L, 25L, 30L}; 
		long check = 35L;


		System.out.println(binsearch(test, check, 0, test.length-1)); 
	}


}












