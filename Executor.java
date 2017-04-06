import java.util.*;
import java.security.MessageDigest;

public class Executor extends Thread{

	@Override
	public void run(){


	}	

	/*
 *  Big blackbox function to route, execute, and return output to user
 *	input line: line of input from user or batch file
 *	return: doc specified output from the execution of that command 
* */
	private static ArrayList<String> execute(String line){

		ArrayList<String> args = new ArrayList<String>(Arrays.asList(line.split(" ")));
		System.out.println(args.toString());
		
		//in that array,args[1] is the key (if the command takes one)
		String key = "";
		if (args.size() > 1){
			key = args.get(1);
		}

		System.out.println(key);

		//ROUTE(KEY) (SKIP THIS STEP IF LIST_LOCAL
		

		//SEND COMMAND TO APPROPRIATE LISTENER ON THE CORRECT VM	



		return args;
	}



	/*
 * param key: key to route
 * return: VM that the key belongs to 
 * */

	private static int route(String key){
		/*
 *		From my notes on routing for Chord:
 *			At each step attempt to minimize your clockwise distance to the key
 *			If you can't get any closer without going over, go to your successor, and stop!
 * 		*/

		//need to turn this key into a 256 bit number
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
	
		int k = Integer.parseInt(sb.toString(),16);
		System.out.println(k);
		

		Stabilizer stab = new Stabilizer();
		long curr_node = main.ID; //starting point
		long[] finger_table = stab.finger_table(curr_node); //finger_table global from main
		boolean done = false;
	/*
		while (!done){
			//need to find the biggest entry < k, or successor otherwise
			for (int i = 0; i < 256; i++){
					

			}



		}
*/
		return 0;
	}


	//for testing only
	public static void main(String [] args){
		String line = "SET DOG 2";
		ArrayList<String> command = execute(line);		
		
		route("Dog");


	}


}












