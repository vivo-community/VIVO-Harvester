import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


public class createFolders 
{
	public static void main(String args[])
	{
		try
		{	//Get the directory name	
			String path = args[0];	
			
			//Store the list of UFID's of the people who don't have an image in VIVO
			HashMap<String, Boolean> listFromVivo = new HashMap<String, Boolean>();				
			FileInputStream fileInputStream = new FileInputStream(path+"/ufids.txt"); 
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(dataInputStream));
			String tempLine;			 
			while ((tempLine = bufferReader.readLine()) != null)   
			{				
				listFromVivo.put(tempLine.substring(0,8),true);
			}			  
			dataInputStream.close();
						
			//Create and transfer images to upload and backup folders
			Runtime.getRuntime().exec("mkdir "+path+"/upload "+path+"/backup");
			File folder = new File(path+"/images");
			String fileName;
			File[] listOfFiles = folder.listFiles(); 
				   
			for (int i = 0; i < listOfFiles.length ; i++) 
			{	
				fileName = listOfFiles[i].getName();						
				if (listOfFiles[i].isFile()) 
				{								
					if(listFromVivo.get(fileName.substring(0, 8)) != null)
					{													
						Runtime.getRuntime().exec("mv "+path+"/images/"+fileName+" "+path+"/upload");							
					}	
					else
					{						
						Runtime.getRuntime().exec("mv "+path+"/images/"+fileName+" "+path+"/backup");						
					}
				}				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	 
	}
}
