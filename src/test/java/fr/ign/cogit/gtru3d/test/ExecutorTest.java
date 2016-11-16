package fr.ign.cogit.gtru3d.test;

import org.junit.Test;

import fr.ign.cogit.geoxygene.sig3d.util.Java3DInstallated;
import fr.ign.cogit.gru3d.regleUrba.Executor;
import junit.framework.Assert;



public class ExecutorTest {
	
	
	private static boolean isJava3DInstalled = Java3DInstallated.isJava3DInstalled();

  /**
   * @param args
   */
  public static void main(String[] args) {

    (new ExecutorTest()).testLoader();
  }
  
  
  @Test
  public void testLoader(){
	  
	  if(!isJava3DInstalled) return;
    
    
    fr.ign.cogit.gru3d.regleUrba.Executor.DATA_REPOSITORY = ExecutorTest.class.getClassLoader()
        .getResource("fr/ign/cogit/gtru/data/").getPath();
    
    try {
      Executor.main(null);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Assert.assertTrue(true);
  }

  

}
