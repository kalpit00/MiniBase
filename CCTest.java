package tests;

import static org.junit.Assert.*;

import java.io.IOException;


import CC.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CCTest  {

    public final static boolean OK = true;
    public final static boolean FAIL = false;
    
    public static int[] newDB()
    {
        return new int[]{0,1,2,3,4,5,6,7,8,9};
    }
	
    /**
     * Schedule 1. Simple testcase with 2 transactions.
     * This testcase is shown in the handout, Part 2.
     */
    @Test
    public void test_A_Simple()
    {
	    // expected output from the given Schedule
        int expectedOutput[] = {0, 2, 3, 3, 4, 5, 6, 7, 8, 9};

	    // input: T1, T2
        List<String> tc1 = Arrays.asList(new String[]{"W(1,5);R(2);W(2,3);R(1);C","R(1);W(1,2);C"});	

	    // Here we are returning the state of the database after executing Schedule 1
        int methodOutput[] = CC.executeSchedule(newDB(),tc1);

        // Here we are checking whether method output matched with the expected output
        // if both match it will pass the test, otherwise it will show an error message
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);

    }

    /**
     * Schedule 2. Testcase with three transactions and no deadlock.
     */
    @Test
    public void test_B_ThreeTransactions()
    {
        
        int expectedOutput[] = {0, 9, 4, 3, 4, 8, 2, 3, 8, 9};
       
        //Testcases
        //2) Three transactions
        List<String> tc2 = Arrays.asList(new String[]{"R(6);W(7,2);W(5,8);W(6,2);C","R(4);W(2,4);R(5);W(7,3);C","R(9);R(6);W(1,9);C"});
        
        int methodOutput[] = CC.executeSchedule(newDB(),tc2);
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
          
    }

    /**
     * Schedule 3. Testcase with three transactions and a deadlock.
     */
    @Test
    public void test_C_Deadlocks()
    {
        
        int expectedOutput[] = {0, 2, 3, 3, 1, 2, 6, 7, 8, 9};
       
        //Testcases
        //3) Deadlocks
        List<String> tc3 = Arrays.asList(new String[]{"W(4,1);R(1);W(1,2);C","W(5,2);R(1);R(2);W(2,3);C","W(6,3);R(2);W(2,4);C"});
        
        int methodOutput[] = CC.executeSchedule(newDB(),tc3);
        
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
         
    }

    /**
     * Schedule 4. Testcase with multiple deadlocks.
     * This testcase is shown in the handout, Part 3 (Deadlock Detection/Handling).
     */
    @Test
    public void test_D_NoConflicts()
    {
        
        int expectedOutput[] = {0, 1, 2, 3, 1, 2, 6, 7, 8, 9};
         
        //Testcases
        //4) No conflicts
        List<String> tc4 = Arrays.asList(new String[]{"R(1);R(2);R(3);W(4,1);C","R(1);R(2);R(3);W(5,2);C"});
        
        int methodOutput[] = CC.executeSchedule(newDB(),tc4);
        
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
         
    }

    /**
     * Schedule 5. Testcase with multiple deadlocks.
     * This testcase is shown in the handout, Part 3 (Deadlock Detection/Handling).
     */
    @Test
    public void test_E_MultipleDeadlocks()
    {
	    // In the Schedule 5 there can be two possible outcomes depending of how you handle deadlocks.
        int expectedOutput[] = {0, 1, 1, 3, 4, 5, 6, 7, 8, 9};
                   
        //Testcases
        //5) Multiple Deadlocks
        List<String> tc5 = Arrays.asList(new String[]{"R(1);W(2,1);C","R(2);R(3);W(1,2);C","R(1);W(3,4);C"});

        int methodOutput[] = CC.executeSchedule(newDB(),tc5);
                
        try{
            // if T3 is aborted, and then T2
            assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
        }catch (AssertionError e) {
            // if we abort T2
            int expectedOutput_anth[] = {0, 1, 1, 4, 4, 5, 6, 7, 8, 9};
            assertArrayEquals("output did not match expected result",expectedOutput_anth, methodOutput);
        }    
    }

    /**
     * Schedule 6. Testcase with large number of transactions (4) and a deadlock.
     */
    @Test
    public void test_F_LargerDeadlock()
    {
        int expectedOutput[] = {0, 1, 1, 2, 3, 5, 6, 7, 8, 9};
                   
        //Testcases
        //6) Larger Deadlock Cycle
        List<String> tc6 = Arrays.asList(new String[]{"R(1);W(2,1);C","R(2);W(3,2);C","R(3);W(4,3);C","R(4);W(1,4);C"});
        
        int methodOutput[] = CC.executeSchedule(newDB(),tc6);
        
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
         
    }

    /**
     * Schedule 7. Testcase with small transactions (2) and a deadlock.
     */
    @Test
    public void test_G_SmallDeadlock()
    {
        int expectedOutput[] = {0, 2, 2, 3, 4, 5, 6, 7, 8, 9};
         
        //Testcases
        //7) Small Deadlock Cycle
        List<String> tc7 = Arrays.asList(new String[]{"W(1,2);R(2);C","W(2,5);R(1);C"});
        
        int methodOutput[] = CC.executeSchedule(newDB(),tc7);
        
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
         
    }

    /**
     * Schedule 8. Testcase with three transactions (2) and a deadlock.
     */
    @Test
    public void test_H_ThreeTransactionsDeadlock()
    {
        int expectedOutput[] = {0, 9, 8, 3, 4, 5, 2, 2, 8, 9};
                 
        //Testcases
        //8) Three Transactions Deadlock
        List<String> tc8 = Arrays.asList(new String[]{"R(6);W(7,2);W(2,8);W(6,2);C","R(4);W(2,4);R(5);W(7,3);C","R(9);R(6);W(1,9);C"});

        int methodOutput[] = CC.executeSchedule(newDB(),tc8);
        
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
         
    }

    /**
     * Schedule 9. Testcase with small transactions (2) and no deadlocks.
     */
    @Test
    public void test_I_SmallTransactionsNoDeadlock()
    {
        int expectedOutput[] = {0, 2, 2, 5, 4, 5, 6, 7, 8, 9};
                 
        //Testcases
        //9) Small Transactions without Deadlock Testcase:
        List<String> tc9 = Arrays.asList(new String[]{"W(1,2);R(2);C","W(3,5);R(4);C"});
    
        int methodOutput[] = CC.executeSchedule(newDB(),tc9);
        
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
         
    }

    /**
     * Schedule 10. Testcase with deadlock between two transactions.
     */
    @Test
    public void test_J_TestCaseDeadlockDeadlock()
    {
        int expectedOutput[] = {0, 5, 3, 3, 4, 5, 6, 7, 8, 9};
                 
        //Testcases
        //10) Testcase with Deadlock:
        List<String> tc10 = Arrays.asList(new String[]{"W(1,5);R(2);W(2,3);R(1);C","R(2);R(1);W(1,2);C"});
        
        int methodOutput[] = CC.executeSchedule(newDB(),tc10);
        
        assertArrayEquals("output did not match expected result",expectedOutput, methodOutput);
         
    }
}
