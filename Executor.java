import java.util.*;
import java.security.MessageDigest;


public class Executor extends Thread{

	
	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};
	

	/*
 * param key: key to route
 * return: VM that the key belongs to 
 * */

	public static int route(String key){
		/*
 *		From my notes on routing for Chord:
 *			At each step attempt to minimize your clive_ids_lockwise distance to the key
 *			If you can't get any closer without going over, go to your successor, and stop!
 * 		*/

		//need to turn this key into a 32 bit number
			MessageDigest md = null;
			try{
				md = MessageDigest.getInstance("SHA-256");
			}catch (Exception ex){
				//System.err.println("No such algorithm!");
			}
			
			md.update(key.getBytes());
			byte byteData[] = md.digest();
			//just take last 4 bytes
			byteData = Arrays.copyOfRange(byteData, 12, 16);

			StringBuffer sb = new StringBuffer();
        	for (int i = 0; i < byteData.length; i++) {
        		sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        	}	
			long k = Long.parseLong(sb.toString(),16);
			// System.err.println("Key hashed to: " + k);
		//done, k is that number!

		long found_id = binsearch(k); 
		
		// System.err.println("ROUTED TO: " + found_id);
		return (int)main.ID_TO_INDEX.get(found_id); 

	}


	//wrapper function
	//replaces the routing function, takes a key and returns an int (1 to 10) of the thing it belongs to 
	public static long binsearch(long k){
        main.live_ids_lock.lock();
        try
        {
		    if (k < main.LIVE_IDS[0])
			    return main.LIVE_IDS[0]; 
		    if (k >= main.LIVE_IDS[main.LIVE_IDS.length-1])
			    return main.LIVE_IDS[0]; 
		    return binsearch(main.LIVE_IDS, k, 0, main.LIVE_IDS.length-1);
        }
        finally {main.live_ids_lock.unlock();}

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
}
