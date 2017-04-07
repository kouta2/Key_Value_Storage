import java.util.*;
import java.security.MessageDigest;


public class Executor extends Thread{

	
	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};
	
	@Override
	public void run(){


	}	


	/*
 *
 *  function to calculate clockwise mod 2^32 distance from a->b
 *  */
	private static long cw_distance(long a, long b){
		if (b >= a){
			return b-a;
		}else{
			return (b + 4294967296L) - a; 
		}

	}





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
			
		}else if (args.get(0).equals("BATCH")){

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

	private static long route(String key){
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
		//	System.out.println(k);
		//done, k is that number!

		
		long curr_node = IDS[3]; //starting point (hard coded for now)
		boolean done = false;
		long found_id = 0; 	
		while (!done){
			//need to find the biggest entry in my table < k, or successor otherwise

			long [] ft = Stabilizer.finger_table(curr_node);
			long best = curr_node; 
			boolean found_closer = false; 
			for (int i = 0; i < 32; i++){
				if (ft[i] < k && cw_distance(ft[i],k) < cw_distance(best,k)){
					best = ft[i]; 
					found_closer = true; 	
				}

			}			
	
			if (found_closer){
				curr_node = best; 
			}else{
				found_id = ft[0]; 
				done = true; 	
			}

		}

	
		System.out.println(found_id);
		return found_id;
	}


	//for testing only
	public static void main(String [] args){
		String line = "SET DOG 2";
		ArrayList<String> command = execute(line);		
		
		route("dog");
		route("Course");
		route("Hero");
		route("Yeee_boi");

		for (int i = 0; i < 50; i ++){
			route(Integer.toString(i));
		}


	}


}












