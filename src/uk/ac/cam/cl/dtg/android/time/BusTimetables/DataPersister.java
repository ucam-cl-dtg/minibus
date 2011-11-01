package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

/**
 * 
 * Deals with persisting a serialized object to the file system, and its restoration
 * 
 * @author dt316
 *
 */
public class DataPersister {

	public static String persistObject(Context con, Serializable obj) throws IOException {
		
		String fileName="persisted_" + System.currentTimeMillis();
		
		FileOutputStream fos = con.openFileOutput(fileName, Context.MODE_PRIVATE);
		ObjectOutputStream oos = new ObjectOutputStream(fos);		
		oos.writeObject(obj);		
		oos.close();
		
		return fileName;
		
	}
	
	public static Serializable fetchObject(Context con, String fileName) throws IOException, ClassNotFoundException {
		
		FileInputStream fis = con.openFileInput(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Serializable obj = (Serializable) ois.readObject();
		ois.close();
		
		return obj;
		
	}
	
	public static boolean clearPersisted(Context con, String fileName) {
		
		return con.deleteFile(fileName);
		
	}
}
