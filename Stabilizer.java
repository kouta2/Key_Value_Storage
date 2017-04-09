import java.util.*; 

public class Stabilizer extends Thread{


	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};


	@Override
	public void run(){


	}

/*
 *param id: id you want the finger table for
 *return: a finger table belonging to the local VM, based on who's living
 * */
	public static long[] finger_table(long id){

	//need to update the list of IDS based on whose alive
	
	ArrayList<Long> newIDS = new ArrayList<Long>();  
	
	for (int i = 0; i < 10; i ++){
		if (main.LIVE_NODES[i] == true){

			newIDS.add(main.IDS[i]); 
		}
	}

	IDS = new long[newIDS.size()]; 
	for (int i = 0; i < newIDS.size(); i ++){
		IDS[i] = newIDS.get(i);
	}

	long []pows = {1L,2L,4L,8L,16L,32L,64L,128L,256L,512L,1024L,2048L,4096L,8192L,16384L,32768L,65536L,131072L,262144L,524288L,1048576L,2097152L,4194304L,8388608L,16777216L,33554432L,67108864L,134217728L,268435456L,536870912L,1073741824L,2147483648L,4294967296L};
	long[] finger_table = new long[32];
		//for every entry
		for (int i = 0; i < 32; i ++){
			long num = id + pows[i];
			num = num % (pows[32]); //	
			

			//just need to find first entry in the VM list that's bigger than this number
			long [] vms = IDS; //temporary for now! this will have to reference the globals in main!
			finger_table[i] = 0; 
			for (int j = 0; j < 10; j ++){

				//edge case, if the num is bigger than the biggest but not big enough to wrap around
				if (num > IDS[IDS.length - 1]){
					finger_table[i] = IDS[0];
					break;
				}
				if (vms[j] >= num){
					finger_table[i] = vms[j];
					break; //only take the first one	
			}

			}
		}
	
		return finger_table;
	}

    public static void check_to_update_left_and_right_replicas(int new_pid, boolean alive)
    {
        /*
        if(alive)
        {
            if(left_replica == -1 or closer(new_pid, left_replica))

        }
        else
        {

        }
        */
    }

	public static void main(String [] args){
		
		int id = 8;
		System.out.println("ID: " + IDS[id]);
		long ft[] = finger_table(IDS[id]);		
		for (int i = 0; i < 32; i ++){
			System.out.println(i + ": " + ft[i]);
		}	
	

	}

}















