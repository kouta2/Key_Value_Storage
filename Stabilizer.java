public class Stabilizer extends Thread{


	@Override
	public void run(){


	}

/*
 *param id: id you want the finger table for
 *return: a finger table belonging to the local VM, based on who's living
 * */
	public long[] finger_table(long id){

	long []pows = {1L,2L,4L,8L,16L,32L,64L,128L,256L,512L,1024L,2048L,4096L,8192L,16384L,32768L,65536L,131072L,262144L,524288L,1048576L,2097152L,4194304L,8388608L,16777216L,33554432L,67108864L,134217728L,268435456L,536870912L,1073741824L,2147483648L,4294967296L};
	long[] finger_table = new long[32];
		//for every entry
		for (int i = 0; i < 32; i ++){
			long num = id + pows[i];
			num = num % (4294967296L-1); //	

			//just need to find first entry in the VM list that's bigger than this number
			long [] vms = main.IDS;
			for (int j = 0; j < 10; j ++){
				if (vms[j] >= num){
					finger_table[i] = vms[j];
				}

			}
		}
	
		return finger_table;
	}



}
