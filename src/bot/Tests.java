package bot;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Test;

public class Tests {
	
	ByteArrayOutputStream myOut;
	
	public void setFileAsInput(String filename) {
		InputStream testInput = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String data = "";
  		String line = "";
  		while((line = in.readLine()) != null)
  		{
  		    data = data + line + "\n\r";
  		}
  		in.close();
			testInput = new ByteArrayInputStream( data.getBytes("UTF-8") );
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.setIn( testInput );
		myOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(myOut));
		BotStarter.main(null);
	}
	
	@Test
	public void inputTest() {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//inputTest.txt");
		String str = myOut.toString().substring(0, 10);
    assertEquals("place_move",str);
	}
	
	@Test
	public void make3Test() throws FileNotFoundException {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//make3Test.txt");
		String str = myOut.toString().substring(0, 14);
//		FileOutputStream f = new FileOutputStream("file.txt");
//		System.setOut(new PrintStream(f));
//		System.out.println(str);
    assertEquals("place_move 2 2",str);
	}
	
	@Test
	public void notOfferOptionsTest() throws FileNotFoundException {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//notOfferOptions.txt");
		String str = myOut.toString();//.substring(0, 14);
  	FileOutputStream f = new FileOutputStream("file.txt");
  	System.setOut(new PrintStream(f));
  	System.out.println(str);
		assertTrue(str.equals("place_move 0 0") || str.equals("place_move 2 2"));
	}

	@Test
	public void block3Test() {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//block3Test.txt");
		String str = myOut.toString().substring(0, 14);
    assertEquals("place_move 2 2",str);
	}
	
	@Test
	public void blockNext3Test() {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//blockNext3Test.txt");
		String str = myOut.toString().substring(0, 14);
		assertTrue(str.equals("place_move 0 0") || str.equals("place_move 2 2"));
	}
	
	@Test
	public void moveScoringTest() {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//moveScoringTest.txt");
		String str = myOut.toString().substring(0, 14);
		assertEquals("place_move 1 0",str);
	}
	
	@Test
	public void macroboardScoreTest() {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//macroboardScoreTest.txt");
		String str = myOut.toString().substring(0, 14);
		assertEquals("place_move 5 3",str);
	}
	
	@Test
	public void winningGameTest() throws FileNotFoundException {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//winningGame.txt");
		String str = myOut.toString().substring(0, 14);
//  	FileOutputStream f = new FileOutputStream("file.txt");
//  	System.setOut(new PrintStream(f));
//  	System.out.println(str);
		assertEquals("place_move 2 6",str);
	}
	
	@Test
	public void uselessMacroboardTest() throws FileNotFoundException {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//uselessMacroboardTest.txt");
		String str1 = myOut.toString().substring(0, 14);
		//String str2 = myOut.toString().substring(16, 30);
//		FileOutputStream f = new FileOutputStream("file.txt");
//		System.setOut(new PrintStream(f));
//		System.out.println(str1);
		assertEquals("place_move 2 7",str1);
//		assertTrue(!"place_move 2 7".equals(str2));
	}
	
	@Test
	public void minMaxTest() throws FileNotFoundException {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//minMaxTest.txt");
		String str = myOut.toString().substring(0, 14);
//		FileOutputStream f = new FileOutputStream("file.txt");
//		System.setOut(new PrintStream(f));
//		System.out.println(str);
		assertEquals("place_move 1 2",str);
	}
	
	@Test
	public void endGameTest() {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//endGameTest.txt");
		String str = myOut.toString().substring(0, 14);
		assertEquals("place_move 1 2",str);
	}
	
	@Test
	public void errorTest() {
		setFileAsInput("C://Users//w.vanderham//workspace//tictactoejava//input//error2.txt");
		String str = myOut.toString().substring(0, 14);
    assertEquals("place_move 2 2",str);
	}
}
