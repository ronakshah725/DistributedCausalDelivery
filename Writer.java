



import java.io.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Writer {

    static int count;
    static boolean fileExists = true;
    static BufferedWriter writer;
     static File file;
     static int pointer;
   

    public Writer(String path)  {
       file = new File(path);
       
        if (!file.exists())   
            try {
                file.createNewFile();
 
       } catch (IOException ex) {
           Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
       }
            
        }
      public  void write(String msg) throws IOException{
          writer= new BufferedWriter(new FileWriter(file, true));
         
       
          writer.append("\n"+msg);

          writer.close();
      }

      public void close(){
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      
        

    
    
    public static void main(String[] args) throws IOException {
       
        Writer w = new Writer("wew");
        w.write("maa chuda");
  
      
       

    }
}
