package main.sudoku;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter{
	
	//Main main;
	/*
	public KeyInput(Main main)
	{
		this.main = main;
	}
	*/
	public void keyPressed(KeyEvent e)
	{
		Game.keynumber=e.getKeyCode();
	}
	public void keyReleased(KeyEvent e)
	{
		//game.
	}

}
